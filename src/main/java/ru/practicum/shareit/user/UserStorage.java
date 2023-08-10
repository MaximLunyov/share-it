package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {

    List<User> findAllUsers();

    User findUserById(long id);

    User createUser(User user);

    User updateUser(long id, User user);

    void deleteUser(long id);
}
