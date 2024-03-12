package edu.java.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.dtos.LinkUpdateRequest;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SuppressWarnings("MagicNumber")
public class BotClient {

    private final RestClient restClient;
    private final static String DEFAULT_BASE_URL = "http://localhost:8090/bot/api";
    private final static String ENDPOINT_UPDATES = "/updates";
    private final RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus1xxHandler;
    private final RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus3xxHandler;
    private final RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus4xxHandler;
    private final RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus5xxHandler;

    public BotClient(
        ObjectMapper objectMapper,
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus1xxHandler,
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus3xxHandler,
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus4xxHandler,
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus5xxHandler
    ) {
        this.restClient = RestClient
            .builder()
            .baseUrl(BotClient.DEFAULT_BASE_URL)
            .build();

        this.endpointUpdatesStatus1xxHandler = endpointUpdatesStatus1xxHandler;
        this.endpointUpdatesStatus3xxHandler = endpointUpdatesStatus3xxHandler;
        this.endpointUpdatesStatus4xxHandler = endpointUpdatesStatus4xxHandler;
        this.endpointUpdatesStatus5xxHandler = endpointUpdatesStatus5xxHandler;
    }

    public BotClient(
        String baseUrl,
        ObjectMapper objectMapper,
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus1xxHandler,
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus3xxHandler,
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus4xxHandler,
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus5xxHandler
    ) {
        this.restClient = RestClient
            .builder()
            .baseUrl(baseUrl)
            .build();

        this.endpointUpdatesStatus1xxHandler = endpointUpdatesStatus1xxHandler;
        this.endpointUpdatesStatus3xxHandler = endpointUpdatesStatus3xxHandler;
        this.endpointUpdatesStatus4xxHandler = endpointUpdatesStatus4xxHandler;
        this.endpointUpdatesStatus5xxHandler = endpointUpdatesStatus5xxHandler;
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
