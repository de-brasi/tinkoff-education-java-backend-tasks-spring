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
    AccessType databaseAccessType,

    @NotNull
    @Bean("kafkaTopic")
    ScrapperTopic topic,

    boolean useQueue
) {
    public record Scheduler(boolean enable, @NotNull Duration interval, @NotNull Duration forceCheckDelay) {
    }

    public record ScrapperTopic(String name, int partitionsCount, int replicasCount) {
    }

    public enum AccessType {
        JDBC,
        JPA,
    }
}
