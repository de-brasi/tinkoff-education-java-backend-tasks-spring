package edu.java.clients;

import edu.java.entities.UpdateResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ReactorNettyClientRequestFactory;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackOverflowClient {
    private final RestClient restClient;

    private final static String defaultBaseUrl =
        "https://api.stackexchange.com/2.3/questions/";

    public StackOverflowClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.requestFactory(new ReactorNettyClientRequestFactory())
            .baseUrl(StackOverflowClient.defaultBaseUrl).build();
    }

    public StackOverflowClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient =
            restClientBuilder.requestFactory(new ReactorNettyClientRequestFactory()).baseUrl(baseUrl).build();
    }

    public UpdateResponse fetchUpdate(Integer questionId) {
        String responseBody = this.restClient
            .get()
            .uri("/%s?site=stackoverflow&filter=withbody".formatted(questionId))
            .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
            .retrieve()
            .body(String.class);

        Pattern dateSearchPattern = Pattern.compile("\"last_activity_date\":\\s*([0-9]+)");
        assert responseBody != null;
        Matcher matcher = dateSearchPattern.matcher(responseBody);

        if (!matcher.find()) {
            throw new IllegalStateException("No match found for last activity date");
        }

        String updateTimeString = matcher.group(1);
        long updateTimeUnixEpoch = Long.parseLong(updateTimeString);
        OffsetDateTime updDate = Instant.ofEpochSecond(updateTimeUnixEpoch).atOffset(ZoneOffset.UTC);

        return new UpdateResponse(updDate);
    }
}
