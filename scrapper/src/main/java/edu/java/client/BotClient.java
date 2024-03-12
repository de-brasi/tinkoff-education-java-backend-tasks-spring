package edu.java.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.dtos.ApiErrorResponse;
import edu.common.dtos.LinkUpdateRequest;
import edu.common.exceptions.IncorrectRequestException;
import edu.common.exceptions.UnexpectedResponse;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SuppressWarnings("MagicNumber")
public class BotClient {

    private final RestClient restClient;

    private final static String DEFAULT_BASE_URL = "http://localhost:8090/bot/api";

    private final static String ENDPOINT_UPDATES = "/updates";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus1xxHandler = (req, resp) -> {
        ApiErrorResponse errorResponse = objectMapper.readValue(
            new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
        );
        throw new UnexpectedResponse(
            resp.getStatusCode().value(),
            errorResponse.getExceptionMessage()
        );
    };

    private final RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus3xxHandler = (req, resp) -> {
        ApiErrorResponse errorResponse = objectMapper.readValue(
            new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
        );
        throw new UnexpectedResponse(
            resp.getStatusCode().value(),
            errorResponse.getExceptionMessage()
        );
    };

    private final RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus4xxHandler = (req, resp) -> {
        ApiErrorResponse errorResponse = objectMapper.readValue(
            new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
        );
        if (resp.getStatusCode().value() == 400) {
            throw new IncorrectRequestException(errorResponse.getExceptionMessage());
        } else {
            throw new UnexpectedResponse(resp.getStatusCode().value(), errorResponse.getExceptionMessage());
        }
    };

    private final RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus5xxHandler = (req, resp) -> {
        ApiErrorResponse errorResponse = objectMapper.readValue(
            new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
        );
        throw new UnexpectedResponse(
            resp.getStatusCode().value(),
            errorResponse.getExceptionMessage()
        );
    };

    public BotClient() {
        this.restClient = RestClient
            .builder()
            .baseUrl(BotClient.DEFAULT_BASE_URL)
            .build();
    }

    public BotClient(String baseUrl) {
        this.restClient = RestClient
            .builder()
            .baseUrl(baseUrl)
            .build();
    }

    public void sendUpdates(
        int id, String updatedUrl, String updateDescription, List<Integer> subscribers
    ) {
        LinkUpdateRequest updates = new LinkUpdateRequest(id, updatedUrl, updateDescription, subscribers);
        this.restClient
            .post()
            .uri(BotClient.ENDPOINT_UPDATES)
            .contentType(APPLICATION_JSON)
            .body(updates)
            .retrieve()
            .onStatus(HttpStatusCode::is1xxInformational, endpointUpdatesStatus1xxHandler)
            .onStatus(HttpStatusCode::is3xxRedirection, endpointUpdatesStatus3xxHandler)
            .onStatus(HttpStatusCode::is4xxClientError, endpointUpdatesStatus4xxHandler)
            .onStatus(HttpStatusCode::is5xxServerError, endpointUpdatesStatus5xxHandler)
            .toBodilessEntity();
    }

}
