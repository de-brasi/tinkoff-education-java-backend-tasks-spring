package edu.java.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.dtos.ApiErrorResponse;
import edu.common.dtos.LinkUpdateRequest;
import edu.common.exceptions.IncorrectRequestException;
import edu.common.exceptions.UnexpectedResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import java.util.List;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class BotClient {

    private final RestClient restClient;

    // TODO: получить порт приложения Bot из его файла конфигурации
    private final static String DEFAULT_BASE_URL = "http://localhost:8090/bot/api";

    private final static String ENDPOINT_UPDATES = "/updates";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient.ResponseSpec.ErrorHandler ENDPOINT_UPDATES_STATUS_1xx_HANDLER = (req, resp) -> {
        ApiErrorResponse errorResponse = objectMapper.readValue(
            new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
        );
        throw new UnexpectedResponse(
            resp.getStatusCode().value(),
            errorResponse.getExceptionMessage()
        );
    };

    private final RestClient.ResponseSpec.ErrorHandler ENDPOINT_UPDATES_STATUS_3xx_HANDLER = (req, resp) -> {
        ApiErrorResponse errorResponse = objectMapper.readValue(
            new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
        );
        throw new UnexpectedResponse(
            resp.getStatusCode().value(),
            errorResponse.getExceptionMessage()
        );
    };

    private final RestClient.ResponseSpec.ErrorHandler ENDPOINT_UPDATES_STATUS_4xx_HANDLER = (req, resp) -> {
        ApiErrorResponse errorResponse = objectMapper.readValue(
            new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
        );
        if (resp.getStatusCode().value() == 400) {
            throw new IncorrectRequestException(errorResponse.getExceptionMessage());
        } else {
            throw new UnexpectedResponse(resp.getStatusCode().value(), errorResponse.getExceptionMessage());
        }
    };

    private final RestClient.ResponseSpec.ErrorHandler ENDPOINT_UPDATES_STATUS_5xx_HANDLER = (req, resp) -> {
        ApiErrorResponse errorResponse = objectMapper.readValue(
            new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
        );
        throw new UnexpectedResponse(
            resp.getStatusCode().value(),
            errorResponse.getExceptionMessage()
        );
    };

    public BotClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(BotClient.DEFAULT_BASE_URL).build();
    }

    public BotClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
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
            .onStatus(HttpStatusCode::is1xxInformational, ENDPOINT_UPDATES_STATUS_1xx_HANDLER)
            .onStatus(HttpStatusCode::is3xxRedirection, ENDPOINT_UPDATES_STATUS_3xx_HANDLER)
            .onStatus(HttpStatusCode::is4xxClientError, ENDPOINT_UPDATES_STATUS_4xx_HANDLER)
            .onStatus(HttpStatusCode::is5xxServerError, ENDPOINT_UPDATES_STATUS_5xx_HANDLER);
    }

}
