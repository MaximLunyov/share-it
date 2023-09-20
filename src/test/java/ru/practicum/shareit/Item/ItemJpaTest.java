package ru.practicum.shareit.Item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemJpaTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private UserDto userDto1 = new UserDto(100L, "Max", "max@mail.ru");
    private UserDto userDto2 = new UserDto(101L, "Ivan", "ivan@ya.ru");
    private ItemDto itemDto1 = new ItemDto(101L, "Hummer", "Small", true, null, null, null, null);
    private ItemDto itemDto2 = new ItemDto(102L, "Fork", "Tiny", true, null, null, null, null);

    @Test
    void shouldCreateItem() {
        User user = userService.createUser(userDto2);
        Item item = itemService.createItem(itemDto2, user.getId());
        assertThat(user.getId(), equalTo(item.getUserId()));
        assertThat(itemDto2.getName(), equalTo(item.getName()));
    }

    @Test
    void shouldNotCreateItemByValidation() {
        final NoSuchElementException exception1 = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> itemService.createItem(itemDto1, -1));

        userService.createUser(userDto1);
        final ValidationException exception2 = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.createItem(new ItemDto(10L, "", "dawd",
                        true, null, null, null, null), 1));
        final ValidationException exception3 = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.createItem(new ItemDto(10L, "dawd", " ",
                        true, null, null, null, null), 1));

    }

    @Test
    void shouldUpdateItem() {
        User user = userService.createUser(userDto1);
        Item item = itemService.createItem(itemDto1, user.getId());
        item.setName("Big Hummer");
        Item item2 = itemService.updateItem(item.getId(), ItemMapper.toItemDto(item), user.getId());
        assertThat(item2.getName(), equalTo("Big Hummer"));
    }

    @Test
    void shouldNotUpdateByWrongOwnerId() {

        User user = userService.createUser(userDto1);
        User updater = userService.createUser(userDto2);
        Item item = itemService.createItem(itemDto1, user.getId());
        item.setName("Big Hummer");
        final NoSuchElementException exception = Assertions.assertThrows(
                NoSuchElementException.class,
                () -> itemService.updateItem(item.getId(), ItemMapper.toItemDto(item), updater.getId()));
    }

    @Test
    void shouldGetItemsByOwner() {
        User user = userService.createUser(userDto1);
        itemService.createItem(itemDto1, user.getId());
        itemService.createItem(itemDto2, user.getId());
        List<ItemDto> listItems = itemService.getItemsByOwner(user.getId(), 0, 10);
        assertEquals(2, listItems.size());
    }

    @Test
    void shouldGetItemsBySearch() {
        User user = userService.createUser(userDto1);
        itemService.createItem(itemDto1, user.getId());
        itemService.createItem(itemDto2, user.getId());
        List<Item> listItems = itemService.searchByText("hummer", 0, 1);
        assertEquals(1, listItems.size());
    }

    @Test
    void shouldCreateComment() {
        User user = userService.createUser(userDto1);
        User commentor = userService.createUser(userDto2);
        Item item = itemService.createItem(itemDto1, user.getId());

        BookingDto bookingDtoStart = new BookingDto();
        bookingDtoStart.setId(1L);
        bookingDtoStart.setStart(LocalDateTime.now().plusSeconds(1));
        bookingDtoStart.setEnd(LocalDateTime.now().plusSeconds(4));
        bookingDtoStart.setItemId(item.getId());

        BookingDto bookingDto = bookingService.createBooking(commentor.getId(), bookingDtoStart);
        bookingService.updateBooking(user.getId(), bookingDto.getId(), true);

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        CommentDto commentDto = new CommentDto(1L, "Comment1",
                commentor.getName(), LocalDateTime.now());

        CommentDto finalCommentDto = itemService.createComment(CommentMapper.toComment(commentDto),
                commentor.getId(), item.getId());
        assertThat(finalCommentDto.getAuthorName(), equalTo(commentDto.getAuthorName()));
    }

    @Test
    void shouldNotCreateComment() {
        User user1 = userService.createUser(userDto1);
        User user2 = userService.createUser(userDto2);
        Item item = itemService.createItem(itemDto1, user1.getId());
        LocalDateTime time = LocalDateTime.now();

        CommentDto commentDto = new CommentDto(1L, "Nice", user2.getName(), time.plusHours(2));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.createComment(CommentMapper.toComment(commentDto), user2.getId(), item.getId()));
    }
}