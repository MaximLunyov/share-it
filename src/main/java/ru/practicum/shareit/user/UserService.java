package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@EnableTransactionManagement
@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private UserRepository userRepository;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new NoSuchElementException();
        }
    }

    @Transactional
    public User createUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getName() == null) {
            throw new ValidationException();
        }
        if (userDto.getEmail().isBlank() && userDto.getName().isBlank()) {
            throw new ValidationException();
        }
        return userRepository.save(UserMapper.toUser(userDto));
    }

    @Transactional
    public User updateUser(long id, UserDto userDto) {
        User first = UserMapper.toUser(userDto);
        User second = findUserById(id);


        if (id != second.getId()) {
            throw new NoSuchElementException();
        }

        if (first.getName() != null && !Objects.equals(first.getName(), second.getName())) {
            second.setName(first.getName());
        }

        if (first.getEmail() != null && !Objects.equals(first.getEmail(), second.getEmail())) {
            second.setEmail(first.getEmail());
        }

        return userRepository.save(second);
    }

    @Transactional
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }
}
