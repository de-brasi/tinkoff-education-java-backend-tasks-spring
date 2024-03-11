package edu.java.scrapper;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.java.clients.GitHubClient;
import edu.java.exceptions.EmptyResponseBodyException;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ClientTestConfig.class)
@WireMockTest(httpPort = 8080)
@Import(ClientTestConfig.class)
public class GitHubClientTest {
    @Autowired
    @Qualifier("testGitHubClient")
    GitHubClient gitHubClient;

    @Test
    @DisplayName("Get update time with GitHub client; Complete response body")
    public void updTimeCompleteBodyTest() {
        final String testURI = "repos";
        final String testOwner = "owner";
        final String testRepoName = "repo";

        stubFor(get("/" + testURI + "/" + testOwner + "/" + testRepoName)
            .willReturn(
                aResponse()
                    .withBody("\"updated_at\": \"2024-02-12T12:45:18Z\"")
                    .withStatus(200)
            )
        );

        AtomicReference<OffsetDateTime> fetchingResult = new AtomicReference<>();

        assertThatCode(
            () -> fetchingResult.set(gitHubClient.fetchUpdate(testOwner, testRepoName).updateTime())
        ).doesNotThrowAnyException();


        assertThat(
            fetchingResult.get()
        ).isEqualTo(
            OffsetDateTime.parse("2024-02-12T12:45:18Z")
        );
    }

    @Test
    @DisplayName("Get update time with GitHub client; Empty response body")
    public void updTimeEmptyBodyTest() {
        final String testURI = "repos";
        final String testOwner = "owner";
        final String testRepoName = "repo";

        stubFor(get("/" + testURI + "/" + testOwner + "/" + testRepoName)
            .willReturn(
                aResponse()
                    .withBody("")
                    .withStatus(200)
            )
        );

        assertThatThrownBy(
            () -> gitHubClient.fetchUpdate(testOwner, testRepoName).updateTime()
        ).isInstanceOf(EmptyResponseBodyException.class);
    }

    @Test
    @DisplayName("Get update time with GitHub client; Incomplete response body")
    public void updTimeIncompleteBodyTest() {
        final String testURI = "repos";
        final String testOwner = "owner";
        final String testRepoName = "repo";

        stubFor(get("/" + testURI + "/" + testOwner + "/" + testRepoName)
            .willReturn(
                aResponse()
                    .withBody("\"some_field\": \"2024-02-12T12:45:18Z\"")
                    .withStatus(200)
            )
        );

        assertThatThrownBy(
            () -> gitHubClient.fetchUpdate(testOwner, testRepoName).updateTime()
        ).isInstanceOf(FieldNotFoundException.class);
    }
}
