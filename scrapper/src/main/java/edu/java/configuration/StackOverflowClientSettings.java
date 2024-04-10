package edu.java.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.stackoverflow-client-settings", ignoreUnknownFields = false)
@Getter
@Setter
public class StackOverflowClientSettings {
    private int timeoutInMilliseconds;
}
