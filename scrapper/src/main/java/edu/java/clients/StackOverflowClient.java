package edu.java.clients;

import edu.java.entities.UpdateResponse;
import edu.java.exceptions.EmptyResponseBodyException;
import edu.java.exceptions.FieldNotFoundException;
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

    public StackOverflowClient(RestClient.Builder restClientBuilder) {
        var requestFactory = new ReactorNettyClientRequestFactory();

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(StackOverflowClient.DEFAULT_BASE_URL)
            .build();
    }

    public StackOverflowClient(RestClient.Builder restClientBuilder, String baseUrl) {
        var requestFactory = new ReactorNettyClientRequestFactory();

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(baseUrl)
            .build();
    }

    public StackOverflowClient(RestClient.Builder restClientBuilder, int timeoutInMilliseconds) {
        var requestFactory = new ReactorNettyClientRequestFactory();
        requestFactory.setReadTimeout(timeoutInMilliseconds);

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(StackOverflowClient.DEFAULT_BASE_URL)
            .build();
    }

    public StackOverflowClient(RestClient.Builder restClientBuilder, String baseUrl, int timeoutInMilliseconds) {
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

        Pattern dateSearchPattern = Pattern.compile("\"last_activity_date\":\\s*([0-9]+)");

        if (responseBody == null) {
            throw new EmptyResponseBodyException("Body has no content.");
        }

        Matcher matcher = dateSearchPattern.matcher(responseBody);

        if (!matcher.find()) {
            throw new FieldNotFoundException("No match found for 'last_activity_date' field.");
        }

        String updateTimeString = matcher.group(1);
        long updateTimeUnixEpoch = Long.parseLong(updateTimeString);
        OffsetDateTime updDate = Instant.ofEpochSecond(updateTimeUnixEpoch).atOffset(ZoneOffset.UTC);

        return new UpdateResponse(updDate);
    }
}
