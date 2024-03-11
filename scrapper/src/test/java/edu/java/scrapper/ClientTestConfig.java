package edu.java.scrapper;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

public class ClientTestConfig {
    @Bean("testGitHubClient")
    public GitHubClient gitHubClient(@Autowired RestClient.Builder builder) {
        return new GitHubClient(builder, "http://localhost:8080/repos/");
    }

    @Bean("testStackOverflowClient")
    public StackOverflowClient stackoverflowClient(@Autowired RestClient.Builder builder) {
        return new StackOverflowClient(builder, "http://localhost:8080/questions/");
    }
}
