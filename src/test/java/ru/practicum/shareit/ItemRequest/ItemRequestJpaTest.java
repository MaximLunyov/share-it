package ru.practicum.shareit.ItemRequest;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestJpaTest {

    private final UserService userService;
    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;

    private UserDto userDto1 = new UserDto(100L, "Max", "max@mail.ru");
    private UserDto userDto2 = new UserDto(101L, "Ivan", "ivan@ya.ru");

    private ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "Searching for camera",
            userDto1, LocalDateTime.of(2023, 9, 21, 18, 0, 0), null);
    private User user;

    @BeforeEach
    void set() {
        user = userService.createUser(userDto1);
    }

    @Test
    void shouldCreateItemRequest() {
        ItemRequestDto returnRequestDto = itemRequestService.create(itemRequestDto, user.getId(),
                LocalDateTime.of(2023, 9, 21, 18, 0, 0));
        assertThat(returnRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void shouldNotCreateItemRequestWithWrongUserId() {
        NoSuchElementException exp = assertThrows(NoSuchElementException.class,
                () -> itemRequestService.create(itemRequestDto, 1021L,
                        LocalDateTime.of(2023, 9, 21, 18, 0, 0)));
    }

    @Test
    void shouldNotCreateItemRequestWithWrongId() {
        NoSuchElementException exp = assertThrows(NoSuchElementException.class,
                () -> itemRequestService.getItemRequestById(100L, user.getId()));
    }

    @Test
    void shouldReturnOwnItemRequests() {
        itemRequestService.create(itemRequestDto, user.getId(),
                LocalDateTime.of(2023, 9, 21, 18, 0, 0));
        itemRequestService.create(itemRequestDto, user.getId(),
                LocalDateTime.of(2023, 9, 21, 18, 0, 0));
        List<ItemRequestDto> listItemRequest = itemRequestService.getOwnItemRequests(user.getId());

        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void shouldReturnItemRequestById() {
        ItemRequestDto newItemRequestDto = itemRequestService.create(itemRequestDto, user.getId(),
                LocalDateTime.of(2023, 9, 21, 18, 0, 0));

        ItemRequestDto returnItemRequestDto = itemRequestService.getItemRequestById(newItemRequestDto.getId(),
                user.getId());

        assertThat(returnItemRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void shouldReturnItemRequestToString() {
        ItemRequestDto returnRequestDto = itemRequestService.create(itemRequestDto, user.getId(),
                LocalDateTime.of(2023, 9, 21, 18, 0, 0));
        String check = "ItemRequestDto(id=" + returnRequestDto.getId() +
                ", description=Searching for camera, requestor=UserDto(id=" + UserMapper.toUserDto(user).getId() +
                ", name=Max," +
                " email=max@mail.ru), created=" + LocalDateTime.of(2023, 9, 21, 18, 0, 0) +
                ", items=[])";
        assertThat(check, equalTo(itemRequestService.getItemRequestById(returnRequestDto.getId(),
                user.getId()).toString()));
    }

    @Test
    void shouldGetAllItemRequests() {
        User user2 = userService.createUser(userDto2);
        itemRequestService.create(itemRequestDto, user.getId(),
                LocalDateTime.of(2023, 9, 21, 18, 0, 0));
        itemRequestService.create(itemRequestDto, user.getId(),
                LocalDateTime.of(2023, 9, 21, 18, 0, 0));

        List<ItemRequestDto> list = itemRequestService.getAllItemRequests(user2.getId(),  0, 100);
        assertThat(list.size(), equalTo(2));
    }

    @Test
    void shouldGetAllItemRequestsWithEmptySize() {
        User user2 = userService.createUser(userDto2);
        itemRequestService.create(itemRequestDto, user.getId(),
                LocalDateTime.of(2023, 9, 21, 18, 0, 0));
        itemRequestService.create(itemRequestDto, user.getId(),
                LocalDateTime.of(2023, 9, 21, 18, 0, 0));

        List<ItemRequestDto> list = itemRequestService.getAllItemRequests(user2.getId(),  0, null);
        assertThat(list.size(), equalTo(4));
    }

}
