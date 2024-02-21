package edu.java.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {
    @Bean("gitHubClient")
    public GitHubClient gitHubClient(@Autowired RestClient.Builder builder) {
        return new GitHubClient(builder, "https://api.github.com/repos/");
    }

    @Bean("stackOverflowClient")
    public StackOverflowClient stackoverflowClient(@Autowired RestClient.Builder builder) {
        return new StackOverflowClient(builder);
    }
}
