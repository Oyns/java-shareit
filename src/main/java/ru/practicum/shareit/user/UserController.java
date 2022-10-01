package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
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
    public UserDto saveUser(@RequestBody UserDto userDto) {
        return userServiceImpl.saveUser(userDto);
    }

    @PatchMapping("/{userId}")
    @ResponseBody
    public UserDto updateUserById(@PathVariable Long userId,
                                  @RequestBody UserDto userDto) {
        return userServiceImpl.updateUserById(userId, userDto);
    }


    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        return userServiceImpl.getUserById(userId);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userServiceImpl.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        userServiceImpl.deleteUserById(userId);
    }

}
