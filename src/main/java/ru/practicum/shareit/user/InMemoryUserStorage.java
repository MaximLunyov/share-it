package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.model.ConflictException;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private Map<Long, User> userMap = new HashMap<>();
    private Map<Long, String> emails = new HashMap<>();
    protected long id = 0;

    @Override
    public List<User> findAllUsers() {
        log.info(emails.toString());
        return new ArrayList<>(userMap.values());
    }

    @Override
    public User findUserById(long id) {
        return userMap.get(id);
    }

    @Override
    public User createUser(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (emails.containsValue(user.getEmail())) {
            throw new ConflictException("Пользователь с указанным email уже существует");
        }

        user.setId(++id);
        emails.put(user.getId(), user.getEmail());
        userMap.put(user.getId(), user);

        return user;
    }

    @Override
    public User updateUser(long id, User user) {

        if (!userMap.containsKey(id)) {
            throw new NoSuchElementException("Пользователь с указанным id не найден");
        }

        User user1 = userMap.get(id);

        if (!emails.containsValue(user.getEmail()) || emails.get(id).equals(user.getEmail())) {
            if (user.getName() == null) {
                user.setName(user1.getName());
            }
            if (user.getEmail() == null) {
                user.setEmail(user1.getEmail());
            } else {
                emails.replace(id, user.getEmail());
            }
            user.setId(user1.getId());
        } else {
            throw new ConflictException("Пользователь с указанным email уже существует");
        }

        userMap.replace(id, user);
        return user;
    }

    @Override
    public void deleteUser(long id) {
        if (userMap.containsKey(id)) {
            userMap.remove(id);
            emails.remove(id);
        } else {
            throw new NoSuchElementException("Пользователь с указанным id не найден");
        }
    }
}
