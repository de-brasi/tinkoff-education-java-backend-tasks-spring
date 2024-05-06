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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ReactorNettyClientRequestFactory;
import org.springframework.web.client.RestClient;

@SuppressWarnings("MultipleStringLiterals")
public class StackOverflowClient implements ExternalServiceClient {
    private final RestClient restClient;
    private final RestClient.ResponseSpec.ErrorHandler notOkResponseHandler;
    private static final String DEFAULT_BASE_URL =
        "https://api.stackexchange.com/2.3/";
    private static final String SUPPOERTED_PREFIX = "https://stackoverflow";
    private static final String SERVICE_NAME_IN_DATABASE = "stackoverflow";

    private final Parser parser = Parser.builder()
        .field(
            "last_activity_date",
            "\"last_activity_date\":\\s*([0-9]+)"
        )
        .field(
            "question",
            "https://stackoverflow\\.com/questions/([0-9]+)/?.*"
        )
        .build();

    // check difference
    private static final Pattern IS_ANSWERED_PATTERN = Pattern.compile(".*\\\"is_answered\\\":\\s*(true|false),.*");
    private static final Pattern ANSWER_COUNT_PATTERN = Pattern.compile(".*\\\"answer_count\\\":\\s*(\\d+),.*");

    public StackOverflowClient(int timeoutInMilliseconds, RestClient.ResponseSpec.ErrorHandler notOkResponseHandler) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new ReactorNettyClientRequestFactory();
        requestFactory.setReadTimeout(timeoutInMilliseconds);

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(StackOverflowClient.DEFAULT_BASE_URL)
            .build();
        this.notOkResponseHandler = notOkResponseHandler;
    }

    public StackOverflowClient(
        String baseUrl,
        int timeoutInMilliseconds,
        RestClient.ResponseSpec.ErrorHandler notOkResponseHandler
    ) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new ReactorNettyClientRequestFactory();
        requestFactory.setReadTimeout(timeoutInMilliseconds);

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(baseUrl)
            .build();
        this.notOkResponseHandler = notOkResponseHandler;
    }

    public UpdateResponse fetchUpdate(Integer questionId) throws FieldNotFoundException {
        String responseBody = this.restClient
            .get()
            .uri("/questions/%s?site=stackoverflow&filter=withbody".formatted(questionId))
            .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
            .retrieve()
            .onStatus(HttpStatusCode::is1xxInformational, notOkResponseHandler)
            .onStatus(HttpStatusCode::is3xxRedirection, notOkResponseHandler)
            .onStatus(HttpStatusCode::is4xxClientError, notOkResponseHandler)
            .onStatus(HttpStatusCode::is5xxServerError, notOkResponseHandler)
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

    @Override
    public String getBodyJSONContent(String url) {
        String question = parser.retrieveValueOfField("question", url);

        if (question != null) {
            Integer questionId = Integer.valueOf(question);

            return this.restClient
                .get()
                .uri("/questions/%s?site=stackoverflow&filter=withbody".formatted(questionId))
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
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
        Matcher beforeJsonIsAnsweredMatcher = IS_ANSWERED_PATTERN.matcher(jsonStringBodyBefore);
        Matcher afterJsonIsAnsweredMatcher = IS_ANSWERED_PATTERN.matcher(jsonStringBodyAfter);

        if (
            beforeJsonIsAnsweredMatcher.find()
            && afterJsonIsAnsweredMatcher.find()
            && !beforeJsonIsAnsweredMatcher.group(1).equals(afterJsonIsAnsweredMatcher.group(1))
        ) {
            return "Question answered!";
        }

        Matcher beforeJsonAnswersCountMatcher = ANSWER_COUNT_PATTERN.matcher(jsonStringBodyBefore);
        Matcher afterJsonAnswersCountMatcher = ANSWER_COUNT_PATTERN.matcher(jsonStringBodyAfter);

        if (
            beforeJsonAnswersCountMatcher.find()
                && afterJsonAnswersCountMatcher.find()
                && !beforeJsonAnswersCountMatcher.group(1).equals(afterJsonAnswersCountMatcher.group(1))
        ) {
            return "New answer!";
        }

        return "Some updates!";
    }

    public boolean checkURLSupportedByService(String url) {
        return url.startsWith(SUPPOERTED_PREFIX);
    }
}
