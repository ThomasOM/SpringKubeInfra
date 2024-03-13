package me.thomazz.userservice.service;

import lombok.RequiredArgsConstructor;
import me.thomazz.userservice.entities.UserDto;
import me.thomazz.userservice.exception.UserEmailAlreadyExistsException;
import me.thomazz.userservice.exception.UserInvalidPasswordException;
import me.thomazz.userservice.exception.UserNotFoundException;
import me.thomazz.userservice.exception.UsernameAlreadyExistsException;
import me.thomazz.userservice.model.User;
import me.thomazz.userservice.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserJwtService jwtService;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public List<UserDto> getAllUsers() {
        return this.repository.findAll().stream()
            .map(user -> this.modelMapper.map(user, UserDto.class))
            .toList();
    }

    public UserDto getUserById(Long id) {
        return this.repository.findById(id)
            .map(user -> this.modelMapper.map(user, UserDto.class))
            .orElseThrow(UserNotFoundException::new);
    }

    public void registerUser(String username, String email, String password) {
        if (this.repository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }

        if (this.repository.findByEmail(username).isPresent()) {
            throw new UserEmailAlreadyExistsException();
        }

        User user = User.builder()
            .username(username)
            .password(this.passwordEncoder.encode(password))
            .build();

        this.repository.save(user);
    }

    public String loginUser(String username, String password) {
        User user = this.repository.findByUsername(username).orElseThrow(UserNotFoundException::new);

        if (!this.passwordEncoder.matches(password, user.getPassword())) {
            throw new UserInvalidPasswordException();
        }

        return this.jwtService.generateToken(user.getId().toString());
    }
}
