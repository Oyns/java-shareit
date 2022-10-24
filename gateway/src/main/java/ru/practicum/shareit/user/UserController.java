package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.Positive;

import static ru.practicum.shareit.utilities.Validator.validateUserDto;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> saveUser(@RequestBody UserDto userDto) {
        log.info("Creating user={}", userDto);
        validateUserDto(userDto);
        return userClient.saveUser(userDto);
    }

    @PatchMapping("/{userId}")
    @ResponseBody
    public ResponseEntity<Object> updateUserById(@PathVariable @Positive Long userId,
                                                 @RequestBody UserDto userDto) {
        log.info("Updating userId={}, user={}", userId, userDto);
        return userClient.updateUserById(userId, userDto);
    }


    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable @Positive Long userId) {
        log.info("Getting userId={}", userId);
        return userClient.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Getting users");
        return userClient.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUserById(@PathVariable @Positive Long userId) {
        log.info("Delete userId={}", userId);
        return userClient.deleteUserById(userId);
    }
}
