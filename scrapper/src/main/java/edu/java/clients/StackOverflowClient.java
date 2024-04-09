package edu.java.clients;

import edu.java.api.util.Parser;
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
    private static final String DEFAULT_BASE_URL =
        "https://api.stackexchange.com/2.3/questions/";
    private static final String SUPPOERTED_PREFIX = "https://stackoverflow";

    private final Parser parser = Parser.builder()
        .field(
            "last_activity_date",
            "\"last_activity_date\":\\s*([0-9]+)"
        )
        .field(
            "question",
            "https://stackoverflow\\.com/questions/([0-9]+)/.*"
        )
        .build();

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

    public UpdateResponse fetchUpdate(Integer questionId) throws FieldNotFoundException {
        String responseBody = this.restClient
            .get()
            .uri("/%s?site=stackoverflow&filter=withbody".formatted(questionId))
            .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
            .retrieve()
            .body(String.class);

        String updateTimeString = parser.retrieveValueOfField("last_activity_date", responseBody);

        if (updateTimeString == null) {
            throw new FieldNotFoundException("No match found for 'last_activity_date' field.");
        }

        long updateTimeUnixEpoch = Long.parseLong(updateTimeString);
        OffsetDateTime updDate = Instant.ofEpochSecond(updateTimeUnixEpoch).atOffset(ZoneOffset.UTC);

        return new UpdateResponse(updDate);
    }

    public UpdateResponse fetchUpdate(String url) throws EmptyResponseBodyException, FieldNotFoundException {
        String question = parser.retrieveValueOfField("question", url);

        if (question != null) {
            Integer questionId = Integer.valueOf(question);
            return fetchUpdate(questionId);
        } else {
            throw new RuntimeException("Incorrect URL %s; Can't parse it via existing regexp pattern!"
                .formatted(url));
        }
    }

    public boolean checkURLSupportedByService(String url) {
        return url.startsWith(SUPPOERTED_PREFIX);
    }
}
