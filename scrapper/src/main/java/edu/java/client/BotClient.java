package edu.java.client;

import edu.java.client.dtos.ApiErrorResponse;
import edu.java.client.dtos.LinkUpdateRequest;
import edu.java.client.exceptions.IncorrectRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Objects;
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

    // TODO: исправить обработку ошибок
    public void sendUpdates(
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

            // todo отличать ошибку 400 от 404 и тд, то есть сделать как в клиенте bot
            throw new IncorrectRequestException(
                Objects.requireNonNull(response.getBody()).getExceptionMessage()
            );
        }
    }

}
