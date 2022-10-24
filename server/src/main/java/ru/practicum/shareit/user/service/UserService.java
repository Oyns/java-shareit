package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto saveUser(UserDto userDto);

    UserDto updateUserById(Long userId, UserDto userDto);

    UserDto getUserById(Long userId);

    List<UserDto> getAllUsers();

    void deleteUserById(Long userId);
}
