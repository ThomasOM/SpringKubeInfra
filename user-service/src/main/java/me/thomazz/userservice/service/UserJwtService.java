package me.thomazz.userservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class UserJwtService {
    private final SecretKey key;
    private final Duration expiration;

    @Autowired
    public UserJwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") String expiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = Duration.parse(expiration);
    }

    public String generateToken(String userId) {
        return Jwts.builder()
            .setClaims(Map.of("id", userId))
            .setSubject(userId)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plus(this.expiration)))
            .signWith(this.key)
            .compact();
    }
}
