package edu.java.bot.client;

import edu.java.bot.api.dtos.ApiErrorResponse;
import edu.java.bot.api.dtos.ListLinksResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class ScrapperClient {

    private final RestClient restClient;

    // TODO: получить порт приложения Bot из его файла конфигурации
    private final static String DEFAULT_BASE_URL = "http://localhost:8090/scrapper/api";

    private final static String ENDPOINT_CHAT_MANAGEMENT_PREFIX = "/tg-chat";
    private final static String ENDPOINT_LINK_MANAGEMENT_PREFIX = "/links";

    public ScrapperClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(ScrapperClient.DEFAULT_BASE_URL).build();
    }

    public ScrapperClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    // todo: возврат void, если ответ не 200, то бросать какую то ошибку;
    //  если ответ не 200, то просмотреть ответ (по всем возможным кодам),
    //  в соотв блоке switch сделать каст ответа Object -> DTO и прокинуть нужную ошибку
    public ApiErrorResponse registerChat(Long chatId) {
        // todo

        ResponseEntity<ApiErrorResponse> response = this.restClient
            .post()
            .uri(ScrapperClient.ENDPOINT_CHAT_MANAGEMENT_PREFIX + "/%d".formatted(chatId))
            .contentType(APPLICATION_JSON)
            .retrieve()
            .toEntity(ApiErrorResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            // todo: better logging
            System.out.println(response.getBody());
        }

        return response.getBody();
    }

    // todo: возврат void, если ответ не 200, то бросать какую то ошибку
    public ApiErrorResponse deleteChat(Long chatId) {
        // todo

        ResponseEntity<ApiErrorResponse> response = this.restClient
            .delete()
            .uri(ScrapperClient.ENDPOINT_CHAT_MANAGEMENT_PREFIX + "/%d".formatted(chatId))
            .retrieve()
            .toEntity(ApiErrorResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            // todo: better logging
            System.out.println(response.getBody());
        }

        return response.getBody();
    }

    // todo: возврат void, если ответ не 200, то бросать какую то ошибку
    public ListLinksResponse getAllTrackedLinks(Long chatId) {
        // todo
        // todo: как обработать ошибку?

        ResponseEntity<ListLinksResponse> response = this.restClient
            .get()
            .uri(ScrapperClient.ENDPOINT_LINK_MANAGEMENT_PREFIX)
            .header("Tg-Chat-Id", "%d".formatted(chatId))
            .retrieve()
            .toEntity(ListLinksResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            // todo: better logging
            System.out.println(response.getBody());
        }

        return response.getBody();
    }

    // todo: возврат void, если ответ не 200, то бросать какую то ошибку
    public ListLinksResponse trackLink(Long chatId, String link) {
        // todo
        // todo: как обработать ошибку?

        ResponseEntity<ListLinksResponse> response = this.restClient
            .post()
            .uri(ScrapperClient.ENDPOINT_LINK_MANAGEMENT_PREFIX)
            .header("Tg-Chat-Id", "%d".formatted(chatId))
            .retrieve()
            .toEntity(ListLinksResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            // todo: better logging
            System.out.println(response.getBody());
        }

        return response.getBody();
    }

    // todo: возврат void, если ответ не 200, то бросать какую то ошибку
    public ListLinksResponse untrackLink(Long chatId, String link) {
        // todo
        // todo: как обработать ошибку?

        ResponseEntity<ListLinksResponse> response = this.restClient
            .delete()
            .uri(ScrapperClient.ENDPOINT_LINK_MANAGEMENT_PREFIX)
            .header("Tg-Chat-Id", "%d".formatted(chatId))
            .retrieve()
            .toEntity(ListLinksResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            // todo: better logging
            System.out.println(response.getBody());
        }

        return response.getBody();
    }

}
