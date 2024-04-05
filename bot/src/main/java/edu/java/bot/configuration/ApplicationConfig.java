package edu.java.bot.configuration;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotEmpty
    String telegramToken,

    @Bean("scrapperTopic")
    ScrapperTopic scrapperTopic
) {
    record ScrapperTopic(String name) {}
}
