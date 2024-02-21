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

    public String getAllInfo(String owner, String repo) {
        return this.restClient.get().uri("/{owner}/{repo}", owner, repo).retrieve().body(String.class);
    }

    public String debugGetLastActivityTimestampAsString(String owner, String repo) {
        String responseBody = this.getAllInfo(owner, repo);

        Pattern dateSearchPattern = Pattern.compile("\"updated_at\":\s*\"([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)\"");
        Matcher matcher = dateSearchPattern.matcher(responseBody);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("No match found for last activity date");
        }
    }

    public OffsetDateTime debugGetLastActivityTimestampAsDateTime(String owner, String repo) {
        String src = this.debugGetLastActivityTimestampAsString(owner, repo);
        return OffsetDateTime.parse(src);
    }

    // TODO
    public UpdateResponse fetchUpdate() {
        return new UpdateResponse("some data");
    }
}
