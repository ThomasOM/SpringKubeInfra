package me.thomazz.userservice.service;

import lombok.RequiredArgsConstructor;
import me.thomazz.userservice.dto.UserDto;
import me.thomazz.userservice.exception.UserInvalidPasswordException;
import me.thomazz.userservice.exception.UserNotFoundException;
import me.thomazz.userservice.exception.UserPageSizeLimitExceededException;
import me.thomazz.userservice.exception.UsernameAlreadyExistsException;
import me.thomazz.userservice.entities.User;
import me.thomazz.userservice.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserJwtService jwtService;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final int pageSizeLimit;

    public List<UserDto> getAllUsers(Pageable pageable) {
        if (pageable.getPageSize() > this.pageSizeLimit) {
            throw new UserPageSizeLimitExceededException();
        }

        return this.repository.findAll(pageable).stream()
            .map(user -> this.modelMapper.map(user, UserDto.class))
            .toList();
    }

    public UserDto getUserById(Long id) {
        return this.repository.findById(id)
            .map(user -> this.modelMapper.map(user, UserDto.class))
            .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public void registerUser(String username, String password) {
        if (this.repository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }

        User user = User.builder()
            .username(username)
            .password(this.passwordEncoder.encode(password))
            .build();

        this.repository.save(user);
    }

    @Transactional
    public void deleteUser(long id) {
        if (this.repository.findById(id).isEmpty()) {
            throw new UserNotFoundException();
        }

        this.repository.deleteById(id);
    }

    public String loginUser(String username, String password) {
        User user = this.repository.findByUsername(username).orElseThrow(UserNotFoundException::new);

        if (!this.passwordEncoder.matches(password, user.getPassword())) {
            throw new UserInvalidPasswordException();
        }

        return this.jwtService.generateToken(user.getId());
    }
}
