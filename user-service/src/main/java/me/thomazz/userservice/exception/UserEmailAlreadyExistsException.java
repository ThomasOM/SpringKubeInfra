package me.thomazz.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Email already exists")
public class UserEmailAlreadyExistsException extends RuntimeException {
}
