package edu.java.clients;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import java.util.List;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SuppressWarnings("MagicNumber")
public class BotClient {

    private final RestClient restClient;
    private final static String DEFAULT_BASE_URL = "http://localhost:8090/bot/api";
    private final static String ENDPOINT_UPDATES = "/updates";
    private final ResponseErrorHandler responseErrorHandler;

    public BotClient(ResponseErrorHandler errorHandler) {
        this.restClient = RestClient
            .builder()
            .baseUrl(BotClient.DEFAULT_BASE_URL)
            .build();

        this.responseErrorHandler = errorHandler;
    }

    public BotClient(String baseUrl, ResponseErrorHandler errorHandler) {
        this.restClient = RestClient
            .builder()
            .baseUrl(baseUrl)
            .build();

        this.responseErrorHandler = errorHandler;
    }

    public void sendUpdates(
        long id, String updatedUrl, String updateDescription, List<Long> subscribers
    ) {
        LinkUpdateRequest updates = new LinkUpdateRequest(id, updatedUrl, updateDescription, subscribers);
        this.restClient
            .post()
            .uri(BotClient.ENDPOINT_UPDATES)
            .contentType(APPLICATION_JSON)
            .body(updates)
            .retrieve()
            .onStatus(responseErrorHandler)
            .toBodilessEntity();
    }

}
