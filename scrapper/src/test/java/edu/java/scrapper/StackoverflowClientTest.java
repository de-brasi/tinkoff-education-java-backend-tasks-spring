package edu.java.scrapper;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.java.clients.StackOverflowClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.OffsetDateTime;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WireMockTest(httpPort = 8080)
@Import(ClientTestConfig.class)
public class StackoverflowClientTest {
    @Autowired
    @Qualifier("testStackOverflowClient")
    StackOverflowClient stackOverflowClient;

    @Test
    @DisplayName("Get update time with StackOverflow client")
    public void test1() {
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

        assertThat(
            stackOverflowClient.fetchUpdate(testQuestionId).updateTime().toEpochSecond()
        ).isEqualTo(
            testDate
        );
    }
}
