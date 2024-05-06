package edu.java.scrapper.domaintest.jdbc;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.java.domain.repositories.jdbc.JdbcLinkRepository;
import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest
public class JdbcLinkRepositoryTest extends IntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcLinkRepository linkRepository;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension
        .newInstance()
        .options(
            WireMockConfiguration
                .wireMockConfig()
                .dynamicPort()
        )
        .build();

    private static final String TEST_LINK = "https://stackoverflow.com/questions/100500";

    @DynamicPropertySource
    public static void mockStackOverflowClientBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("third-party-web-clients.stackoverflow-properties.base-url", wireMockExtension::baseUrl);
    }

    @BeforeEach
    public void configureProperties() {
        final String stubResponseBody = """
                {
                  "items": [
                    {
                      "tags": [
                        "java",
                        "xml",
                        "csv",
                        "data-conversion"
                      ],
                      "is_answered": true,
                      "protected_date": 1433355035,
                      "closed_date": 1543288543,
                      "last_activity_date": 1590400952,
                      "creation_date": 1217606932,
                      "last_edit_date": 1445938449,
                      "question_id": 123,
                      "link": "https://stackoverflow.com/questions/123/java-lib-or-app-to-convert-csv-to-xml-file",
                      "title": "Java lib or app to convert CSV to XML file?"
                    }
                  ]
                }
            """;
        wireMockExtension.stubFor(
            WireMock.get(WireMock.urlMatching("/questions/[0-9]+\\??.*"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
                        .withBody(stubResponseBody)
                )
        );
    }

    @Test
    @Transactional
    @Rollback
    void addURLTest() {
        final int res = linkRepository.add(TEST_LINK);
        assertThat(res).isEqualTo(1);

        String query = "SELECT COUNT(*) FROM links WHERE url = ?";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, TEST_LINK);
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addEqualURLsTest() {
        final int res = linkRepository.add(TEST_LINK);
        assertThat(res).isEqualTo(1);

        final int resOneMore = linkRepository.add(TEST_LINK);
        assertThat(resOneMore).isEqualTo(0);

        String query = "SELECT COUNT(*) FROM links WHERE url = ?";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, TEST_LINK);
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() {
        // add record
        final int addResult = linkRepository.add(TEST_LINK);
        assertThat(addResult).isEqualTo(1);

        // clear record
        final int removeResult = linkRepository.remove(TEST_LINK);
        assertThat(removeResult).isEqualTo(1);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM links WHERE url = ?",
            Integer.class,
            TEST_LINK
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeTwiceTest() {
        // add record
        final int addResult = linkRepository.add(TEST_LINK);
        assertThat(addResult).isEqualTo(1);

        // clear record once
        final int firstRemoveResult = linkRepository.remove(TEST_LINK);
        assertThat(firstRemoveResult).isEqualTo(1);

        // clear record twice
        int secondRemoveResult = linkRepository.remove(TEST_LINK);
        assertThat(secondRemoveResult).isEqualTo(0);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM links WHERE url = ?",
            Integer.class,
            TEST_LINK
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void getAllTest() {
        final String testLink1 = "https://stackoverflow.com/questions/100500";
        final String testLink2 = "https://stackoverflow.com/questions/100501";
        final String testLink3 = "https://stackoverflow.com/questions/100502";

        // add records
        final int addResult1 = linkRepository.add(testLink1);
        assertThat(addResult1).isEqualTo(1);
        final int addResult2 = linkRepository.add(testLink2);
        assertThat(addResult2).isEqualTo(1);
        final int addResult3 = linkRepository.add(testLink3);
        assertThat(addResult3).isEqualTo(1);

        // check result contains all
        Collection<String> gettingAll = linkRepository.findAll();
        assertThat(gettingAll).containsExactlyInAnyOrder(testLink1, testLink2, testLink3);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM links WHERE url IN (?, ?, ?)",
            Integer.class,
            testLink1, testLink2, testLink3
        );
        assertThat(rowCount).isEqualTo(3);
    }

    @Test
    @Transactional
    @Rollback
    void updateLastCheckTimeTest() {
        // add record
        linkRepository.add(TEST_LINK);

        Timestamp newCheckTimeExpected = Timestamp.from(Instant.now());
        linkRepository.updateLastCheckTime(TEST_LINK, newCheckTimeExpected);

        Timestamp newCheckTimeActual = jdbcTemplate.queryForObject(
            "select last_check_time from links where url = ?",
            Timestamp.class,
            TEST_LINK
        );

        assert newCheckTimeActual != null;
        assertThat(newCheckTimeExpected.toInstant()).isCloseTo(
            newCheckTimeActual.toInstant(),
            within(1, ChronoUnit.MILLIS)
        );
    }

    @Test
    @Transactional
    @Rollback
    void updateLastUpdateTimeTest() {
        // add record
        linkRepository.add(TEST_LINK);

        Timestamp newCheckTimeExpected = Timestamp.from(Instant.now());
        linkRepository.updateLastUpdateTime(TEST_LINK, newCheckTimeExpected);

        Timestamp newCheckTimeActual = jdbcTemplate.queryForObject(
            "select last_update_time from links where url = ?",
            Timestamp.class,
            TEST_LINK
        );

        assert newCheckTimeActual != null;
        assertThat(newCheckTimeExpected.toInstant()).isCloseTo(
            newCheckTimeActual.toInstant(),
            within(1, ChronoUnit.MILLIS)
        );
    }
}
