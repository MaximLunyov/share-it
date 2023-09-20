package ru.practicum.shareit.Booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

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

    @Test
    void shouldCreateBooking() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(booker.getId(), bookingDtoStart);

        assertThat(bookingDtoStart.getItemId(), equalTo(bookingDto.getItemId()));
    }

    @Test
    void shouldNotCreateBookingIfItemNotExists() {
        User booker = userService.createUser(userDto2);

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(3L);

        final NoSuchElementException exception = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> bookingService.createBooking(booker.getId(), bookingDtoStart));
    }

    @Test
    void shouldNotCreateBookingIfItemNotAvailable() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());
        itemDto1.setAvailable(false);
        itemService.updateItem(item.getId(), itemDto1, user.getId());

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(booker.getId(), bookingDtoStart));
    }

    @Test
    void shouldNotCreateBookingIfBookerIsOwner() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());

        final NoSuchElementException exception = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> bookingService.createBooking(user.getId(), bookingDtoStart));
    }

    @Test
    void shouldUpdateBooking() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(booker.getId(), bookingDtoStart);
        BookingDto updatedBooking = bookingService.updateBooking(user.getId(), bookingDto.getId(), false);

        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.REJECTED));

        BookingDto updatedBooking2 = bookingService.updateBooking(user.getId(), bookingDto.getId(), true);

        assertThat(updatedBooking2.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void shouldNotUpdateBookingIfWrongOwner() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(booker.getId(), bookingDtoStart);

        final NoSuchElementException exception = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> bookingService.updateBooking(booker.getId(), bookingDto.getId(), false));
    }

    @Test
    void shouldGetBookingById() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(booker.getId(), bookingDtoStart);

        BookingShortDto checked = bookingService.getById(user.getId(), bookingDto.getId());

        assertThat(checked.getId(), equalTo(bookingDto.getId()));
        assertThat(checked.getStart(), equalTo(bookingDto.getStart()));
        assertThat(checked.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(checked.getStatus(), equalTo(bookingDto.getStatus()));

    }

    @Test
    void shouldNotGetBookingByIdIfNotOwner() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(booker.getId(), bookingDtoStart);

        final NoSuchElementException exception = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> bookingService.getById(100L, bookingDto.getId()));

    }

    @Test
    void shouldGetAllBookingsByOwnerWithStateAll() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(booker.getId(), bookingDtoStart);

        List<BookingShortDto> bookingShortDtos = bookingService.getByOwnerId(user.getId(), "ALL", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }

    @Test
    void shouldGetAllBookingsByOwnerWithStateRejected() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(booker.getId(), bookingDtoStart);
        bookingService.updateBooking(user.getId(), bookingDto.getId(), false);
        List<BookingShortDto> bookingShortDtos = bookingService.getByOwnerId(user.getId(), "REJECTED", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }

    @Test
    void shouldGetAllBookingsByOwnerWithStateWaiting() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(booker.getId(), bookingDtoStart);

        List<BookingShortDto> bookingShortDtos = bookingService.getByOwnerId(user.getId(), "WAITING", 0, 100);

        assertThat(bookingShortDtos.size(), equalTo(1));
    }
}