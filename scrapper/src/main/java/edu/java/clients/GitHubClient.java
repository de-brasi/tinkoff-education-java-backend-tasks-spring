package edu.java.clients;

import edu.java.clients.entities.UpdateResponse;
import edu.java.clients.exceptions.EmptyResponseBodyException;
import edu.java.clients.exceptions.FieldNotFoundException;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@SuppressWarnings("MultipleStringLiterals")
public class GitHubClient implements ExternalServiceClient {

    private final RestClient restClient;
    private final RestClient.ResponseSpec.ErrorHandler notOkResponseHandler;
    private static final String DEFAULT_BASE_URL = "https://api.github.com/repos/";
    private static final String SUPPOERTED_PREFIX = "https://github";
    private static final String DB_SERVICE_NAME = "github";
    private static final Pattern PUSHED_AT_SEARCH_PATTERN = Pattern.compile(
        "\"pushed_at\": *\"([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)\""
    );
    private static final Pattern RETRIEVE_GH_NAME_AND_REPO_NAME_FROM_URL = Pattern.compile(
        "https://github\\.com/([^/\\s]+)/([^/\\s]+)/?.*"
    );


    public GitHubClient(RestClient.ResponseSpec.ErrorHandler notOkResponseHandler) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        this.restClient = restClientBuilder
            .baseUrl(GitHubClient.DEFAULT_BASE_URL)
            .build();
        this.notOkResponseHandler = notOkResponseHandler;
    }

    public GitHubClient(String baseUrl, RestClient.ResponseSpec.ErrorHandler notOkResponseHandler) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        this.restClient = restClientBuilder
            .baseUrl(baseUrl)
            .build();
        this.notOkResponseHandler = notOkResponseHandler;
    }

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

    public UpdateResponse fetchUpdate(String owner, String repo)
        throws EmptyResponseBodyException, FieldNotFoundException {
        String responseBody = this.restClient
            .get()
            .uri("/{owner}/{repo}", owner, repo)
            .retrieve()
            .body(String.class);

        String updTimeString = retrievePushedAtField(responseBody);
        return new UpdateResponse(OffsetDateTime.parse(updTimeString));
    }

    public UpdateResponse fetchUpdate(String url)
        throws EmptyResponseBodyException, FieldNotFoundException {

        Matcher matcher = RETRIEVE_GH_NAME_AND_REPO_NAME_FROM_URL.matcher(url);

        if (matcher.find()) {
            String username = matcher.group(1);
            String repoName = matcher.group(2);

            return fetchUpdate(username, repoName);
        } else {
            throw new RuntimeException("Incorrect URL %s; Can't parse it via existing regexp pattern!"
                .formatted(url));
        }
    }

    @Override
    public String getBodyJSONContent(String url) {
        Matcher matcher = RETRIEVE_GH_NAME_AND_REPO_NAME_FROM_URL.matcher(url);

        if (matcher.find()) {
            String username = matcher.group(1);
            String repoName = matcher.group(2);

            return this.restClient
                .get()
                .uri("/{owner}/{repo}", username, repoName)
                .retrieve()
                .body(String.class);
        } else {
            throw new RuntimeException("Incorrect URL %s; Can't parse it via existing regexp pattern!"
                .formatted(url));
        }
    }

    @Override
    public String getServiceNameInDatabase() {
        return DB_SERVICE_NAME;
    }

    @Override
    public String getChangeDescriptionFromResponseBodies(String jsonStringBodyBefore, String jsonStringBodyAfter) {
        return "Some updates!";
    }

    public boolean checkURLSupportedByService(String url) {
        return url.startsWith(SUPPOERTED_PREFIX);
    }


    private static String retrievePushedAtField(String source)
        throws FieldNotFoundException, EmptyResponseBodyException {
        if (source == null) {
            throw new EmptyResponseBodyException("Body has no content.");
        }

        Matcher matcher = PUSHED_AT_SEARCH_PATTERN.matcher(source);

        if (!matcher.find()) {
            throw new FieldNotFoundException("No match found for 'updated_at' field.");
        }

        return matcher.group(1);
    }
}
