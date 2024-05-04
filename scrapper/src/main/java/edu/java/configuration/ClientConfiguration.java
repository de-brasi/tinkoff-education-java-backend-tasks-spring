package edu.java.configuration;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {
    private final WebClientProperties gitHubClientSettings;
    private final WebClientProperties stackOverflowClientSettings;

    public ClientConfiguration(
        @Qualifier("githubProperties")
        WebClientProperties gitHubClientSettings,

        @Qualifier("stackoverflowProperties")
        WebClientProperties stackOverflowClientSettings
    ) {
        this.gitHubClientSettings = gitHubClientSettings;
        this.stackOverflowClientSettings = stackOverflowClientSettings;
    }

    @Bean("gitHubClient")
    public GitHubClient gitHubClient() {
        return new GitHubClient(
            gitHubClientSettings.getBaseUrl(),
            gitHubClientSettings.getTimeoutInMilliseconds()
        );
    }

    @Bean("stackOverflowClient")
    public StackOverflowClient stackoverflowClient() {
        return new StackOverflowClient(
            stackOverflowClientSettings.getBaseUrl(),
            stackOverflowClientSettings.getTimeoutInMilliseconds()
        );
    }
}
