package edu.java.configuration;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotNull
    @Bean("scheduler")
    Scheduler scheduler,

    @NotNull
    @Bean("githubClientSettings")
    ClientSettings githubClientSettings,

    @NotNull
    @Bean("stackoverflowClientSettings")
    ClientSettings stackoverflowClientSettings,

    AccessType databaseAccessType
) {
    public record Scheduler(boolean enable, @NotNull Duration interval, @NotNull Duration forceCheckDelay) {
    }

    public record ClientSettings(int timeoutInMilliseconds){
    }

    public enum AccessType {
        JDBC,
        JPA,
    }
}
