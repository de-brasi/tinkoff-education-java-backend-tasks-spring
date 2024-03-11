package edu.java.configuration;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {
    @Bean("gitHubClient")
    public GitHubClient gitHubClient() {
        // todo: timeout value from config
        return new GitHubClient("https://api.github.com/repos/", 1000);
    }

    @Bean("stackOverflowClient")
    public StackOverflowClient stackoverflowClient() {
        // todo: timeout value from config
        return new StackOverflowClient(1000);
    }
}
