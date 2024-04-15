package edu.java.scrapper.domaintest;

import edu.java.domain.JdbcChatLinkBoundRepository;
import edu.java.domain.JdbcLinkRepository;
import edu.java.domain.JdbcTelegramChatRepository;
import edu.java.domain.exceptions.NoExpectedEntityInDataBaseException;
import edu.java.domain.entities.ChatLinkBound;
import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.net.MalformedURLException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class JdbcChatLinkBoundRepositoryTest extends IntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcChatLinkBoundRepository chatLinkBoundRepository;

    @Autowired
    private JdbcTelegramChatRepository telegramChatRepository;

    @Autowired
    private JdbcLinkRepository linkRepository;

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
        final String testLink = "https://example/link1";
        final Long testChat = 1L;
        telegramChatRepository.add(testChat);
        linkRepository.add(testLink);

        // bound
        int resCount = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
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
            testLink, testChat
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addCorrectPairTwice() {
        // preconditions
        final String testLink = "https://example/link2";
        final Long testChat = 2L;
        telegramChatRepository.add(testChat);
        linkRepository.add(testLink);

        // bound
        int res = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
        assertThat(res).isEqualTo(1);

        int secondRes = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
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
            testLink, testChat
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addIncorrectPairTgChatRequired() {
        // preconditions
        final String testLink = "https://example/link3";
        final Long testChat = 3L;
        linkRepository.add(testLink);

        assertThatThrownBy(() -> chatLinkBoundRepository.add(new ChatLinkBound(
            testChat, testLink
        ))).isInstanceOf(NoExpectedEntityInDataBaseException.class);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairLinkRequiredCheckLinkActuallyAddedToTable() throws MalformedURLException {
        // preconditions
        final String testLink = "https://example/link4";
        final Long testChat = 4L;
        telegramChatRepository.add(testChat);

        // bound
        chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));

        // check link actually added
        String queryToCheckTestLink = "select count(*) from links where url = ?";
        int rowCount = jdbcTemplate.queryForObject(
            queryToCheckTestLink,
            Integer.class,
            testLink
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairLinkRequiredCheckBoundingInformationActuallyCreated() throws MalformedURLException {
        // preconditions
        final String testLink = "https://example/link5";
        final Long testChat = 5L;
        telegramChatRepository.add(testChat);

        // bound
        int res = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
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
            testLink, testChat
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairTgChatAndLinkRequired() {
        // preconditions: empty
        final String testLink = "https://example/link6";
        final Long testChat = 6L;

        assertThatThrownBy(() -> chatLinkBoundRepository.add(new ChatLinkBound(
            testChat,
            testLink
        ))).isInstanceOf(NoExpectedEntityInDataBaseException.class);
    }

    @Test
    @Transactional
    @Rollback
    void removeCorrectPair() throws MalformedURLException {
        // preconditions
        final String testLink = "https://example/link7";
        final Long testChat = 7L;
        telegramChatRepository.add(testChat);
        linkRepository.add(testLink);
        chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));

        // remove
        final ChatLinkBound removedExpected = new ChatLinkBound(testChat, testLink);
        final int removedRowsCount = chatLinkBoundRepository.remove(removedExpected);

        // checkout equivalency
        assertThat(removedRowsCount).isEqualTo(1);

        // checkout records count
        String query =
            "select count(*) " +
                "from track_info " +
                "where link_id = ((select id as tg_chat_id from telegram_chat where chat_id = ?)) " +
                "and telegram_chat_id = (select id as link_id from links where url = ?);";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, testChat , testLink);
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairTgChatNotExists() throws MalformedURLException {
        // preconditions
        final String testLink = "https://example/link8";
        final Long testChat = 8L;
        linkRepository.add(testLink);

        var res = chatLinkBoundRepository.remove(new ChatLinkBound(
            testChat, testLink
        ));

        assertThat(res).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairLinkNotExists() throws MalformedURLException {
        // preconditions
        final String testLink = "https://example/link9";
        final Long testChat = 9L;
        var res = chatLinkBoundRepository.remove(new ChatLinkBound(testChat, testLink));
        assertThat(res).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairLinkAndChatNotExists() throws MalformedURLException {
        // preconditions
        final String testLink = "https://example/link10";
        final Long testChat = 10L;

        var res = chatLinkBoundRepository.remove(new ChatLinkBound(
            testChat,
            testLink
        ));

        assertThat(res).isEqualTo(0);
    }
}
