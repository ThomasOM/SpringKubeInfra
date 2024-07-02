package me.thomazz.userservice.test.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.thomazz.userservice.service.UserJwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class UserJwtServiceTests {
    private final Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
    private final String secret = "EOwOOG2hds94sChfQqm92yQlahx02KOPPbVEw4SQuLY=";
    private final UserJwtService userService = new UserJwtService(
        this.secret, "PT15M", Clock.fixed(Instant.now(), ZoneOffset.UTC)
    );

    @Test
    @DisplayName("Token generation")
    public void testGenerateToken() {
        String token = this.userService.generateToken(1L);

        JwtParser parser = Jwts.parserBuilder()
            .setClock(() -> Date.from(this.clock.instant()))
            .setSigningKey(Keys.hmacShaKeyFor(this.secret.getBytes()))
            .build();

        Jws<Claims> jws = parser.parseClaimsJws(token);

        assertThat(jws.getBody().get("id")).isEqualTo(1); // Long signature is lost during compacting
    }
}
