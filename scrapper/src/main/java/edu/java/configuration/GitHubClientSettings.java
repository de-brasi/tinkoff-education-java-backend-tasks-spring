package edu.java.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.github-client-settings", ignoreUnknownFields = false)
@Getter
@Setter
public class GitHubClientSettings {
    private int timeoutInMilliseconds;
}


