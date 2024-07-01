package me.thomazz.userservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class UserJwtService {
    private final SecretKey key;
    private final Duration expiration;
    private final Clock clock;

    @Autowired
    public UserJwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") String expiration,
        Clock clock
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = Duration.parse(expiration);
        this.clock = clock;
    }

    public String generateToken(long userId) {
        return Jwts.builder()
            .setClaims(Map.of("id", userId))
            .setSubject(Long.toString(userId))
            .setIssuedAt(Date.from(this.clock.instant()))
            .setExpiration(Date.from(Instant.now(this.clock).plus(this.expiration)))
            .signWith(this.key)
            .compact();
    }
}
