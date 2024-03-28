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

@SuppressWarnings("MultipleStringLiterals")
public class StackOverflowClient implements ExternalServiceClient {
    private final RestClient restClient;
    private final RestClient.ResponseSpec.ErrorHandler notOkResponseHandler;
    private static final String DEFAULT_BASE_URL =
        "https://api.stackexchange.com/2.3/questions/";
    private static final String SUPPOERTED_PREFIX = "https://stackoverflow";
    private static final String DB_SERVICE_NAME = "stackoverflow";
    private static final Pattern LAST_ACTIVITY_DATE_SEARCH_PATTERN =
        Pattern.compile("\"last_activity_date\":\\s*([0-9]+)");
    private static final Pattern RETRIEVE_QUESTION_NUMBER_FROM_URL = Pattern.compile(
        "https://stackoverflow\\.com/questions/([0-9]+)/.*"
    );

    // check difference
    private static final Pattern IS_ANSWERED_PATTERN = Pattern.compile(".*\\\"is_answered\\\":\\s*(true|false),.*");
    private static final Pattern ANSWER_COUNT_PATTERN = Pattern.compile(".*\\\"answer_count\\\":\\s*(\\d+),.*");

    public StackOverflowClient(RestClient.ResponseSpec.ErrorHandler notOkResponseHandler) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new ReactorNettyClientRequestFactory();

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(StackOverflowClient.DEFAULT_BASE_URL)
            .build();
        this.notOkResponseHandler = notOkResponseHandler;
    }

    public StackOverflowClient(String baseUrl, RestClient.ResponseSpec.ErrorHandler notOkResponseHandler) {
        RestClient.Builder restClientBuilder = RestClient.builder();

        var requestFactory = new ReactorNettyClientRequestFactory();

        this.restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(baseUrl)
            .build();
        this.notOkResponseHandler = notOkResponseHandler;
    }

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

    @Override
    public String getBodyJSONContent(String url) {
        Matcher matcher = RETRIEVE_QUESTION_NUMBER_FROM_URL.matcher(url);

        if (matcher.find()) {
            Integer questionId = Integer.valueOf(matcher.group(1));
            return this.restClient
                .get()
                .uri("/%s?site=stackoverflow&filter=withbody".formatted(questionId))
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
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

    private static String retrieveLastActivityDateField(String source)
        throws FieldNotFoundException, EmptyResponseBodyException {
        if (source == null) {
            throw new EmptyResponseBodyException("Body has no content.");
        }

        Matcher matcher = LAST_ACTIVITY_DATE_SEARCH_PATTERN.matcher(source);

        if (!matcher.find()) {
            throw new FieldNotFoundException("No match found for 'last_activity_date' field.");
        }

        return matcher.group(1);
    }
}
