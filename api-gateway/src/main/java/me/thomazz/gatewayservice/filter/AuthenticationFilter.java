package me.thomazz.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.thomazz.gatewayservice.configuration.ApiGatewayRoutePathConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RefreshScope
@Component
public class AuthenticationFilter implements GatewayFilter {
    private final Clock clock;
    private final List<PathPattern> allowedPatterns;
    private JwtParser jwtParser;

    @Autowired
    public AuthenticationFilter(
        Clock clock,
        ApiGatewayRoutePathConfigurationProperties routeProperties,
        @Value("${jwt.secret}") String jwtSecret
    ) {
        this.clock = clock;

        PathPatternParser parser = new PathPatternParser();
        this.allowedPatterns = routeProperties.getAllowed().stream()
            .map(parser::parse)
            .collect(Collectors.toList());

        this.jwtParser = Jwts.parserBuilder()
            .setClock(() -> Date.from(this.clock.instant()))
            .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        if (this.isSecured(request)) {
            if (!request.getCookies().containsKey("spring_kube_infra_login_token")) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            HttpCookie cookie = request.getCookies().get("spring_kube_infra_login_token").get(0);
            String accessToken = cookie.getValue();

            if (this.isExpired(accessToken)) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }

        return chain.filter(exchange);
    }

    private boolean isSecured(ServerHttpRequest request) {
        PathContainer path = PathContainer.parsePath(request.getURI().getPath());
        return this.allowedPatterns.stream().noneMatch(pattern -> pattern.matches(path));
    }

    public boolean isExpired(String accessToken) {
        try {
            Claims accessClaims = this.jwtParser.parseClaimsJws(accessToken).getBody();
            Date now = Date.from(this.clock.instant());
            return accessClaims.getExpiration().before(now);
        } catch (JwtException ignored) {
            return true;
        }
    }
}
