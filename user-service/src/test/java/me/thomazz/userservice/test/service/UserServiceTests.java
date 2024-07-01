package me.thomazz.userservice.test.service;

import me.thomazz.userservice.dto.UserDto;
import me.thomazz.userservice.entities.User;
import me.thomazz.userservice.repository.UserRepository;
import me.thomazz.userservice.service.UserJwtService;
import me.thomazz.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @Mock
    private UserJwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setup() {
        when(this.passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
    }

    @Test
    @Order(1)
    @DisplayName("Get All Users")
    public void testGetAllUsers() {
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

        when(this.userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> allUsers = this.userService.getAllUsers();

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
    @DisplayName("Get User By Id")
    public void testGetUserById() {
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
    @Order(3)
    @DisplayName("Register User")
    public void testRegisterUser() {
        this.userService.registerUser("test", "testing");

        User expected = User.builder()
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        verify(this.userRepository).save(expected);
    }

    @Test
    @Order(4)
    @DisplayName("Login User")
    public void testLoginUser() {
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
    @Order(5)
    @DisplayName("Delete User")
    public void testDeleteUser() {
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
