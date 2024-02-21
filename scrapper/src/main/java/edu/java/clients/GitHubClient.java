package edu.java.clients;

import edu.java.entities.UpdateResponse;
import org.springframework.web.client.RestClient;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubClient {

    private final RestClient restClient;

    private final static String defaultBaseUrl = "https://api.github.com/repos/";


    public GitHubClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(GitHubClient.defaultBaseUrl).build();
    }

    public GitHubClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public UpdateResponse fetchUpdate(String owner, String repo) {
        String responseBody = this.restClient
            .get()
            .uri("/{owner}/{repo}", owner, repo)
            .retrieve()
            .body(String.class);

        Pattern dateSearchPattern = Pattern.compile(
            "\"updated_at\": *\"([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)\""
        );
        assert responseBody != null;
        Matcher matcher = dateSearchPattern.matcher(responseBody);

        if (!matcher.find()) {
            throw new IllegalStateException("No match found for last activity date");
        }

        String updTimeString = matcher.group(1);
        return new UpdateResponse(OffsetDateTime.parse(updTimeString));
    }
}
