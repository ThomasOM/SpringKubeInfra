package me.thomazz.userservice.test.service;

import me.thomazz.userservice.config.UserConfiguration;
import me.thomazz.userservice.dto.UserDto;
import me.thomazz.userservice.entities.User;
import me.thomazz.userservice.exception.UserPageSizeLimitExceededException;
import me.thomazz.userservice.repository.UserRepository;
import me.thomazz.userservice.service.UserJwtService;
import me.thomazz.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserConfiguration.class)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class UserServiceTests {
    @Mock
    private UserJwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Autowired
    private int pageSizeLimit;

    private UserService userService;

    @BeforeEach
    public void setup() {
        this.userService = new UserService(
            this.jwtService,
            this.userRepository,
            this.passwordEncoder,
            new ModelMapper(),
            this.pageSizeLimit
        );
    }

    @Test
    @Order(1)
    @DisplayName("Get all users")
    public void testGetAllUsers() {
        when(this.passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        Pageable pageable = PageRequest.of(0, 2);

        User user1 = User.builder()
            .id(1L)
            .username("test1")
            .password(this.passwordEncoder.encode("testing1"))
            .build();

        User user2 = User.builder()
            .id(2L)
            .username("test2")
            .password(this.passwordEncoder.encode("testing2"))
            .build();

        when(this.userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user1, user2)));

        List<UserDto> allUsers = this.userService.getAllUsers(pageable);

        UserDto expected1 = UserDto.builder()
            .id(1L)
            .username("test1")
            .build();

        UserDto expected2 = UserDto.builder()
            .id(2L)
            .username("test2")
            .build();

        assertThat(allUsers).hasSameElementsAs(List.of(expected1, expected2));
    }

    @Test
    @Order(2)
    @DisplayName("Get all users page size limit exceeded")
    public void testGetAllUsersPageSizeLimitExceeded() {
        Pageable pageable = PageRequest.of(0, this.pageSizeLimit + 1);

        assertThatExceptionOfType(UserPageSizeLimitExceededException.class)
            .isThrownBy(() -> this.userService.getAllUsers(pageable));
    }

    @Test
    @Order(3)
    @DisplayName("Get user by Id")
    public void testGetUserById() {
        when(this.passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        User user = User.builder()
            .id(1L)
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        when(this.userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto userById = this.userService.getUserById(1L);

        UserDto expected = UserDto.builder()
            .id(1L)
            .username("test")
            .build();

        assertThat(userById).isEqualTo(expected);
    }

    @Test
    @Order(4)
    @DisplayName("Register user")
    public void testRegisterUser() {
        when(this.passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        this.userService.registerUser("test", "testing");

        User expected = User.builder()
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        verify(this.userRepository).save(expected);
    }

    @Test
    @Order(5)
    @DisplayName("Login user")
    public void testLoginUser() {
        when(this.passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        User user = User.builder()
            .id(1L)
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        when(this.userRepository.findByUsername("test")).thenReturn(Optional.of(user));
        when(this.jwtService.generateToken(1L)).thenReturn("token");
        when(this.passwordEncoder.matches("testing", "testing")).thenReturn(true);

        String token = this.userService.loginUser("test", "testing");

        assertThat(token).isEqualTo("token");
    }

    @Test
    @Order(6)
    @DisplayName("Delete user")
    public void testDeleteUser() {
        when(this.passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        User user = User.builder()
            .id(1L)
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        when(this.userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));

        this.userService.deleteUser(1L);

        verify(this.userRepository).deleteById(1L);
    }
}
