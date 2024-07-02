package me.thomazz.gatewayservice.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "route.path")
@Getter
@AllArgsConstructor
public class ApiGatewayRoutePathConfigurationProperties {
    private List<String> secured;
    private List<String> allowed;
}
