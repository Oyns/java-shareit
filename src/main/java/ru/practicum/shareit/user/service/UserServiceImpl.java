package ru.practicum.shareit.user.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.shareit.utilities.Validator.userValidator;

@Repository
public class UserServiceImpl implements UserService {

    private final List<User> users = new ArrayList<>();
    private Long id = 1L;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User createUser(UserDto userDto) {
        userValidator(userDto);
        emailConflictValidation(userDto);
        userDto.setId(id++);
        users.add(userMapper.toUser(userDto));
        return userMapper.toUser(userDto);
    }

    @Override
    public User updateUserInfo(Long userId, UserDto userDto) {
        User user = users.stream()
                .filter(user1 -> user1.getId().equals(userId))
                .findAny()
                .orElseThrow();
        emailConflictValidation(userDto);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findAny()
                .orElseThrow();
    }

    @Override
    public List<User> getAllUsers() {
        return users;
    }

    @Override
    public void deleteUserById(Long userId) {
        users.removeIf(user -> user.getId().equals(userId));
    }

    private void emailConflictValidation(UserDto userDto) {
        for (User user : users) {
            if (user.getEmail().equals(userDto.getEmail())) {
                throw new ConflictException("Пользователь с таким email уже существует.");
            }
        }
    }
}
