package edu.java.clients;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SuppressWarnings("MagicNumber")
public class BotClient {

    private final RestClient restClient;
    private final static String DEFAULT_BASE_URL = "http://localhost:8090/bot/api";
    private final static String ENDPOINT_UPDATES = "/updates";
    private final RestClient.ResponseSpec.ErrorHandler notOkResponseHandler;

    public BotClient(RestClient.ResponseSpec.ErrorHandler notOkResponseHandler) {
        this.restClient = RestClient
            .builder()
            .baseUrl(BotClient.DEFAULT_BASE_URL)
            .build();

        this.notOkResponseHandler = notOkResponseHandler;
    }

    public BotClient(String baseUrl, RestClient.ResponseSpec.ErrorHandler notOkResponseHandler) {
        this.restClient = RestClient
            .builder()
            .baseUrl(baseUrl)
            .build();

        this.notOkResponseHandler = notOkResponseHandler;
    }

    public void sendUpdates(
        int id, String updatedUrl, String updateDescription, List<Long> subscribers
    ) {
        LinkUpdateRequest updates = new LinkUpdateRequest(id, updatedUrl, updateDescription, subscribers);
        this.restClient
            .post()
            .uri(BotClient.ENDPOINT_UPDATES)
            .contentType(APPLICATION_JSON)
            .body(updates)
            .retrieve()
            .onStatus(HttpStatusCode::is1xxInformational, notOkResponseHandler)
            .onStatus(HttpStatusCode::is3xxRedirection, notOkResponseHandler)
            .onStatus(HttpStatusCode::is4xxClientError, notOkResponseHandler)
            .onStatus(HttpStatusCode::is5xxServerError, notOkResponseHandler)
            .toBodilessEntity();
    }

}
