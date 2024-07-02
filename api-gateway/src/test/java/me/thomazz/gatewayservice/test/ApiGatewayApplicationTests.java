package me.thomazz.gatewayservice.test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.thomazz.gatewayservice.ApiGatewayApplication;
import me.thomazz.gatewayservice.configuration.ApiGatewayConfiguration;
import me.thomazz.gatewayservice.test.configuration.ApiGatewayTestConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.security.Key;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@SpringBootTest(
    classes = { ApiGatewayApplication.class, ApiGatewayConfiguration.class, ApiGatewayTestConfiguration.class },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"service.user-service-uri=http://localhost:${wiremock.server.port}"}
)
@AutoConfigureWireMock(port = 0)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiGatewayApplicationTests {
    private final Key jwtKey;
    private final Duration jwtExpiration;
    private final Clock clock;
    private final WebTestClient webTestClient;

    @Autowired
    public ApiGatewayApplicationTests(
        @Value("${jwt.secret}") String jwtSecret,
        @Value("${jwt.expiration}") Duration jwtExpiration,
        Clock clock,
        WebTestClient webTestClient
    ) {
        this.jwtKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpiration = jwtExpiration;
        this.clock = clock;
        this.webTestClient = webTestClient;
    }

    @Test
    @Order(1)
    @DisplayName("Register route - Valid")
    public void testRegisterRouteValidReturnsOk() {
        stubFor(get(urlEqualTo("/api/v1/users/register")).willReturn(ok()));

        this.webTestClient.get()
            .uri("/api/v1/users/register")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    @Order(2)
    @DisplayName("Login route - Valid")
    public void testLoginRouteValidReturnsOkAndCookie() {
        stubFor(get(urlEqualTo("/api/v1/users/login"))
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
    @DisplayName("Protected route - Unauthorized")
    public void testProtectedRouteInvalidReturnsUnauthorized() {
        stubFor(get(urlEqualTo("/api/v1/users")).willReturn(ok()));

        long userId = 1L;
        String jwtToken = Jwts.builder()
            .setClaims(Map.of("id", userId))
            .setSubject(Long.toString(userId))
            .setIssuedAt(Date.from(this.clock.instant()))
            .setExpiration(Date.from(Instant.now(this.clock).plus(this.jwtExpiration)))
            .signWith(this.jwtKey)
            .compact();

        this.webTestClient.get()
            .uri("/api/v1/users")
            .cookie("spring_kube_infra_login_token", jwtToken)
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    @Order(4)
    @DisplayName("Protected route - Valid")
    public void testProtectedRouteValidReturnsOk() {
        stubFor(get(urlEqualTo("/api/v1/users")).willReturn(ok()));

        this.webTestClient.get()
            .uri("/api/v1/users")
            .exchange()
            .expectStatus().isUnauthorized();
    }
}
