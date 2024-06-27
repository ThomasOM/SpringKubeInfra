package me.thomazz.gatewayservice.configuration;

import me.thomazz.gatewayservice.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfiguration {
    @Autowired
    private AuthenticationFilter filter;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route(
                "user-service",
                route -> route.path("api/v1/users/**")
                    .filters(filter -> filter.filter(this.filter))
                    .uri("http://user-service-svc")
            )
            .build();
    }
}
