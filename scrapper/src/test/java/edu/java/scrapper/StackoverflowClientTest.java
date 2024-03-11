package edu.java.scrapper;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.java.clients.StackOverflowClient;
import edu.java.exceptions.FieldNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ClientTestConfig.class)
@WireMockTest(httpPort = 8080)
@Import(ClientTestConfig.class)
public class StackoverflowClientTest {
    @Autowired
    @Qualifier("testStackOverflowClient")
    StackOverflowClient stackOverflowClient;

    @Test
    @DisplayName("Get update time with StackOverflow client; Complete response body")
    public void updTimeCompleteBodyTest() {
        final String testURI = "questions";
        final int testQuestionId = 123456;
        final long testDate = 1549279221;


        stubFor(get(urlMatching("/" + testURI + "/" + testQuestionId + "\\??(.*)"))
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

        stubFor(get(urlMatching("/" + testURI + "/" + testQuestionId + "\\??(.*)"))
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

        stubFor(get(urlMatching("/" + testURI + "/" + testQuestionId + "\\??(.*)"))
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
