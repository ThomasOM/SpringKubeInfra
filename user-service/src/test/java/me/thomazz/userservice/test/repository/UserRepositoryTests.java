package me.thomazz.userservice.test.repository;

import lombok.RequiredArgsConstructor;
import me.thomazz.userservice.entities.User;
import me.thomazz.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserRepositoryTests {
    private final UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        when(this.passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
    }

    @Test
    @Order(1)
    @DisplayName("Save user")
    public void testUserRepositorySave() {
        User user = User.builder()
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        User saved = this.userRepository.save(user);

        User expected = User.builder()
            .id(1L)
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        assertThat(saved).isEqualTo(expected);
    }

    @Test
    @Order(2)
    @DisplayName("Find all users")
    public void testUserRepositoryFindAll() {
        User user1 = User.builder()
            .username("test1")
            .password(this.passwordEncoder.encode("testing1"))
            .build();

        User user2 = User.builder()
            .username("test2")
            .password(this.passwordEncoder.encode("testing2"))
            .build();

        this.userRepository.saveAll(List.of(user1, user2));

        List<User> foundUsers = this.userRepository.findAll();

        User expected1 = User.builder()
            .id(1L)
            .username("test1")
            .password(this.passwordEncoder.encode("testing1"))
            .build();

        User expected2 = User.builder()
            .id(2L)
            .username("test2")
            .password(this.passwordEncoder.encode("testing2"))
            .build();

        assertThat(foundUsers).hasSameElementsAs(List.of(expected1, expected2));
    }

    @Test
    @Order(3)
    @DisplayName("Find user by username")
    public void testUserRepositoryFindByUsername() {
        User user = User.builder()
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        this.userRepository.save(user);

        User foundUser = this.userRepository.findByUsername("test").orElseThrow(NoSuchElementException::new);

        User expected = User.builder()
            .id(1L)
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        assertThat(foundUser).isEqualTo(expected);
    }

    @Test
    @Order(4)
    @DisplayName("Find user by id")
    public void testUserRepositoryFindById() {
        User user = User.builder()
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        this.userRepository.save(user);

        User foundUser = this.userRepository.findById(1L).orElseThrow(NoSuchElementException::new);

        User expected = User.builder()
            .id(1L)
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        assertThat(foundUser).isEqualTo(expected);
    }

    @Test
    @Order(5)
    @DisplayName("Delete user by id")
    public void testUserRepositoryDeleteById() {
        User user = User.builder()
            .username("test")
            .password(this.passwordEncoder.encode("testing"))
            .build();

        this.userRepository.save(user);
        this.userRepository.deleteById(1L);

        List<User> foundUsers = this.userRepository.findAll();
        assertThat(foundUsers).isEmpty();
    }
}
