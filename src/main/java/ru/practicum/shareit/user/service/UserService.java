package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User createUser(UserDto userDto);

    User updateUserInfo(Long userId, UserDto userDto);

    User getUserById(Long userId);

    List<User> getAllUsers();

    void deleteUserById(Long userId);
}
