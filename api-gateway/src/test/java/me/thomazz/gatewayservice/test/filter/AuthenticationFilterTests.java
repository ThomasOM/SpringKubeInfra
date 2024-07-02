package me.thomazz.gatewayservice.test.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.thomazz.gatewayservice.configuration.ApiGatewayRoutePathConfigurationProperties;
import me.thomazz.gatewayservice.filter.AuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class AuthenticationFilterTests {
    private final Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
    private final String jwtSecret = "EOwOOG2hds94sChfQqm92yQlahx02KOPPbVEw4SQuLY=";

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private GatewayFilterChain chain;

    private String jwtToken;
    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    public void setup() {
        long userId = 1L;
        this.jwtToken = Jwts.builder()
            .setClaims(Map.of("id", userId))
            .setSubject(Long.toString(userId))
            .setIssuedAt(Date.from(this.clock.instant()))
            .setExpiration(Date.from(Instant.now(this.clock).plus(Duration.ofMinutes(15L))))
            .signWith(Keys.hmacShaKeyFor(this.jwtSecret.getBytes()))
            .compact();

        this.authenticationFilter = new AuthenticationFilter(
            this.clock,
            new ApiGatewayRoutePathConfigurationProperties(Collections.emptyList(), List.of("/test1")),
            this.jwtSecret
        );

        when(this.exchange.getRequest()).thenReturn(this.request);
        when(this.exchange.getResponse()).thenReturn(this.response);
    }

    @Test
    @Order(1)
    @DisplayName("Secured by filter - Valid")
    public void testCoveredByAuthenticationFilterValid() {
        MultiValueMap<String, HttpCookie> cookieMap = new LinkedMultiValueMap<>();
        cookieMap.put("spring_kube_infra_login_token", List.of(new HttpCookie("spring_kube_infra_login_token", this.jwtToken)));

        when(this.request.getURI()).thenReturn(URI.create("/test2"));
        when(this.request.getCookies()).thenReturn(cookieMap);

        this.authenticationFilter.filter(this.exchange, this.chain);

        verify(this.response, never()).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(2)
    @DisplayName("Secured by filter - Invalid")
    public void testCoveredByAuthenticationFilterInvalid() {
        when(this.request.getURI()).thenReturn(URI.create("/test2"));
        when(this.request.getCookies()).thenReturn(new LinkedMultiValueMap<>());

        this.authenticationFilter.filter(this.exchange, this.chain);

        verify(this.response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(3)
    @DisplayName("Insecure - Valid")
    public void test() {
        when(this.request.getURI()).thenReturn(URI.create("/test1"));

        this.authenticationFilter.filter(this.exchange, this.chain);

        verify(this.response, never()).setStatusCode(HttpStatus.UNAUTHORIZED);
    }
}
