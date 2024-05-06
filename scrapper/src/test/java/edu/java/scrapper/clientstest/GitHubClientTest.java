package edu.java.scrapper.clientstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.java.clients.GitHubClient;
import edu.java.clients.exceptions.EmptyResponseBodyException;
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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {ClientTestConfig.class, ClientConfig.class, ObjectMapper.class})
@EnableConfigurationProperties(value = {ThirdPartyWebClientsConfig.class, ApplicationConfig.class})
@WireMockTest
public class GitHubClientTest {

    @Autowired
    GitHubClient gitHubClient;

    @RegisterExtension
    static WireMockExtension wireMockExtension =
        WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    private final static String UPDATE_TIME = "2024-02-12T12:45:18Z";

    private static final String MOCKED_BODY = """
        {
          "id": 756321201,
          "node_id": "R_kgDOLRSLsQ",
          "name": "tinkoff-education-java-backend-tasks-spring",
          "full_name": "de-brasi/tinkoff-education-java-backend-tasks-spring",
          "private": false,
          "owner": {
            "login": "de-brasi",
            "id": 89832786,
            "node_id": "MDQ6VXNlcjg5ODMyNzg2",
            "avatar_url": "https://avatars.githubusercontent.com/u/89832786?v=4",
            "gravatar_id": "",
            "url": "https://api.github.com/users/de-brasi",
            "html_url": "https://github.com/de-brasi",
            "followers_url": "https://api.github.com/users/de-brasi/followers",
            "following_url": "https://api.github.com/users/de-brasi/following{/other_user}",
            "gists_url": "https://api.github.com/users/de-brasi/gists{/gist_id}",
            "starred_url": "https://api.github.com/users/de-brasi/starred{/owner}{/repo}",
            "subscriptions_url": "https://api.github.com/users/de-brasi/subscriptions",
            "organizations_url": "https://api.github.com/users/de-brasi/orgs",
            "repos_url": "https://api.github.com/users/de-brasi/repos",
            "events_url": "https://api.github.com/users/de-brasi/events{/privacy}",
            "received_events_url": "https://api.github.com/users/de-brasi/received_events",
            "type": "User",
            "site_admin": false
          },
          "created_at": "2024-02-12T12:44:47Z",
          "updated_at": "%s",
          "pushed_at": "%s",
          "visibility": "public",
          "forks": 0,
          "open_issues": 7,
          "watchers": 0,
          "default_branch": "master",
          "temp_clone_token": null,
          "network_count": 0,
          "subscribers_count": 1
        }
        """.formatted(UPDATE_TIME, UPDATE_TIME);

    @DynamicPropertySource
    public static void setUpMockUrl(DynamicPropertyRegistry registry) {
        registry.add("third-party-web-clients.github-properties.base-url", wireMockExtension::baseUrl);
    }

    @Test
    @DisplayName("Get update time with GitHub client; Complete response body")
    public void updTimeCompleteBodyTest() {
        final String testOwner = "owner";
        final String testRepoName = "repo";

        wireMockExtension.stubFor(get("/repos/%s/%s".formatted(testOwner, testRepoName))
            .willReturn(
                aResponse()
                    .withBody(MOCKED_BODY)
                    .withStatus(200)
            )
        );

        AtomicReference<OffsetDateTime> fetchingResult = new AtomicReference<>();

        assertThatCode(
            () -> fetchingResult.set(gitHubClient.fetchUpdate(testOwner, testRepoName).updateTime())
        ).doesNotThrowAnyException();


        assertThat(
            fetchingResult.get().isEqual(OffsetDateTime.parse(UPDATE_TIME))
        ).isTrue();
    }

    @Test
    @DisplayName("Get update time with GitHub client; Empty response body")
    public void updTimeEmptyBodyTest() {
        final String testOwner = "owner";
        final String testRepoName = "repo";

        wireMockExtension.stubFor(get("/repos/%s/%s".formatted(testOwner, testRepoName))
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
        final String testOwner = "owner";
        final String testRepoName = "repo";
        final String incompleteBody = """
            {
                "owner": {
                "login": "de-brasi",
                "id": 89832786,
                "node_id": "MDQ6VXNlcjg5ODMyNzg2",
                "avatar_url": "https://avatars.githubusercontent.com/u/89832786?v=4",
                "gravatar_id": "",
                "url": "https://api.github.com/users/de-brasi",
                "html_url": "https://github.com/de-brasi",
                "followers_url": "https://api.github.com/users/de-brasi/followers",
                "following_url": "https://api.github.com/users/de-brasi/following{/other_user}",
                "gists_url": "https://api.github.com/users/de-brasi/gists{/gist_id}",
                "starred_url": "https://api.github.com/users/de-brasi/starred{/owner}{/repo}",
                "subscriptions_url": "https://api.github.com/users/de-brasi/subscriptions",
                "organizations_url": "https://api.github.com/users/de-brasi/orgs",
                "repos_url": "https://api.github.com/users/de-brasi/repos",
                "events_url": "https://api.github.com/users/de-brasi/events{/privacy}",
                "received_events_url": "https://api.github.com/users/de-brasi/received_events",
                "type": "User",
                "site_admin": false
                }
            }
            """;

        wireMockExtension.stubFor(get("/repos/%s/%s".formatted(testOwner, testRepoName))
            .willReturn(
                aResponse()
                    .withBody(incompleteBody)
                    .withStatus(200)
            )
        );

        assertThatThrownBy(
            () -> gitHubClient.fetchUpdate(testOwner, testRepoName).updateTime()
        ).isInstanceOf(FieldNotFoundException.class);
    }
}
