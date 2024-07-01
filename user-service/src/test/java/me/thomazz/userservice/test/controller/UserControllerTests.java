package me.thomazz.userservice.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.thomazz.userservice.dto.UserByIdRequest;
import me.thomazz.userservice.dto.UserDeleteByIdRequest;
import me.thomazz.userservice.dto.UserDto;
import me.thomazz.userservice.dto.UserLoginRequest;
import me.thomazz.userservice.dto.UserRegisterRequest;
import me.thomazz.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTests {
    private final MockMvc mockMvc;
    private final ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Test
    @Order(1)
    @DisplayName("Get all users")
    public void testGetAllUsers() throws Exception {
        UserDto user1 = UserDto.builder()
            .id(1L)
            .username("test1")
            .build();

        UserDto user2 = UserDto.builder()
            .id(2L)
            .username("test2")
            .build();

        when(this.userService.getAllUsers()).thenReturn(List.of(user1, user2));

        UserDto expected1 = UserDto.builder()
            .id(1L)
            .username("test1")
            .build();

        UserDto expected2 = UserDto.builder()
            .id(2L)
            .username("test2")
            .build();

        this.mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(this.mapper.writeValueAsString(List.of(expected1, expected2))));
    }

    @Test
    @Order(2)
    @DisplayName("Get user by ID")
    public void testGetUserById() throws Exception {
        UserDto user = UserDto.builder()
            .id(1L)
            .username("test")
            .build();

        when(this.userService.getUserById(1L)).thenReturn(user);

        UserByIdRequest request = UserByIdRequest.builder()
            .id(1L)
            .build();

        UserDto expected = UserDto.builder()
            .id(1L)
            .username("test")
            .build();

        this.mockMvc.perform(
                get("/api/v1/users/id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(this.mapper.writeValueAsString(expected)));
    }

    @Test
    @Order(3)
    @DisplayName("Register user")
    public void testRegisterUser() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
            .username("test")
            .password("testing")
            .build();

        this.mockMvc.perform(
                post("/api/v1/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("Delete user by ID")
    public void testDeleteUserById() throws Exception {
        UserDeleteByIdRequest request = UserDeleteByIdRequest.builder()
            .id(1L)
            .build();

        this.mockMvc.perform(
                delete("/api/v1/users/id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @DisplayName("Login user")
    public void testLoginUser() throws Exception {
        when(this.userService.loginUser("test", "testing")).thenReturn("token");

        UserLoginRequest request = UserLoginRequest.builder()
            .username("test")
            .password("testing")
            .build();

        this.mockMvc.perform(
                post("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(cookie().value("spring_kube_infra_login_token", "token"));
    }
}
