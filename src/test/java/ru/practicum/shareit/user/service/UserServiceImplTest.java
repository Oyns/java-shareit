package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.user.mapper.UserMapper.toUser;
import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;

public class UserServiceImplTest {
    private UserServiceImpl userService;

    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);

        userService = new UserServiceImpl(userRepository);
        user = new User(1L, "Егор", "egor@mailbox.ru");
    }

    @Test
    void saveUser() {
        when(userRepository.save(user)).thenReturn(user);

        UserDto userDto = userService.saveUser(toUserDto(user));

        assertNotNull(userDto);
        assertEquals(toUserDto(user), userDto);
    }

    @Test
    void saveUserWithEmptyEmail() {
        when(userRepository.save(user)).thenReturn(user);

        user.setEmail("");

        ValidationException thrown = assertThrows(ValidationException.class, () ->
                userService.saveUser(toUserDto(user)));
        assertEquals("Некорректный email.", thrown.getMessage());
    }

    @Test
    void saveUserWithNoEmail() {
        when(userRepository.save(user)).thenReturn(user);

        user.setEmail(null);

        ValidationException thrown = assertThrows(ValidationException.class, () ->
                userService.saveUser(toUserDto(user)));
        assertEquals("Некорректный email.", thrown.getMessage());
    }

    @Test
    void updateUserById() {
        when(userRepository.save(user)).thenReturn(user);

        UserDto userDto = userService.saveUser(toUserDto(user));

        assertNotNull(userDto);
        assertEquals(toUserDto(user), userDto);
    }

    @Test
    void getUserById() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto userDto = userService.getUserById(user.getId());

        assertNotNull(userDto);
        assertEquals(user, toUser(userDto));
    }

    @Test
    void getAllUsers() {
        final List<User> users = List.of(user);

        when(userRepository.findAll())
                .thenReturn(users);

        final List<UserDto> userDtos = userService.getAllUsers();

        assertNotNull(userDtos);
        assertEquals(1, userDtos.size());
        assertEquals(toUserDto(user), userDtos.get(0));
    }

    @Test
    void deleteUserById() {
        when(userRepository.save(user)).thenReturn(user);
        userRepository.deleteById(user.getId());
        userService.deleteUserById(user.getId());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(user.getId()));
    }
}
