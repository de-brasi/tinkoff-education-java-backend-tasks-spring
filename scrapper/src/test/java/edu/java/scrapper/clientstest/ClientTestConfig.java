package edu.java.scrapper.clientstest;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

public class ClientTestConfig {
    @Bean("testGitHubClient")
    public GitHubClient gitHubClient(
        @Autowired
        @Qualifier("notOkResponseHandler")
        RestClient.ResponseSpec.ErrorHandler notOkResponseHandler
    ) {
        return new GitHubClient("http://localhost:8080/repos/", 1000, notOkResponseHandler);
    }

    @Bean("testStackOverflowClient")
    public StackOverflowClient stackoverflowClient(
        @Autowired
        @Qualifier("notOkResponseHandler")
        RestClient.ResponseSpec.ErrorHandler notOkResponseHandler
    ) {
        return new StackOverflowClient("http://localhost:8080/questions/", 1000, notOkResponseHandler);
    }
}
