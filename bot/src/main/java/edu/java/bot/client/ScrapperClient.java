package edu.java.bot.client;

import edu.common.Retry;
import edu.common.dtos.AddLinkRequest;
import edu.common.dtos.RemoveLinkRequest;
import edu.java.bot.client.dtos.LinkResponse;
import edu.java.bot.client.dtos.ListLinksResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SuppressWarnings({"MagicNumber", "MultipleStringLiterals"})
public class ScrapperClient {

    private final RestClient restClient;
    private static final String DEFAULT_BASE_URL = "http://localhost:8080/scrapper/api";
    private static final String ENDPOINT_CHAT_MANAGEMENT_PREFIX = "/tg-chat";
    private static final String ENDPOINT_LINK_MANAGEMENT_PREFIX = "/links";
    private static final String CUSTOM_HEADER_TG_CHAT_ID = "Tg-Chat-Id";
    private final RestClient.ResponseSpec.ErrorHandler badResponseStatusHandler;

    public ScrapperClient(
        RestClient.ResponseSpec.ErrorHandler badResponseStatusHandler
    ) {
        this.badResponseStatusHandler = badResponseStatusHandler;
        this.restClient = RestClient
            .builder()
            .baseUrl(ScrapperClient.DEFAULT_BASE_URL)
            .build();
    }

    public ScrapperClient(
        String baseUrl,
        RestClient.ResponseSpec.ErrorHandler badResponseStatusHandler
    ) {
        this.badResponseStatusHandler = badResponseStatusHandler;
        this.restClient = RestClient
            .builder()
            .baseUrl(baseUrl)
            .build();
    }

    public void registerChat(Long chatId) {
        this.restClient
            .post()
            .uri(ScrapperClient.ENDPOINT_CHAT_MANAGEMENT_PREFIX + "/%d".formatted(chatId))
            .contentType(APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::is1xxInformational, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is3xxRedirection, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is4xxClientError, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is5xxServerError, badResponseStatusHandler)
            .toBodilessEntity();
    }

    public void deleteChat(Long chatId) {
        this.restClient
            .delete()
            .uri(ScrapperClient.ENDPOINT_CHAT_MANAGEMENT_PREFIX + "/%d".formatted(chatId))
            .retrieve()
            .onStatus(HttpStatusCode::is1xxInformational, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is3xxRedirection, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is4xxClientError, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is5xxServerError, badResponseStatusHandler)
            .toBodilessEntity();
    }

    public ListLinksResponse getAllTrackedLinks(Long chatId) {
        return this.restClient
            .get()
            .uri(ScrapperClient.ENDPOINT_LINK_MANAGEMENT_PREFIX)
            .header(CUSTOM_HEADER_TG_CHAT_ID, "%d".formatted(chatId))
            .retrieve()
            .onStatus(HttpStatusCode::is1xxInformational, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is3xxRedirection, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is4xxClientError, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is5xxServerError, badResponseStatusHandler)
            .body(ListLinksResponse.class);
    }

    public LinkResponse trackLink(Long chatId, String link) {
        AddLinkRequest requestBody = new AddLinkRequest(link);
        return this.restClient
            .post()
            .uri(ScrapperClient.ENDPOINT_LINK_MANAGEMENT_PREFIX)
            .header(CUSTOM_HEADER_TG_CHAT_ID, "%d".formatted(chatId))
            .body(requestBody)
            .retrieve()
            .onStatus(HttpStatusCode::is1xxInformational, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is3xxRedirection, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is4xxClientError, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is5xxServerError, badResponseStatusHandler)
            .body(LinkResponse.class);
    }

    public LinkResponse untrackLink(Long chatId, String link) {
        RemoveLinkRequest requestBody = new RemoveLinkRequest(link);
        return this.restClient
            .method(HttpMethod.DELETE)
            .uri(ScrapperClient.ENDPOINT_LINK_MANAGEMENT_PREFIX)
            .header(CUSTOM_HEADER_TG_CHAT_ID, "%d".formatted(chatId))
            .body(requestBody)
            .retrieve()
            .onStatus(HttpStatusCode::is1xxInformational, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is3xxRedirection, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is4xxClientError, badResponseStatusHandler)
            .onStatus(HttpStatusCode::is5xxServerError, badResponseStatusHandler)
            .body(LinkResponse.class);
    }
}
