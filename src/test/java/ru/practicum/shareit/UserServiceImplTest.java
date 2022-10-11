package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {
    private UserServiceImpl userService;

    private UserRepository userRepository;

    @BeforeEach
    void setUpd() {
        userRepository = mock(UserRepository.class);
        when(userRepository.save(any())).then(invocation -> invocation.getArgument(0));

        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void getUser() {
        var user = new User();

        user.setId(1L);
        user.setName("Егор");
        user.setEmail("egor@mailbox.ru");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        var result = userService.getUserById(user.getId());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }
}
