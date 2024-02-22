package edu.java.scrapper;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.java.clients.GitHubClient;
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
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WireMockTest(httpPort = 8080)
@Import(ClientTestConfig.class)
public class GitHubClientTest {
    @Autowired
    @Qualifier("testGitHubClient")
    GitHubClient gitHubClient;

    @Test
    @DisplayName("Get update time with GitHub client")
    public void test1() {
        final String testURL = "localhost";
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

        assertThat(
            gitHubClient.fetchUpdate(testOwner, testRepoName).updateTime()
        ).isEqualTo(
            OffsetDateTime.parse("2024-02-12T12:45:18Z")
        );
    }
}
