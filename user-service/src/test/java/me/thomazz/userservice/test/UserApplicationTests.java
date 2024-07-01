package me.thomazz.userservice.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.thomazz.userservice.UserApplication;
import me.thomazz.userservice.dto.UserByIdRequest;
import me.thomazz.userservice.dto.UserDeleteByIdRequest;
import me.thomazz.userservice.dto.UserDto;
import me.thomazz.userservice.dto.UserGetAllRequest;
import me.thomazz.userservice.dto.UserLoginRequest;
import me.thomazz.userservice.dto.UserRegisterRequest;
import me.thomazz.userservice.entities.User;
import me.thomazz.userservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = UserApplication.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserApplicationTests {
    private final MockMvc mvc;
    private final ObjectMapper mapper;
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final int pageSizeLimit;

    @Test
    @Order(1)
    @DisplayName("User registration - Valid")
    public void testRegisterUserValidReturnsOk() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
            .username("test")
            .password("testing")
            .build();

        this.mvc.perform(
                post("/api/v1/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("User registration - Conflict")
    public void testRegisterUserInvalidReturnsConflict() throws Exception {
        this.repository.save(
            User.builder()
                .username("test")
                .password(this.encoder.encode("testing"))
                .build()
        );

        UserRegisterRequest request = UserRegisterRequest.builder()
            .username("test")
            .password("testing")
            .build();

        this.mvc.perform(
                post("/api/v1/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("User login - Valid")
    public void testUserLoginValidReturnsOkAndCookie() throws Exception {
        this.repository.save(
            User.builder()
                .username("test")
                .password(this.encoder.encode("testing"))
                .build()
        );

        UserLoginRequest request = UserLoginRequest.builder()
            .username("test")
            .password("testing")
            .build();

        this.mvc.perform(
                post("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(cookie().exists("spring_kube_infra_login_token"));
    }

    @Test
    @Order(4)
    @DisplayName("User login - Unauthorized")
    public void testUserLoginInvalidReturnsUnauthorized() throws Exception {
        this.repository.save(
            User.builder()
                .username("test")
                .password(this.encoder.encode("testing"))
                .build()
        );

        UserLoginRequest request = UserLoginRequest.builder()
            .username("test")
            .password("1234")
            .build();

        this.mvc.perform(
                post("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @DisplayName("User login - Not found")
    public void testUserLoginInvalidReturnsNotFound() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
            .username("test")
            .password("testing")
            .build();

        this.mvc.perform(
                post("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    @DisplayName("Get all users - No users")
    public void testUserGetAllValidReturnsOkEmpty() throws Exception {
        UserGetAllRequest request = UserGetAllRequest.builder()
            .pageNumber(0)
            .pageSize(10)
            .build();

        this.mvc.perform(
                get("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    @DisplayName("Get all users - Page size limit exceeded")
    public void testUserGetAllInvalidReturnsBadRequest() throws Exception {
        UserGetAllRequest request = UserGetAllRequest.builder()
            .pageNumber(0)
            .pageSize(this.pageSizeLimit + 1)
            .build();

        this.mvc.perform(
                get("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    @DisplayName("Get all users - Valid")
    public void testUserGetAllValidReturnsOk() throws Exception {
        UserGetAllRequest request = UserGetAllRequest.builder()
            .pageNumber(0)
            .pageSize(1)
            .build();

        this.repository.save(
            User.builder()
                .id(1L)
                .username("test")
                .password(this.encoder.encode("testing"))
                .build()
        );

        List<UserDto> response = List.of(
            UserDto.builder()
                .id(1L)
                .username("test")
                .build()
        );

        this.mvc.perform(
                get("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(content().json(this.mapper.writeValueAsString(response)));
    }

    @Test
    @Order(9)
    @DisplayName("Get user by id - Valid")
    public void testUserGetByIdValidReturnsOk() throws Exception {
        this.repository.save(
            User.builder()
                .id(1L)
                .username("test")
                .password(this.encoder.encode("testing"))
                .build()
        );

        UserByIdRequest request = UserByIdRequest.builder()
            .id(1L)
            .build();

        UserDto response = UserDto.builder()
            .id(1L)
            .username("test")
            .build();

        this.mvc.perform(
                get("/api/v1/users/id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(content().json(this.mapper.writeValueAsString(response)));
    }

    @Test
    @Order(10)
    @DisplayName("Get user by id - Not found")
    public void testGetUserByIdInvalidReturnsNotFound() throws Exception {
        UserByIdRequest request = UserByIdRequest.builder()
            .id(1L)
            .build();

        this.mvc.perform(
                get("/api/v1/users/id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @DisplayName("Delete user by id - Valid")
    public void testUserDeleteByIdValidReturnsOk() throws Exception {
        this.repository.save(
            User.builder()
                .id(1L)
                .username("test")
                .password(this.encoder.encode("testing"))
                .build()
        );

        UserDeleteByIdRequest request = UserDeleteByIdRequest.builder()
            .id(1L)
            .build();

        this.mvc.perform(
                delete("/api/v1/users/id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    @DisplayName("Delete user by id - Not found")
    public void testDeleteUserByIdInvalidReturnsNotFound() throws Exception {
        UserDeleteByIdRequest request = UserDeleteByIdRequest.builder()
            .id(1L)
            .build();

        this.mvc.perform(
                delete("/api/v1/users/id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.mapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound());
    }
}
