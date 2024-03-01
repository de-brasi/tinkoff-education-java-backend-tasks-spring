package edu.java.client;

import edu.java.api.dtos.ApiErrorResponse;
import edu.java.api.dtos.LinkUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import java.util.List;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class BotClient {

    private final RestClient restClient;

    // TODO: получить порт приложения Bot из его файла конфигурации
    private final static String DEFAULT_BASE_URL = "http://localhost:8090/bot/api";

    private final static String ENDPOINT_UPDATES = "/updates";

    public BotClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(BotClient.DEFAULT_BASE_URL).build();
    }

    public BotClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    // todo: возврат void, если ответ не 200, то бросать какую то ошибку
    public ApiErrorResponse sendUpdates(
        int id, String updatedUrl, String updateDescription, List<Integer> subscribers
    ) {
        LinkUpdateRequest updates = new LinkUpdateRequest(id, updatedUrl, updateDescription, subscribers);
        ResponseEntity<ApiErrorResponse> response = this.restClient
            .post()
            .uri(BotClient.ENDPOINT_UPDATES)
            .contentType(APPLICATION_JSON)
            .body(updates)
            .retrieve()
            .toEntity(ApiErrorResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            // todo: better logging
            System.out.println(response.getBody());
        }

        return response.getBody();
    }

}
