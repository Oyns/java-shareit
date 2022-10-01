package ru.practicum.shareit.user.service;


import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.shareit.user.mapper.UserMapper.toUser;
import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;
import static ru.practicum.shareit.utilities.Validator.validateUserDto;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        validateUserDto(userDto);
        return toUserDto(userRepository.save(toUser(userDto)));
    }

    @Override
    public UserDto updateUserById(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId).orElseThrow();
        validateForUserUpdate(user, userDto);
        userRepository.save(user);
        return toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        try {
            return toUserDto(userRepository.findById(userId).orElseThrow());
        } catch (Exception e) {
            throw new EntityNotFoundException("Пользователя с таким id не существует");
        }
    }


    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            userDtos.add(toUserDto(user));
        }
        return userDtos;
    }

    @Override
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    private void validateForUserUpdate(User user, UserDto userDto) {
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
    }
}
