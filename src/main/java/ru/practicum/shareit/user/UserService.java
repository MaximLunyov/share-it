package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private UserStorage userStorage;

    public List<User> findAllUsers() {
        return userStorage.findAllUsers();
    }

    public User findUserById(long id) {
        return userStorage.findUserById(id);
    }

    public User createUser(UserDto userDto) {
        return userStorage.createUser(UserMapper.toUser(userDto));
    }

    public User updateUser(long id, UserDto userDto) {
        return userStorage.updateUser(id, UserMapper.toUser(userDto));
    }

    public void deleteUser(long id) {
        userStorage.deleteUser(id);
    }
}
