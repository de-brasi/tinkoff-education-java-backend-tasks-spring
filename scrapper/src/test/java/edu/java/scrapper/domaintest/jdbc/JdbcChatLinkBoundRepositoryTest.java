package edu.java.scrapper.domaintest.jdbc;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.java.domain.repositories.jdbc.JdbcChatLinkBoundRepository;
import edu.java.domain.repositories.jdbc.JdbcLinkRepository;
import edu.java.domain.repositories.jdbc.JdbcTelegramChatRepository;
import edu.java.domain.exceptions.NoExpectedEntityInDataBaseException;
import edu.java.domain.entities.ChatLinkBound;
import edu.java.scrapper.IntegrationTest;
import edu.java.services.jdbc.JdbcLinkUpdater;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest
public class JdbcChatLinkBoundRepositoryTest extends IntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcChatLinkBoundRepository chatLinkBoundRepository;

    @Autowired
    private JdbcTelegramChatRepository telegramChatRepository;

    @Autowired
    private JdbcLinkRepository linkRepository;

    @MockBean
    JdbcLinkUpdater linkUpdater;

    private static final String TEST_LINK = "https://stackoverflow.com/questions/100500";

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension
        .newInstance()
        .options(
            WireMockConfiguration
                .wireMockConfig()
                .dynamicPort()
        )
        .build();

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

    @BeforeEach
    void testTablesCleared() {
        // check all tables is empty
        String allFromChatTableQuery = "select count(*) from telegram_chat";
        String allFromLinksTableQuery = "select count(*) from links";
        String allFromBoundsTableQuery = "select count(*) from track_info";

        int chatRecordsCount = jdbcTemplate.queryForObject(allFromChatTableQuery, Integer.class);
        int linksRecordsCount = jdbcTemplate.queryForObject(allFromLinksTableQuery, Integer.class);
        int trackingRecordsCount = jdbcTemplate.queryForObject(allFromBoundsTableQuery, Integer.class);

        assertThat(chatRecordsCount).isEqualTo(0);
        assertThat(linksRecordsCount).isEqualTo(0);
        assertThat(trackingRecordsCount).isEqualTo(0);
    }

    @AfterEach
    void testTables() {
        jdbcTemplate.update("truncate table telegram_chat cascade ");
        jdbcTemplate.update("truncate table links cascade ");
        jdbcTemplate.update("truncate table track_info cascade ");
    }

    @Test
    @Transactional
    @Rollback
    void addCorrectPair() {
        // preconditions
        final Long testChat = 1L;
        telegramChatRepository.add(testChat);
        linkRepository.add(TEST_LINK);

        // bound
        int resCount = chatLinkBoundRepository.add(new ChatLinkBound(testChat, TEST_LINK));
        assertThat(resCount).isEqualTo(1);

        // checkout records count
        String query =
            "select count(*) " +
            "from track_info " +
            "where link_id = (select id as link_id from links where url = ?) " +
            "and telegram_chat_id = (select id as tg_chat_id from telegram_chat where chat_id = ?);";

        int rowCount = jdbcTemplate.queryForObject(
            query,
            Integer.class,
            TEST_LINK, testChat
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addCorrectPairTwice() {
        // preconditions
        final Long testChat = 2L;
        telegramChatRepository.add(testChat);
        linkRepository.add(TEST_LINK);

        // bound
        int res = chatLinkBoundRepository.add(new ChatLinkBound(testChat, TEST_LINK));
        assertThat(res).isEqualTo(1);

        int secondRes = chatLinkBoundRepository.add(new ChatLinkBound(testChat, TEST_LINK));
        assertThat(secondRes).isEqualTo(0);

        // checkout records count
        String query =
            "select count(*) " +
                "from track_info " +
                "where link_id = (select id as link_id from links where url = ?) " +
                "and telegram_chat_id = (select id as tg_chat_id from telegram_chat where chat_id = ?);";

        int rowCount = jdbcTemplate.queryForObject(
            query,
            Integer.class,
            TEST_LINK, testChat
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addIncorrectPairTgChatRequired() {
        // preconditions
        final Long testChat = 3L;
        linkRepository.add(TEST_LINK);

        assertThatThrownBy(() -> chatLinkBoundRepository.add(new ChatLinkBound(
            testChat, TEST_LINK
        ))).isInstanceOf(NoExpectedEntityInDataBaseException.class);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairLinkRequiredCheckLinkActuallyAddedToTable() {
        // preconditions
        final Long testChat = 4L;
        telegramChatRepository.add(testChat);

        // bound
        chatLinkBoundRepository.add(new ChatLinkBound(testChat, TEST_LINK));

        // check link actually added
        String queryToCheckTestLink = "select count(*) from links where url = ?";
        int rowCount = jdbcTemplate.queryForObject(
            queryToCheckTestLink,
            Integer.class,
            TEST_LINK
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairLinkRequiredCheckBoundingInformationActuallyCreated() {
        // preconditions
        final Long testChat = 5L;
        telegramChatRepository.add(testChat);

        // bound
        int res = chatLinkBoundRepository.add(new ChatLinkBound(testChat, TEST_LINK));
        assertThat(res).isEqualTo(1);

        // check link actually added
        String queryToCheckBoundingInfo =
            "select count(*) " +
            "from track_info " +
            "where link_id = (select id as link_id from links where url = ?) " +
            "and telegram_chat_id = (select id as tg_chat_id from telegram_chat where chat_id = ?);";

        int rowCount = jdbcTemplate.queryForObject(
            queryToCheckBoundingInfo,
            Integer.class,
            TEST_LINK, testChat
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void removeCorrectPair() {
        // preconditions
        final Long testChat = 7L;
        telegramChatRepository.add(testChat);
        linkRepository.add(TEST_LINK);
        chatLinkBoundRepository.add(new ChatLinkBound(testChat, TEST_LINK));

        // remove
        final ChatLinkBound removedExpected = new ChatLinkBound(testChat, TEST_LINK);
        final int removedRowsCount = chatLinkBoundRepository.remove(removedExpected);

        // checkout equivalency
        assertThat(removedRowsCount).isEqualTo(1);

        // checkout records count
        String query =
            "select count(*) " +
                "from track_info " +
                "where link_id = ((select id as tg_chat_id from telegram_chat where chat_id = ?)) " +
                "and telegram_chat_id = (select id as link_id from links where url = ?);";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, testChat , TEST_LINK);
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairTgChatNotExists() {
        stubFor(WireMock.get(urlEqualTo("/2.3/questions/100500"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"items\":[{\"tags\":[\"java\",\"xml\",\"csv\",\"data-conversion\"],\"owner\":{\"account_id\":64,\"reputation\":1325,\"user_id\":78,\"user_type\":\"registered\",\"accept_rate\":75,\"profile_image\":\"https://www.gravatar.com/avatar/90f0ea16725b9f41c2121b40e2cc45cd?s=256&d=identicon&r=PG\",\"display_name\":\"A Salim\",\"link\":\"https://stackoverflow.com/users/78/a-salim\"},\"is_answered\":true,\"view_count\":81218,\"protected_date\":1433355035,\"closed_date\":1543288543,\"accepted_answer_id\":183,\"answer_count\":16,\"score\":121,\"last_activity_date\":1590400952,\"creation_date\":1217606932,\"last_edit_date\":1445938449,\"question_id\":123,\"link\":\"https://stackoverflow.com/questions/123/java-lib-or-app-to-convert-csv-to-xml-file\",\"closed_reason\":\"Not suitable for this site\",\"title\":\"Java lib or app to convert CSV to XML file?\"}],\"has_more\":false,\"quota_max\":300,\"quota_remaining\":231}")));


        // preconditions
        final Long testChat = 8L;
        linkRepository.add(TEST_LINK);

        var res = chatLinkBoundRepository.remove(new ChatLinkBound(
            testChat, TEST_LINK
        ));

        assertThat(res).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairLinkNotExists() {
        // preconditions
        final Long testChat = 9L;
        var res = chatLinkBoundRepository.remove(new ChatLinkBound(testChat, TEST_LINK));
        assertThat(res).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairLinkAndChatNotExists() {
        // preconditions
        final Long testChat = 10L;

        var res = chatLinkBoundRepository.remove(new ChatLinkBound(
            testChat,
            TEST_LINK
        ));

        assertThat(res).isEqualTo(0);
    }
}
