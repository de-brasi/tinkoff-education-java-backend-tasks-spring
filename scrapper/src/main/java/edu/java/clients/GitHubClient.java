package edu.java.clients;

import edu.java.entities.UpdateResponse;
import edu.java.exceptions.EmptyResponseBodyException;
import edu.java.exceptions.FieldNotFoundException;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.client.RestClient;

public class GitHubClient {

    private final RestClient restClient;

    private final static String DEFAULT_BASE_URL = "https://api.github.com/repos/";


    public GitHubClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(GitHubClient.DEFAULT_BASE_URL).build();
    }

    public GitHubClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public UpdateResponse fetchUpdate(String owner, String repo)
        throws EmptyResponseBodyException, FieldNotFoundException {
        String responseBody = this.restClient
            .get()
            .uri("/{owner}/{repo}", owner, repo)
            .retrieve()
            .body(String.class);

        Pattern dateSearchPattern = Pattern.compile(
            "\"updated_at\": *\"([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)\""
        );

        if (responseBody == null) {
            throw new EmptyResponseBodyException("Body has no content.");
        }

        Matcher matcher = dateSearchPattern.matcher(responseBody);

        if (!matcher.find()) {
            throw new FieldNotFoundException("No match found for 'updated_at' field.");
        }

        String updTimeString = matcher.group(1);
        return new UpdateResponse(OffsetDateTime.parse(updTimeString));
    }
}
