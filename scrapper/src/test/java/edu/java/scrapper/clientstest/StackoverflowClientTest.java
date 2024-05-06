package edu.java.scrapper.clientstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.java.clients.StackOverflowClient;
import edu.java.clients.exceptions.FieldNotFoundException;
import edu.java.configuration.ApplicationConfig;
import edu.java.configuration.ClientConfig;
import edu.java.configuration.ThirdPartyWebClientsConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {ClientConfig.class, ObjectMapper.class})
@EnableConfigurationProperties(value = {ThirdPartyWebClientsConfig.class, ApplicationConfig.class})
@WireMockTest
public class StackoverflowClientTest {

    @Autowired
    StackOverflowClient stackOverflowClient;

    @RegisterExtension
    static WireMockExtension wireMockExtension =
        WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    public static void setUpMockUrl(DynamicPropertyRegistry registry) {
        registry.add("third-party-web-clients.stackoverflow-properties.base-url", wireMockExtension::baseUrl);
    }

    @Test
    @DisplayName("Get update time with StackOverflow client; Complete response body")
    public void updTimeCompleteBodyTest() {
        final int testQuestionId = 123456;
        final long testDate = 1549279221;


        wireMockExtension.stubFor(get(urlMatching("/questions/[0-9]+\\??.*"))
            .willReturn(
                aResponse()
                    .withBody("\"last_activity_date\": " + testDate)
                    .withStatus(200)
            )
        );

        AtomicReference<OffsetDateTime> fetchingResult = new AtomicReference<>();

        assertThatCode(
            () -> fetchingResult.set(stackOverflowClient.fetchUpdate(testQuestionId).updateTime())
        ).doesNotThrowAnyException();

        assertThat(
            fetchingResult.get().toEpochSecond()
        ).isEqualTo(
            testDate
        );
    }

    @Test
    @DisplayName("Get update time with GitHub client; Empty response body")
    public void updTimeEmptyBodyTest() {
        final String testURI = "questions";
        final int testQuestionId = 123456;

        wireMockExtension.stubFor(get(urlMatching("/questions/[0-9]+\\??.*"))
            .willReturn(
                aResponse()
                    .withBody("")
                    .withStatus(200)
            )
        );

        assertThatThrownBy(
            () -> stackOverflowClient.fetchUpdate(testQuestionId).updateTime()
        ).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Get update time with GitHub client; Incomplete response body")
    public void updTimeIncompleteBodyTest() {
        final String testURI = "questions";
        final int testQuestionId = 123456;

        wireMockExtension.stubFor(get(urlMatching("/questions/[0-9]+\\??.*"))
            .willReturn(
                aResponse()
                    .withBody("\"some_field\": \"2024-02-12T12:45:18Z\"")
                    .withStatus(200)
            )
        );

        assertThatThrownBy(
            () -> stackOverflowClient.fetchUpdate(testQuestionId).updateTime()
        ).isInstanceOf(FieldNotFoundException.class);
    }
}
