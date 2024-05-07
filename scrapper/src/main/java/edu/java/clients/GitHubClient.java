package edu.java.clients;

import edu.java.api.util.Parser;
import edu.java.clients.entities.UpdateResponse;
import edu.java.clients.exceptions.EmptyResponseBodyException;
import edu.java.clients.exceptions.FieldNotFoundException;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@SuppressWarnings("MultipleStringLiterals")
public class GitHubClient implements ExternalServiceClient {

    private final RestClient restClient;
    private final RestClient.ResponseSpec.ErrorHandler notOkResponseHandler;
    private static final String DEFAULT_BASE_URL = "https://api.github.com/";
    private static final String SUPPOERTED_PREFIX = "https://github";
    private static final String SERVICE_NAME_IN_DATABASE = "github";
    private final Parser parser = Parser.builder()
        .field("update",
            "\"pushed_at\": *\"([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)\"")
        .field("owner",
            "https://github\\.com/([^/\\s]+)/[^/\\s]+/?.*")
        .field("repo",
            "https://github\\.com/[^/\\s]+/([^/\\s]+)/?.*")
        .build();

    public GitHubClient(int timeoutInMilliseconds, RestClient.ResponseSpec.ErrorHandler notOkResponseHandler) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(timeoutInMilliseconds);

        this.restClient = restClientBuilder
            .baseUrl(GitHubClient.DEFAULT_BASE_URL)
            .requestFactory(requestFactory)
            .build();
        this.notOkResponseHandler = notOkResponseHandler;
    }

    public GitHubClient(
        String baseUrl,
        int timeoutInMilliseconds,
        RestClient.ResponseSpec.ErrorHandler notOkResponseHandler
    ) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(timeoutInMilliseconds);

        this.restClient = restClientBuilder
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
        this.notOkResponseHandler = notOkResponseHandler;
    }

    public UpdateResponse fetchUpdate(String url) throws FieldNotFoundException, EmptyResponseBodyException {
        String ownerName = parser.retrieveValueOfField("owner", url);
        String repoName = parser.retrieveValueOfField("repo", url);

        if (ownerName != null && repoName != null) {
            return fetchUpdate(ownerName, repoName);
        } else {
            throw new RuntimeException("Incorrect URL %s; Can't parse it via existing regexp pattern!"
                .formatted(url));
        }
    }

    public UpdateResponse fetchUpdate(String owner, String repo)
        throws FieldNotFoundException, EmptyResponseBodyException {
        String responseBody = this.restClient
            .get()
            .uri("/repos/{owner}/{repo}", owner, repo)
            .retrieve()
            .onStatus(HttpStatusCode::is1xxInformational, notOkResponseHandler)
            .onStatus(HttpStatusCode::is3xxRedirection, notOkResponseHandler)
            .onStatus(HttpStatusCode::is4xxClientError, notOkResponseHandler)
            .onStatus(HttpStatusCode::is5xxServerError, notOkResponseHandler)
            .body(String.class);

        if (responseBody == null) {
            throw new EmptyResponseBodyException("Body is null");
        }

        String updTimeString = parser.retrieveValueOfField("update", responseBody);

        if (updTimeString == null) {
            throw new FieldNotFoundException("No match found for 'updated_at' field in response body '%s'".formatted(
                responseBody));
        }

        return new UpdateResponse(OffsetDateTime.parse(updTimeString));
    }

    @Override
    public String getBodyJSONContent(String url) {
        String ownerName = parser.retrieveValueOfField("owner", url);
        String repoName = parser.retrieveValueOfField("repo", url);

        if (ownerName != null && repoName != null) {
            return this.restClient
                .get()
                .uri("/repos/{owner}/{repo}", ownerName, repoName)
                .retrieve()
                .onStatus(HttpStatusCode::is1xxInformational, notOkResponseHandler)
                .onStatus(HttpStatusCode::is3xxRedirection, notOkResponseHandler)
                .onStatus(HttpStatusCode::is4xxClientError, notOkResponseHandler)
                .onStatus(HttpStatusCode::is5xxServerError, notOkResponseHandler)
                .body(String.class);
        } else {
            throw new RuntimeException("Incorrect URL %s; Can't parse it via existing regexp pattern!"
                .formatted(url));
        }
    }

    @Override
    public String getServiceNameInDatabase() {
        return SERVICE_NAME_IN_DATABASE;
    }

    @Override
    public String getChangeDescriptionFromResponseBodies(String jsonStringBodyBefore, String jsonStringBodyAfter) {
        return "Some updates!";
    }

    public boolean checkURLSupportedByService(String url) {
        return url.startsWith(SUPPOERTED_PREFIX);
    }
}
