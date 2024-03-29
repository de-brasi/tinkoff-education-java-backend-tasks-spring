package edu.java.scrapper.clientstest;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import org.springframework.context.annotation.Bean;

public class ClientTestConfig {
    @Bean("testGitHubClient")
    public GitHubClient gitHubClient() {
        return new GitHubClient("http://localhost:8080/repos/", 1000);
    }

    @Bean("testStackOverflowClient")
    public StackOverflowClient stackoverflowClient() {
        return new StackOverflowClient("http://localhost:8080/questions/", 1000);
    }
}
