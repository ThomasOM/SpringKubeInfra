package me.thomazz.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@RefreshScope
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter {
    private JwtParser jwtParser;

    @Autowired
    public AuthenticationFilter(
        @Value("${jwt.secret}") String secret
    ) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        if (this.isSecured(request)) {
            if (!request.getHeaders().containsKey("Authorization")) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            List<String> authorization = request.getHeaders().getOrEmpty("Authorization");
            String accessToken = authorization.get(0);

            if (this.isExpired(accessToken)) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }

        return chain.filter(exchange);
    }

    private boolean isSecured(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return !path.contains("/users/register") && !path.contains("/users/login");
    }

    public boolean isExpired(String accessToken) {
        try {
            Claims accessClaims = this.jwtParser.parseClaimsJws(accessToken).getBody();
            Date now = Date.from(Instant.now());
            return accessClaims.getExpiration().before(now);
        } catch (JwtException ignored) {
            return true;
        }
    }
}
