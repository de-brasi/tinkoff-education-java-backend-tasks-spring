package edu.java.clients;

import edu.java.api.util.Parser;
import edu.java.clients.entities.UpdateResponse;
import edu.java.clients.exceptions.FieldNotFoundException;
import java.time.OffsetDateTime;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public class GitHubClient {

    private final RestClient restClient;
    private static final String DEFAULT_BASE_URL = "https://api.github.com/repos/";
    private static final String SUPPOERTED_PREFIX = "https://github";

    private final Parser parser = Parser.builder()
        .field(
            "update",
            "\"pushed_at\": *\"([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)\""
        )
        .field(
            "owner",
            "https://github\\.com/([^/\\s]+)/[^/\\s]+/?.*"
        )
        .field(
            "repo",
            "https://github\\.com/[^/\\s]+/([^/\\s]+)/?.*"
        )
        .build();


    public GitHubClient() {
        RestClient.Builder restClientBuilder = RestClient.builder();

        this.restClient = restClientBuilder
            .baseUrl(GitHubClient.DEFAULT_BASE_URL)
            .build();
    }

    public GitHubClient(String baseUrl) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        this.restClient = restClientBuilder
            .baseUrl(baseUrl)
            .build();
    }

    public GitHubClient(int timeoutInMilliseconds) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(timeoutInMilliseconds);

        this.restClient = restClientBuilder
            .baseUrl(GitHubClient.DEFAULT_BASE_URL)
            .requestFactory(requestFactory)
            .build();
    }

    public GitHubClient(String baseUrl, int timeoutInMilliseconds) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(timeoutInMilliseconds);

        this.restClient = restClientBuilder
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }

    public UpdateResponse fetchUpdate(String url) throws FieldNotFoundException {
        String ownerName = parser.retrieveValueOfField("owner", url);
        String repoName = parser.retrieveValueOfField("repo", url);

        if (ownerName != null && repoName != null) {
            return fetchUpdate(ownerName, repoName);
        } else {
            throw new RuntimeException("Incorrect URL %s; Can't parse it via existing regexp pattern!"
                .formatted(url));
        }
    }

    public UpdateResponse fetchUpdate(String owner, String repo) throws FieldNotFoundException {
        String responseBody = this.restClient
            .get()
            .uri("/{owner}/{repo}", owner, repo)
            .retrieve()
            .body(String.class);

        String updTimeString = parser.retrieveValueOfField("update", responseBody);

        if (updTimeString == null) {
            throw new FieldNotFoundException("No match found for 'updated_at' field in response body '%s'".formatted(
                responseBody));
        }

        return new UpdateResponse(OffsetDateTime.parse(updTimeString));
    }

    public boolean checkURLSupportedByService(String url) {
        return url.startsWith(SUPPOERTED_PREFIX);
    }
}
