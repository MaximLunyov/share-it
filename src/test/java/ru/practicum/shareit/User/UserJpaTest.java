package ru.practicum.shareit.User;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserJpaTest {
    private final UserService userService;
    private User user = new User(1L, "Max", "dawd@gmail.com");

    @Test
    void shouldReturnAfterCreating() {
        User user1 = userService.createUser(UserMapper.toUserDto(user));
        assertThat(user.getName(), equalTo(user1.getName()));
        assertThat(user.getEmail(), equalTo(user1.getEmail()));
    }

    @Test
    void shouldReturnUserById() {
        User user2 = userService.createUser(UserMapper.toUserDto(user));
        User user1 = userService.findUserById(user2.getId());
        assertThat(user.getName(), equalTo(user1.getName()));
        assertThat(user.getEmail(), equalTo(user1.getEmail()));
    }

    @Test
    void shouldReturnAllUsers() {
        userService.createUser(UserMapper.toUserDto(user));
        userService.createUser(UserMapper.toUserDto(new User(2L, "Oleg", "oleja@mail.ru")));
        List<User> userList = userService.findAllUsers();
        assertThat(userList.size(), equalTo(2));
    }

    @Test
    void shouldUpdateUser() {
        User user1 = userService.createUser(UserMapper.toUserDto(user));
        user.setName("UpdatedName");
        User user2 = userService.updateUser(user1.getId(), UserMapper.toUserDto(user));
        assertThat(user1.getName(), equalTo("UpdatedName"));
    }

    @Test
    void shouldNotPassWithoutName() {
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> userService.createUser(new UserDto(1L, "", "email@email.ru")));
    }

    @Test
    void shouldNotPassWithWrongEmail() {
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> userService.createUser(new UserDto(1L, "dwadaw", "emailemail.ru")));
    }

    @Test
    void shouldNotSaveWhenUserWithExistEmail() {
        userService.createUser(UserMapper.toUserDto(new User(2L, "TestUser", "test@test.gmail")));

        final DataIntegrityViolationException exception = Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.createUser(UserMapper.toUserDto(new User(3L,
                        "NextUser", "test@test.gmail"))));
    }
}
