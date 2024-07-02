package me.thomazz.gatewayservice.configuration;

import me.thomazz.gatewayservice.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApiGatewayConfiguration {
    @Autowired
    private AuthenticationFilter filter;
    @Value("${service.user-service-uri}")
    private String userServiceUri;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder, ApiGatewayRoutePathConfigurationProperties routeProperties) {
        String[] secured = routeProperties.getSecured().toArray(String[]::new);

        return builder.routes()
            .route(
                "user-service",
                route -> route.path(secured)
                    .filters(filter -> filter.filter(this.filter))
                    .uri(this.userServiceUri)
            )
            .build();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
