package me.thomazz.userservice.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.thomazz.userservice.UserApplication;
import me.thomazz.userservice.dto.UserByIdRequest;
import me.thomazz.userservice.dto.UserDto;
import me.thomazz.userservice.dto.UserLoginRequest;
import me.thomazz.userservice.dto.UserRegisterRequest;
import me.thomazz.userservice.entities.User;
import me.thomazz.userservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = UserApplication.class)
@TestPropertySource(locations = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserApplicationIntegrationTests {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository repository;
    @Autowired private PasswordEncoder encoder;

    @Test
    @Order(1)
    @DisplayName("User registration - Valid")
    public void userRegister_givenValidRequest_shouldReturnOk() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder().username("test").password("testing").build();

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
    public void userLogin_givenInvalidRequest_shouldReturnConflict() throws Exception {
        this.repository.save(
            User.builder()
                .username("test")
                .password(this.encoder.encode("testing"))
                .build()
        );

        UserRegisterRequest request = UserRegisterRequest.builder().username("test").password("testing").build();

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
    public void userLogin_givenValidRequest_shouldReturnOkAndCookie() throws Exception {
        this.repository.save(
            User.builder()
                .username("test")
                .password(this.encoder.encode("testing"))
                .build()
        );

        UserLoginRequest request = UserLoginRequest.builder().username("test").password("testing").build();

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
    public void userLogin_givenInvalidRequest_shouldReturnUnauthorized() throws Exception {
        this.repository.save(
            User.builder()
                .username("test")
                .password(this.encoder.encode("testing"))
                .build()
        );

        UserLoginRequest request = UserLoginRequest.builder().username("test").password("1234").build();

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
    public void userLogin_givenInvalidRequest_shouldReturnNotFound() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder().username("test").password("testing").build();

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
    public void userGetAll_givenValidRequest_shouldReturnOk() throws Exception {
        this.mvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    @DisplayName("Get all users - Valid")
    public void userGetAll_givenValidRequest_shouldReturnOkAndUsers() throws Exception {
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

        this.mvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk())
            .andExpect(content().json(this.mapper.writeValueAsString(response)));
    }

    @Test
    @Order(8)
    @DisplayName("Get user by id - Valid")
    public void userGetById_givenValidRequest_shouldReturnOkAndUser() throws Exception {
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
    @Order(9)
    @DisplayName("Get user by id - Not found")
    public void userGetById_givenInvalidRequest_shouldReturnNotFound() throws Exception {
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
}
