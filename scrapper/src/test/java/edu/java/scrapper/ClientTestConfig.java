package edu.java.scrapper;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

public class ClientTestConfig {
    @Bean("testGitHubClient")
    public GitHubClient gitHubClient() {
        RestClient.Builder builder = RestClient.builder();
        return new GitHubClient(builder, "http://localhost:8080/repos/");
    }

    @Bean("testStackOverflowClient")
    public StackOverflowClient stackoverflowClient() {
        RestClient.Builder builder = RestClient.builder();
        return new StackOverflowClient(builder, "http://localhost:8080/questions/");
    }
}
