package edu.java.configuration;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {
    @Bean("gitHubClient")
    public GitHubClient gitHubClient(
        @Value("#{@githubClientSettings.timeoutInMilliseconds()}") int timeoutInMilliseconds
    ) {
        return new GitHubClient("https://api.github.com/repos/", timeoutInMilliseconds);
    }

    @Bean("stackOverflowClient")
    public StackOverflowClient stackoverflowClient(
        @Value("#{@stackoverflowClientSettings.timeoutInMilliseconds()}") int timeoutInMilliseconds
    ) {
        return new StackOverflowClient(timeoutInMilliseconds);
    }
}
