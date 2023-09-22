package ru.practicum.shareit.Booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingJpaTest {

    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    private UserDto userDto1 = new UserDto(100L, "Max", "max@mail.ru");
    private UserDto userDto2 = new UserDto(101L, "Ivan", "ivan@ya.ru");
    private ItemDto itemDto1 = new ItemDto(101L, "Hummer", "Small", true, null, null, null, null);
    private ItemDto itemDto2 = new ItemDto(102L, "Fork", "Tiny", true, null, null, null, null);
    private User user;
    private User secondUser;
    private Item item;
    private BookingDto bookingDtoStart;

    @BeforeEach
    void set() {
        user = userService.createUser(userDto1);
        secondUser = userService.createUser(userDto2);
        item = itemService.createItem(itemDto1, user.getId());
        bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());
    }

    
    @Test
    void shouldCreateBooking() {

        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);

        bookingService.updateBooking(user.getId(), bookingDto.getId(), true);

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        CommentDto commentDto = new CommentDto(1L, "Comment1",
                secondUser.getName(), LocalDateTime.now());

        CommentDto finalCommentDto = itemService.createComment(CommentMapper.toComment(commentDto),
                secondUser.getId(), item.getId());
        assertThat(bookingDtoStart.getItemId(), equalTo(bookingDto.getItemId()));
    }

    @Test
    void shouldNotCreateBookingIfItemNotExists() {
        bookingDtoStart.setItemId(3L);

        final NoSuchElementException exception = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> bookingService.createBooking(secondUser.getId(), bookingDtoStart));
    }

    @Test
    void shouldNotCreateBookingIfItemNotAvailable() {
        itemDto1.setAvailable(false);
        itemService.updateItem(item.getId(), itemDto1, user.getId());

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(secondUser.getId(), bookingDtoStart));
    }

    @Test
    void shouldNotCreateBookingIfBookerIsOwner() {
        final NoSuchElementException exception = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> bookingService.createBooking(user.getId(), bookingDtoStart));
    }

    @Test
    void shouldThrowValidationExceptionByWrongBookingTime() {
        bookingDtoStart.setStart(null);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(secondUser.getId(), bookingDtoStart));

        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(4));
        final ValidationException exception2 = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(secondUser.getId(), bookingDtoStart));

        bookingDtoStart.setStart(LocalDateTime.of(2023,1,1,1,1));
        bookingDtoStart.setEnd(LocalDateTime.of(2023,1,1,1,1));
        final ValidationException exception3 = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(secondUser.getId(), bookingDtoStart));
    }

    @Test
    void shouldUpdateBooking() {

        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        BookingDto updatedBooking = bookingService.updateBooking(user.getId(), bookingDto.getId(), false);

        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.REJECTED));

        BookingDto updatedBooking2 = bookingService.updateBooking(user.getId(), bookingDto.getId(), true);

        assertThat(updatedBooking2.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void shouldNotUpdateBookingIfWrongOwner() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);

        final NoSuchElementException exception = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> bookingService.updateBooking(secondUser.getId(), bookingDto.getId(), false));
    }

    @Test
    void shouldGetBookingById() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);

        BookingShortDto checked = bookingService.getById(user.getId(), bookingDto.getId());

        assertThat(checked.getId(), equalTo(bookingDto.getId()));
        assertThat(checked.getStart(), equalTo(bookingDto.getStart()));
        assertThat(checked.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(checked.getStatus(), equalTo(bookingDto.getStatus()));

    }

    @Test
    void shouldNotGetBookingByIdIfNotOwner() {
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);

        final NoSuchElementException exception = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> bookingService.getById(100L, bookingDto.getId()));

    }

    @Test
    void shouldNotGetAllBookingsByOwnerWithStateAll() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);

        List<BookingShortDto> bookingShortDtos = bookingService.getByOwnerId(user.getId(), "ALL", 0, 100);

        final NoSuchElementException exception = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> bookingService.getById(100L, bookingDto.getId()));
    }

    @Test
    void shouldGetAllBookingsByOwnerWithStateCurrent() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByOwnerId(user.getId(), "CURRENT", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(0));
    }

    @Test
    void shouldGetAllBookingsByOwnerWithStateFuture() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByOwnerId(user.getId(), "FUTURE", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }

    @Test
    void shouldGetAllBookingsByOwnerWithStatePast() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByOwnerId(user.getId(), "PAST", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(0));
    }

    @Test
    void shouldThrowExceptionIfGetAllBookingsByOwnerWithStateUnknown() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.getByOwnerId(user.getId(), "UNKNOWN", 0, 100));
    }

    @Test
    void shouldGetAllBookingsByOwnerWithStateRejected() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        bookingService.updateBooking(user.getId(), bookingDto.getId(), false);
        List<BookingShortDto> bookingShortDtos = bookingService.getByOwnerId(user.getId(), "REJECTED", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }

    @Test
    void shouldGetAllBookingsByOwnerWithStateWaiting() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByOwnerId(user.getId(), "WAITING", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }

    @Test
    void shouldGetAllBookingsByOwnerWithStateAllWithoutSize() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByOwnerId(user.getId(), "ALL", 0, null);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }

    @Test
    void shouldThrowValidationExceptionByWrongUpdateInfo() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.updateBooking(user.getId(), bookingDto.getId(), null));

        bookingService.updateBooking(user.getId(), bookingDto.getId(), true);
        final ValidationException exception2 = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.updateBooking(user.getId(), bookingDto.getId(), true));

        bookingService.updateBooking(user.getId(), bookingDto.getId(), false);
        final ValidationException exception3 = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.updateBooking(user.getId(), bookingDto.getId(), false));
    }

    @Test
    void shouldGetAllBookingsByUserWithStateAll() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByUserId(secondUser.getId(), "ALL", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }

    @Test
    void shouldGetAllBookingsByUserWithStateCurrent() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByUserId(secondUser.getId(), "CURRENT", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(0));
    }

    @Test
    void shouldGetAllBookingsByUserWithStatePast() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByUserId(secondUser.getId(), "PAST", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(0));
    }

    @Test
    void shouldGetAllBookingsByUserWithStateFuture() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByUserId(secondUser.getId(), "FUTURE", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }

    @Test
    void shouldGetAllBookingsByUserWithStateRejected() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByUserId(secondUser.getId(), "REJECTED", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(0));
    }

    @Test
    void shouldGetAllBookingsByUserWithStateWaiting() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByUserId(secondUser.getId(), "WAITING", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }

    @Test
    void shouldThrowValidationExceptionByUserWithStateUnknown() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);

        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getByUserId(secondUser.getId(), "UNKNOWN", 0, 100));
    }

    @Test
    void shouldGetAllBookingsByUserWithStateWaitingAndEmptySize() {
        BookingDto bookingDto = bookingService.createBooking(secondUser.getId(), bookingDtoStart);
        List<BookingShortDto> bookingShortDtos = bookingService.getByUserId(secondUser.getId(), "WAITING", 0, null);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }
}
