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

public class StackOverflowClient  {
    private final RestClient restClient;

    private final static String defaultBaseUrl =
        "https://api.stackexchange.com/2.3/questions/";

    public StackOverflowClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.requestFactory(new ReactorNettyClientRequestFactory()).baseUrl(StackOverflowClient.defaultBaseUrl).build();
    }

    public StackOverflowClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient = restClientBuilder.requestFactory(new ReactorNettyClientRequestFactory()).baseUrl(baseUrl).build();
    }

    public String getAllInfo(String questionId) {
        return this.restClient
            .get()
            .uri("/%s?site=stackoverflow&filter=withbody".formatted(questionId))
            .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
            .retrieve()
            .body(String.class);
    }

    public String getAllInfo(Integer questionId) {
        return this.getAllInfo(Integer.toString(questionId));
    }

    public String debugGetLastActivityTimestampAsString(Integer questionId) {
        String responseBody = this.getAllInfo(Integer.toString(questionId));
        System.out.println(responseBody);

        Pattern dateSearchPattern = Pattern.compile("\"last_activity_date\":\\s*([0-9]+)");
        Matcher matcher = dateSearchPattern.matcher(responseBody);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("No match found for last activity date");
        }
    }

    public OffsetDateTime debugGetLastActivityTimestampAsDateTime(Integer questionId) {
        String timestampString = debugGetLastActivityTimestampAsString(questionId);
        long epoch = Long.parseLong(timestampString);

        Instant instant = Instant.ofEpochSecond(epoch);

        return instant.atOffset(ZoneOffset.UTC);
    }

    // TODO
    public UpdateResponse fetchUpdate() {
        return null;
    }
}
