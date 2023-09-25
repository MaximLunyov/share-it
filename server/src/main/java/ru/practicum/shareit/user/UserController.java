package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> findAllUsers() {
        log.info("Получен запрос на получение списка пользователей");
        return userService.findAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        log.info("Получен запрос на добавление пользователя с id: " + id);
        return userService.findUserById(id);
    }

    @PostMapping
    public User create(@RequestBody UserDto userDto) {
        log.info("Получен запрос на добавление пользователя: " + userDto);
        return userService.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public User update(@PathVariable long id, @RequestBody UserDto userDto) {
        log.info("Получен запрос на обновление пользователя: " + id);
        return userService.updateUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        log.info("Получен запрос на удаление пользователя c id: " + id);
        userService.deleteUser(id);
    }

}
