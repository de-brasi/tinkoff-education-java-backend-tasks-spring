package edu.java.bot.apitest.scrapperclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.common.dtos.ApiErrorResponse;
import edu.common.dtos.LinkResponse;
import edu.common.dtos.ListLinksResponse;
import edu.common.exceptions.ChatIdNotExistsException;
import edu.common.exceptions.IncorrectRequestException;
import edu.java.bot.client.ScrapperClient;
import edu.java.bot.configuration.ScrapperClientConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.net.URI;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {TestConfig.class, ScrapperClientConfig.class})
@WireMockTest(httpPort = 8080)
public class ScrapperClientTest {
    @Autowired
    ScrapperClient scrapperClient;

    final String TEST_URI = "scrapperMock";

    final ObjectMapper objectMapper = new ObjectMapper();

    final ApiErrorResponse apiErrorResponseStub = new ApiErrorResponse(
        "testDescription",
        "testCode",
        "testExceptionName",
        "testExceptionMessage",
        null
    );

    final ListLinksResponse listLinksResponseStub = new ListLinksResponse(
        List.of(
            new LinkResponse(1, "https://www.wikipedia.org/"),
            new LinkResponse(2, "https://en.wikipedia.org/wiki/Main_Page")
        ),
        2
    );

    final edu.java.bot.client.dtos.LinkResponse linkResponseStub = new edu.java.bot.client.dtos.LinkResponse(
        1,
        URI.create("https://www.wikipedia.org/")
    );

    @Test
    @DisplayName("test /tg-chat/{id} registry")
    public void registry_chat_success() {
        final long chatIdToRegistry = 1;

        stubFor(
            post("/" + TEST_URI + "/tg-chat/" + chatIdToRegistry)
                .willReturn(
                    aResponse()
                        .withStatus(200)
                )
        );

        assertThatCode(() -> scrapperClient.registerChat(chatIdToRegistry)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("test /tg-chat/{id} registry")
    public void registry_chat_failure_400() throws JsonProcessingException {
        final long chatIdToRegistry = 1;

        stubFor(
            post("/" + TEST_URI + "/tg-chat/" + chatIdToRegistry)
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(apiErrorResponseStub))
                        .withStatus(400)
                )
        );

        assertThatThrownBy(
            () -> scrapperClient.registerChat(chatIdToRegistry)
        ).isInstanceOf(IncorrectRequestException.class);
    }

    @Test
    @DisplayName("test /tg-chat/{id} delete")
    public void delete_chat_success() {
        final long chatIdToDelete = 1;

        stubFor(
            delete("/" + TEST_URI + "/tg-chat/" + chatIdToDelete)
                .willReturn(
                    aResponse()
                        .withStatus(200)
                )
        );

        assertThatCode(() -> scrapperClient.deleteChat(chatIdToDelete)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("test /tg-chat/{id} delete")
    public void delete_chat_failure_400() throws JsonProcessingException {
        final long chatIdToDelete = 1;

        stubFor(
            delete("/" + TEST_URI + "/tg-chat/" + chatIdToDelete)
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(apiErrorResponseStub))
                        .withStatus(400)
                )
        );

        assertThatThrownBy(
            () -> scrapperClient.deleteChat(chatIdToDelete)
        ).isInstanceOf(IncorrectRequestException.class);
    }

    @Test
    @DisplayName("test /tg-chat/{id} delete")
    public void delete_chat_failure_404() throws JsonProcessingException {
        final long chatIdToDelete = 1;

        stubFor(
            delete("/" + TEST_URI + "/tg-chat/" + chatIdToDelete)
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(apiErrorResponseStub))
                        .withStatus(404)
                )
        );

        assertThatThrownBy(
            () -> scrapperClient.deleteChat(chatIdToDelete)
        ).isInstanceOf(ChatIdNotExistsException.class);
    }

    @Test
    @DisplayName("test /links get")
    public void get_links_success() throws JsonProcessingException {
        final long chatIdToGetTrackedLinks = 1;

        stubFor(
            get("/" + TEST_URI + "/links")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(listLinksResponseStub))
                        .withStatus(200)
                )
        );

        assertThatCode(() -> scrapperClient.getAllTrackedLinks(chatIdToGetTrackedLinks)).doesNotThrowAnyException();
        assertThat(scrapperClient.getAllTrackedLinks(chatIdToGetTrackedLinks))
            .isInstanceOf(edu.java.bot.client.dtos.ListLinksResponse.class);
    }

    @Test
    @DisplayName("test /links get")
    public void get_links_failure_400() throws JsonProcessingException {
        final long chatIdToGetTrackedLinks = 1;

        stubFor(
            get("/" + TEST_URI + "/links")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(apiErrorResponseStub))
                        .withStatus(400)
                )
        );

        assertThatThrownBy(
            () -> scrapperClient.getAllTrackedLinks(chatIdToGetTrackedLinks)
        ).isInstanceOf(IncorrectRequestException.class);
    }
}
