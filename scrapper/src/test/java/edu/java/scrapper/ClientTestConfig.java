package edu.java.scrapper;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import org.springframework.context.annotation.Bean;

public class ClientTestConfig {
    @Bean("testGitHubClient")
    public GitHubClient gitHubClient() {
        // todo: timeout value from config
        return new GitHubClient("http://localhost:8080/repos/", 1000);
    }

    @Bean("testStackOverflowClient")
    public StackOverflowClient stackoverflowClient() {
        // todo: timeout value from config
        return new StackOverflowClient("http://localhost:8080/questions/");
    }
}
