package me.thomazz.gatewayservice.test.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@TestConfiguration
public class ApiGatewayTestConfiguration {
    @Primary
    @Bean("testClock")
    public Clock clock() {
        return Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
    }
}
