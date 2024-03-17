package edu.java.clients;

import edu.java.clients.entities.UpdateResponse;
import edu.java.clients.exceptions.EmptyResponseBodyException;
import edu.java.clients.exceptions.FieldNotFoundException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ReactorNettyClientRequestFactory;
import org.springframework.web.client.RestClient;

public class StackOverflowClient {
    private final RestClient restClient;

    private final static String DEFAULT_BASE_URL =
        "https://api.stackexchange.com/2.3/questions/";

    private static final Pattern LAST_ACTIVITY_DATE_SEARCH_PATTERN =
        Pattern.compile("\"last_activity_date\":\\s*([0-9]+)");
    private static final Pattern RETRIEVE_QUESTION_NUMBER_FROM_URL = Pattern.compile(
        "https://stackoverflow\\.com/questions/([0-9]+)/.*"
    );

    public StackOverflowClient() {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new ReactorNettyClientRequestFactory();

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(StackOverflowClient.DEFAULT_BASE_URL)
            .build();
    }

    public StackOverflowClient(String baseUrl) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new ReactorNettyClientRequestFactory();

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(baseUrl)
            .build();
    }

    public StackOverflowClient(int timeoutInMilliseconds) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new ReactorNettyClientRequestFactory();
        requestFactory.setReadTimeout(timeoutInMilliseconds);

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(StackOverflowClient.DEFAULT_BASE_URL)
            .build();
    }

    public StackOverflowClient(String baseUrl, int timeoutInMilliseconds) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new ReactorNettyClientRequestFactory();
        requestFactory.setReadTimeout(timeoutInMilliseconds);

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(baseUrl)
            .build();
    }

    public UpdateResponse fetchUpdate(Integer questionId) throws EmptyResponseBodyException, FieldNotFoundException {
        String responseBody = this.restClient
            .get()
            .uri("/%s?site=stackoverflow&filter=withbody".formatted(questionId))
            .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
            .retrieve()
            .body(String.class);

        String updateTimeString = retrieveLastActivityDateField(responseBody);
        long updateTimeUnixEpoch = Long.parseLong(updateTimeString);
        OffsetDateTime updDate = Instant.ofEpochSecond(updateTimeUnixEpoch).atOffset(ZoneOffset.UTC);

        return new UpdateResponse(updDate);
    }

    public UpdateResponse fetchUpdate(String url) throws EmptyResponseBodyException, FieldNotFoundException {
        Matcher matcher = RETRIEVE_QUESTION_NUMBER_FROM_URL.matcher(url);

        if (matcher.find()) {
            Integer questionId = Integer.valueOf(matcher.group(1));
            return fetchUpdate(questionId);
        } else {
            throw new RuntimeException("Incorrect URL %s; Can't parse it via existing regexp pattern!"
                .formatted(url));
        }
    }

    public String getDefaultBaseUrl() {
        return DEFAULT_BASE_URL;
    }

    private static String retrieveLastActivityDateField(String source)
        throws FieldNotFoundException, EmptyResponseBodyException {
        if (source == null) {
            throw new EmptyResponseBodyException("Body has no content.");
        }

        Matcher matcher = LAST_ACTIVITY_DATE_SEARCH_PATTERN.matcher(source);

        if (!matcher.find()) {
            throw new FieldNotFoundException("No match found for 'updated_at' field.");
        }

        return matcher.group(1);
    }
}
