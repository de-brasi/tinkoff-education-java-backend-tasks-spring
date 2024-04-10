package edu.java.configuration;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {
    private final GitHubClientSettings gitHubClientSettings;
    private final StackOverflowClientSettings stackOverflowClientSettings;

    public ClientConfiguration(
        GitHubClientSettings gitHubClientSettings,
        StackOverflowClientSettings stackOverflowClientSettings
    ) {
        this.gitHubClientSettings = gitHubClientSettings;
        this.stackOverflowClientSettings = stackOverflowClientSettings;
    }

    @Bean("gitHubClient")
    public GitHubClient gitHubClient() {
        return new GitHubClient(
            "https://api.github.com/repos/",
            gitHubClientSettings.getTimeoutInMilliseconds()
        );
    }

    @Bean("stackOverflowClient")
    public StackOverflowClient stackoverflowClient() {
        return new StackOverflowClient(stackOverflowClientSettings.getTimeoutInMilliseconds());
    }
}
