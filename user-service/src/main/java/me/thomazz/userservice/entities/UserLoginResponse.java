package me.thomazz.userservice.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginResponse {
    private String accessToken;
}
