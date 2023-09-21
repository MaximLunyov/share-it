package ru.practicum.shareit.Booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingModelTest {
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    private UserDto userDto1 = new UserDto(100L, "Max", "max@mail.ru");
    private UserDto userDto2 = new UserDto(101L, "Ivan", "ivan@ya.ru");
    private ItemDto itemDto1 = new ItemDto(101L, "Hummer", "Small", true, null, null, null, null);
    private ItemDto itemDto2 = new ItemDto(102L, "Fork", "Tiny", true, null, null, null, null);

    @Test
    void shouldReturnToString() {
        User user = userService.createUser(userDto1);
        User booker = userService.createUser(userDto2);

        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.of(2023, 1, 1, 1, 1));
        bookingDtoStart.setEnd(LocalDateTime.of(2023, 2, 1, 1, 1));
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(booker.getId(), bookingDtoStart);
        String forCheck = "Booking{id=0, start=2023-01-01T01:01, end=2023-02-01T01:01," +
                " item=Item{id=" + bookingMapper.toBooking(bookingDto).getItem().getId() +
                ", name='Hummer', description='Small', available=true, " +
                "userId=" + bookingMapper.toBooking(bookingDto).getItem().getUserId() + "}" +
                ", booker=User{id=" + bookingMapper.toBooking(bookingDto).getBooker().getId() +
                ", name='Ivan', email='ivan@ya.ru'}, status=WAITING}";
        assertThat(bookingMapper.toBooking(bookingDto).toString(), equalTo(forCheck));
    }
}
