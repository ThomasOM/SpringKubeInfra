package me.thomazz.userservice.entities;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String username;
    private String password;
}
