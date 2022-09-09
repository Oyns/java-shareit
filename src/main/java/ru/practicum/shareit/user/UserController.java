package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;


@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserServiceImpl userServiceImpl;

    @Autowired
    public UserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping
    @ResponseBody
    public User createUser(@RequestBody UserDto userDto) {
        return userServiceImpl.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    @ResponseBody
    public User updateUserInfo(@PathVariable Long userId,
                               @RequestBody UserDto userDto) {
        return userServiceImpl.updateUserInfo(userId, userDto);
    }


    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userServiceImpl.getUserById(userId);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userServiceImpl.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        userServiceImpl.deleteUserById(userId);
    }

}
