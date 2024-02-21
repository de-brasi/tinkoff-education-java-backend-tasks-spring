package edu.java.clients;

import edu.java.entities.UpdateResponse;
import org.springframework.web.client.RestClient;

public class GitHubClient {

    private final RestClient restClient;

    private final static String defaultBaseUrl = "https://api.github.com/repos/";


    public GitHubClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(GitHubClient.defaultBaseUrl).build();
    }

    public GitHubClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public String getAllInfo(String owner, String repo) {
        return this.restClient.get().uri("/{owner}/{repo}", owner, repo).retrieve().body(String.class);
    }

    // TODO
    public UpdateResponse fetchUpdate() {
        return new UpdateResponse("some data");
    }
}
