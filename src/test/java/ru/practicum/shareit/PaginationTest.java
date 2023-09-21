package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PaginationTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private UserDto userDto1 = new UserDto(100L, "Max", "max@mail.ru");
    private UserDto userDto2 = new UserDto(101L, "Ivan", "ivan@ya.ru");
    private ItemDto itemDto1 = new ItemDto(101L, "Hummer", "Small", true, null, null, null, null);
    private ItemDto itemDto2 = new ItemDto(102L, "Fork", "Tiny", true, null, null, null, null);


    @Test
    void shouldThrowValidationExceptionByWrongPaginationSize() {
        User user = userService.createUser(userDto1);
        itemService.createItem(itemDto1, user.getId());
        itemService.createItem(itemDto2, user.getId());
        Assertions.assertThrows(ValidationException.class,
                () -> itemService.getItemsByOwner(user.getId(), 0, -1));
        Assertions.assertThrows(ValidationException.class,
                () -> itemService.getItemsByOwner(user.getId(), 0, 0));
    }

    @Test
    void shouldGetCorrectDataIfFromEqualsSize() {
        User user = userService.createUser(userDto1);
        itemService.createItem(itemDto1, user.getId());
        itemService.createItem(itemDto2, user.getId());
        List<ItemDto> items = itemService.getItemsByOwner(user.getId(), 1, 1);
        assertThat(items.size(), equalTo(1));
    }
}
