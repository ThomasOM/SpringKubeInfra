package me.thomazz.userservice.controller;

import lombok.RequiredArgsConstructor;
import me.thomazz.userservice.entities.UserByIdRequest;
import me.thomazz.userservice.entities.UserDto;
import me.thomazz.userservice.entities.UserLoginRequest;
import me.thomazz.userservice.entities.UserLoginResponse;
import me.thomazz.userservice.entities.UserRegisterRequest;
import me.thomazz.userservice.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return this.service.getAllUsers();
    }

    @GetMapping("id")
    public UserDto getUserById(@RequestBody UserByIdRequest request) {
        return this.service.getUserById(request.getId());
    }

    @PostMapping("register")
    public void registerUser(@RequestBody UserRegisterRequest request) {
        this.service.registerUser(request.getUsername(), request.getPassword());
    }

    @PostMapping("login")
    public UserLoginResponse loginUser(@RequestBody UserLoginRequest request) {
        String token = this.service.loginUser(request.getUsername(), request.getPassword());
        return UserLoginResponse.builder().accessToken(token).build();
    }
}
