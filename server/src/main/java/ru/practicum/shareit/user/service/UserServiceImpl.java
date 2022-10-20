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

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        return toUserDto(userRepository.save(toUser(userDto)));
    }

    @Override
    public UserDto updateUserById(Long userId, UserDto userDto) {
        validateForUserUpdate(userId, userDto);
        return toUserDto(userRepository.save(toUser(userDto)));
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

    private void validateForUserUpdate(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId).orElseThrow();
        if (userDto.getEmail() == null) {
            userDto.setEmail(user.getEmail());
        }
        if (userDto.getName() == null) {
            userDto.setName(user.getName());
        }
        userDto.setId(user.getId());
    }
}
