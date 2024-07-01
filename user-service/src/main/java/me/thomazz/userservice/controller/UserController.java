package me.thomazz.userservice.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.thomazz.userservice.dto.UserByIdRequest;
import me.thomazz.userservice.dto.UserDeleteByIdRequest;
import me.thomazz.userservice.dto.UserDto;
import me.thomazz.userservice.dto.UserGetAllRequest;
import me.thomazz.userservice.dto.UserLoginRequest;
import me.thomazz.userservice.dto.UserRegisterRequest;
import me.thomazz.userservice.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping
    public List<UserDto> getAllUsers(@RequestBody UserGetAllRequest request) {
        return this.service.getAllUsers(PageRequest.of(request.getPageNumber(), request.getPageSize()));
    }

    @GetMapping("id")
    public UserDto getUserById(@RequestBody UserByIdRequest request) {
        return this.service.getUserById(request.getId());
    }

    @PostMapping("register")
    public void registerUser(@RequestBody UserRegisterRequest request) {
        this.service.registerUser(request.getUsername(), request.getPassword());
    }

    @DeleteMapping("id")
    public void deleteUserById(@RequestBody UserDeleteByIdRequest request) {
        this.service.deleteUser(request.getId());
    }

    @PostMapping("login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest loginRequest, HttpServletResponse response) {
        String token = this.service.loginUser(loginRequest.getUsername(), loginRequest.getPassword());

        Cookie cookie = new Cookie("spring_kube_infra_login_token", token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) Duration.ofMinutes(15L).toSeconds()); // Cookie expires in 15 minutes

        // cookie.setSecure(true); // Enable this when using HTTPS

        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }
}
