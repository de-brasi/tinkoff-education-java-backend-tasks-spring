package edu.java.configuration;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {
    @Bean("gitHubClient")
    public GitHubClient gitHubClient() {
        return new GitHubClient("https://api.github.com/repos/");
    }

    @Bean("stackOverflowClient")
    public StackOverflowClient stackoverflowClient() {
        return new StackOverflowClient();
    }
}
