package edu.java.bot.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.dtos.AddLinkRequest;
import edu.common.dtos.ApiErrorResponse;
import edu.common.dtos.LinkResponse;
import edu.common.dtos.ListLinksResponse;
import edu.common.dtos.RemoveLinkRequest;
import edu.common.exceptions.ChatIdNotExistsException;
import edu.common.exceptions.IncorrectRequestException;
import edu.common.exceptions.UnexpectedResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import static org.springframework.http.MediaType.APPLICATION_JSON;

// TODO: при обработке ошибок запросов в вывод писать И запрос, И ответ
// TODO: обрабатывать ошибки сервера - 5xx
public class ScrapperClient {

    private final RestClient restClient;

    // TODO: получить порт приложения Bot из его файла конфигурации
    private final static String DEFAULT_BASE_URL = "http://localhost:8080/scrapper/api";

    private final static String ENDPOINT_CHAT_MANAGEMENT_PREFIX = "/tg-chat";
    private final static String ENDPOINT_LINK_MANAGEMENT_PREFIX = "/links";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient.ResponseSpec.ErrorHandler LINK_MANAGEMENT_CODE_4xx_ERROR_HANDLER = (req, resp) -> {
        ApiErrorResponse errorResponse = objectMapper.readValue(
            new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
        );

        if (resp.getStatusCode().value() == 400) {
            throw new IncorrectRequestException(errorResponse.getExceptionMessage());
        } else {
            throw new UnexpectedResponse(resp.getStatusCode().value(), errorResponse.getExceptionMessage());
        }

    };

    public ScrapperClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(ScrapperClient.DEFAULT_BASE_URL).build();
    }

    public ScrapperClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public void registerChat(Long chatId) {
        this.restClient
            .post()
            .uri(ScrapperClient.ENDPOINT_CHAT_MANAGEMENT_PREFIX + "/%d".formatted(chatId))
            .contentType(APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                ApiErrorResponse errorResponse = objectMapper.readValue(
                    new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
                );

                if (resp.getStatusCode().value() == 400) {
                    throw new IncorrectRequestException(
                            errorResponse.getExceptionMessage()
                    );
                }
                throw new UnexpectedResponse(
                        resp.getStatusCode().value(),
                        errorResponse.getExceptionMessage()
                );

            })
            .toEntity(ApiErrorResponse.class);
    }

    public void deleteChat(Long chatId) {
        this.restClient
            .delete()
            .uri(ScrapperClient.ENDPOINT_CHAT_MANAGEMENT_PREFIX + "/%d".formatted(chatId))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                ApiErrorResponse errorResponse = objectMapper.readValue(
                    new String(resp.getBody().readAllBytes()), ApiErrorResponse.class
                );

                switch (resp.getStatusCode().value()) {
                    case 400 -> throw new IncorrectRequestException(
                        errorResponse.getExceptionMessage()
                    );
                    case 404 -> throw new ChatIdNotExistsException(
                        errorResponse.getExceptionMessage()
                    );
                    default -> throw new UnexpectedResponse(
                        resp.getStatusCode().value(),
                        errorResponse.getExceptionMessage()
                    );
                }

            })
            .toEntity(ApiErrorResponse.class);
    }

    // todo: собственный DTO который хранит ссылку в типе URI
    public ListLinksResponse getAllTrackedLinks(Long chatId) {
        return this.restClient
            .get()
            .uri(ScrapperClient.ENDPOINT_LINK_MANAGEMENT_PREFIX)
            .header("Tg-Chat-Id", "%d".formatted(chatId))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, LINK_MANAGEMENT_CODE_4xx_ERROR_HANDLER)
            .body(ListLinksResponse.class);
    }

    // todo: собственный DTO который хранит ссылку в типе URI
    public LinkResponse trackLink(Long chatId, String link) {
        AddLinkRequest requestBody = new AddLinkRequest(link);
        return this.restClient
            .post()
            .uri(ScrapperClient.ENDPOINT_LINK_MANAGEMENT_PREFIX)
            .header("Tg-Chat-Id", "%d".formatted(chatId))
            .body(requestBody)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, LINK_MANAGEMENT_CODE_4xx_ERROR_HANDLER)
            .body(LinkResponse.class);
    }

    // todo: собственный DTO который хранит ссылку в типе URI
    public LinkResponse untrackLink(Long chatId, String link) {
        RemoveLinkRequest requestBody = new RemoveLinkRequest(link);
        return this.restClient
            .method(HttpMethod.DELETE)
            .uri(ScrapperClient.ENDPOINT_LINK_MANAGEMENT_PREFIX)
            .header("Tg-Chat-Id", "%d".formatted(chatId))
            .body(requestBody)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, LINK_MANAGEMENT_CODE_4xx_ERROR_HANDLER)
            .body(LinkResponse.class);
    }
}
