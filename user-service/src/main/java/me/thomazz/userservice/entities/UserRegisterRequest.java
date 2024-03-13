package me.thomazz.userservice.entities;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String username;
    private String password; // Plaintext
}
