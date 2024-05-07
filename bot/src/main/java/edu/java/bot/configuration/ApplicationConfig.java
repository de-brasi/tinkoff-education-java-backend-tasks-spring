package edu.java.bot.configuration;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotEmpty
    String telegramToken,

    @Bean("kafkaSettings")
    KafkaSettings kafkaSettings
) {
    public record KafkaSettings(
        boolean enabled,
        String consumerGroupId,

        @NestedConfigurationProperty
        KafkaTopicsConfig topics
    ) {
    }

    public record KafkaTopicsConfig(
        @NestedConfigurationProperty
        KafkaTopicConfig scrapperTopic
    ) {
    }

    public record KafkaTopicConfig(boolean enabled, String name, int partitionsCount, int replicasCount) {
    }
}
