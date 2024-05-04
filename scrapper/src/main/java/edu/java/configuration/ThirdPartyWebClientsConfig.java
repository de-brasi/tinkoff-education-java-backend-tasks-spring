package edu.java.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "third-party-web-clients", ignoreUnknownFields = false)
public record ThirdPartyWebClientsConfig(
    @NotNull
    @Bean
    WebClientProperties githubProperties,

    @NotNull
    @Bean
    WebClientProperties stackoverflowProperties
) {}
