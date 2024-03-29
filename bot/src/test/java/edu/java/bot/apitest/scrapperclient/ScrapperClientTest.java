package edu.java.bot.apitest.scrapperclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.common.datatypes.dtos.LinkResponse;
import edu.common.datatypes.dtos.ListLinksResponse;
import edu.common.datatypes.exceptions.httpresponse.BadHttpResponseException;
import edu.java.bot.client.ScrapperClient;
import edu.java.bot.configuration.ScrapperClientConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {TestConfig.class, ScrapperClientConfig.class})
@WireMockTest(httpPort = 8080)
public class ScrapperClientTest {
    @Autowired
    @Qualifier("scrapperTestClient")
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

    @Test
    @DisplayName("test /tg-chat/{id} registry")
    public void registry_chat_success() {
        final long chatIdToRegistry = 1;
        final String postUrl = "/" + TEST_URI + "/tg-chat/" + chatIdToRegistry;

        stubFor(
            post(postUrl)
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

        BadHttpResponseException exception = assertThrows(
            BadHttpResponseException.class,
            () -> scrapperClient.registerChat(chatIdToRegistry)
        );
        assertThat(exception.getHttpCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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

        BadHttpResponseException exception = assertThrows(
            BadHttpResponseException.class,
            () -> scrapperClient.deleteChat(chatIdToDelete)
        );
        assertThat(exception.getHttpCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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

        BadHttpResponseException exception = assertThrows(
            BadHttpResponseException.class,
            () -> scrapperClient.deleteChat(chatIdToDelete)
        );
        assertThat(exception.getHttpCode()).isEqualTo(HttpStatus.NOT_FOUND);
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

        BadHttpResponseException exception = assertThrows(
            BadHttpResponseException.class,
            () -> scrapperClient.getAllTrackedLinks(chatIdToGetTrackedLinks)
        );
        assertThat(exception.getHttpCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
