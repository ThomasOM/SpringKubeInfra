package me.thomazz.gatewayservice.test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.thomazz.gatewayservice.ApiGatewayApplication;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.security.Key;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

@SpringBootTest(classes = ApiGatewayApplication.class)
@AutoConfigureWireMock
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("WIP")
public class ApiGatewayApplicationTests {
    private final String port;
    private final Key jwtKey;
    private final Duration jwtExpiration;
    private final Clock clock;

    private WebTestClient webTestClient;

    public ApiGatewayApplicationTests(
        @Value("${wiremock.server.port}") String port,
        @Value("${jwt.secret}") String jwtSecret,
        @Value("${jwt.expiration}") Duration jwtExpiration
    ) {
        this.port = port;
        this.jwtKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpiration = jwtExpiration;
        this.clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
    }

    @BeforeEach
    public void setup() {
        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + this.port).build();
    }

    @Test
    @Order(1)
    @DisplayName("Unprotected route - Valid")
    public void testUnprotectedRouteValidReturnsOk() {
        stubFor(get("/test").willReturn(ok()));

        this.webTestClient.get()
            .uri("/test")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    @Order(2)
    @DisplayName("Login route - Valid")
    public void testLoginRouteValidReturnsOkAndCookie() {
        stubFor(get("/api/v1/users/login")
            .willReturn(
                aResponse()
                    .withHeader("Set-Cookie", "spring_kube_infra_login_token=token")
                    .withStatus(200)
            )
        );

        this.webTestClient.get()
            .uri("/api/v1/users/login")
            .exchange()
            .expectStatus().isOk()
            .expectCookie().value("spring_kube_infra_login_token", Matchers.equalTo("token"));
    }

    @Test
    @Order(3)
    @DisplayName("Protected route - Valid")
    public void test() {
        stubFor(get("/api/v1/users/id").willReturn(ok()));

        long userId = 1L;
        String jwtToken = Jwts.builder()
            .setClaims(Map.of("id", userId))
            .setSubject(Long.toString(userId))
            .setIssuedAt(Date.from(this.clock.instant()))
            .setExpiration(Date.from(Instant.now(this.clock).plus(this.jwtExpiration)))
            .signWith(this.jwtKey)
            .compact();

        this.webTestClient.get()
            .uri("/api/v1/users/id")
            .cookie("spring_kube_infra_login_tok", jwtToken)
            .exchange()
            .expectStatus().isOk();
    }
}
