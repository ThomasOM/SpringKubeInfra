package me.thomazz.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Page size of ${pageSizeLimit} exceeded!")
public class UserPageSizeLimitExceededException extends RuntimeException {
}
