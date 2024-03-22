package edu.java.scrapper.domaintest;

import edu.java.domain.repositories.jdbc.JdbcChatLinkBoundRepository;
import edu.java.domain.repositories.jdbc.JdbcLinkRepository;
import edu.java.domain.repositories.jdbc.JdbcTelegramChatRepository;
import edu.java.domain.exceptions.NoExpectedEntityInDataBaseException;
import edu.java.domain.entities.ChatLinkBound;
import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
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
import java.net.URI;
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
    void addCorrectPair() throws MalformedURLException {
        // preconditions
        final Link testLink = new Link(URI.create("https://stackoverflow.com/questions/3838242/minimum-date-in-java"));
        final TelegramChat testChat = new TelegramChat(1L);
        telegramChatRepository.add(testChat);
        linkRepository.add(testLink);

        // bound
        boolean res = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
        assertThat(res).isTrue();

        // checkout records count
        String query =
            "select count(*) " +
            "from track_info " +
            "where link_id = (select id as link_id from links where url = ?) " +
            "and telegram_chat_id = (select id as tg_chat_id from telegram_chat where chat_id = ?);";

        int rowCount = jdbcTemplate.queryForObject(
            query,
            Integer.class,
            testLink.uri().toURL().toString(), testChat.id()
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addCorrectPairTwice() throws MalformedURLException {
        // preconditions
        final Link testLink = new Link(URI.create("https://stackoverflow.com/questions/3838242/minimum-date-in-java"));
        final TelegramChat testChat = new TelegramChat(2L);
        telegramChatRepository.add(testChat);
        linkRepository.add(testLink);

        // bound
        boolean res = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
        assertThat(res).isTrue();

        boolean secondRes = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
        assertThat(secondRes).isFalse();

        // checkout records count
        String query =
            "select count(*) " +
                "from track_info " +
                "where link_id = (select id as link_id from links where url = ?) " +
                "and telegram_chat_id = (select id as tg_chat_id from telegram_chat where chat_id = ?);";

        int rowCount = jdbcTemplate.queryForObject(
            query,
            Integer.class,
            testLink.uri().toURL().toString(), testChat.id()
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addIncorrectPairTgChatRequired() {
        // preconditions
        final Link testLink = new Link(URI.create("https://stackoverflow.com/questions/3838242/minimum-date-in-java"));
        final TelegramChat testChat = new TelegramChat(3L);
        linkRepository.add(testLink);

        assertThatThrownBy(() -> chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink))).isInstanceOf(
            NoExpectedEntityInDataBaseException.class);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairLinkRequiredCheckLinkActuallyAddedToTable() throws MalformedURLException {
        // preconditions
        final TelegramChat testChat = new TelegramChat(4L);
        final Link testLink = new Link(URI.create("https://stackoverflow.com/questions/3838242/minimum-date-in-java"));
        telegramChatRepository.add(testChat);

        // bound
        chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));

        // check link actually added
        String queryToCheckTestLink = "select count(*) from links where url = ?";
        int rowCount = jdbcTemplate.queryForObject(
            queryToCheckTestLink,
            Integer.class,
            testLink.uri().toURL().toString()
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairLinkRequiredCheckBoundingInformationActuallyCreated() throws MalformedURLException {
        // preconditions
        final TelegramChat testChat = new TelegramChat(5L);
        final Link testLink = new Link(URI.create("https://stackoverflow.com/questions/3838242/minimum-date-in-java"));
        telegramChatRepository.add(testChat);

        // bound
        boolean res = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
        assertThat(res).isTrue();

        // check link actually added
        String queryToCheckBoundingInfo =
            "select count(*) " +
            "from track_info " +
            "where link_id = (select id as link_id from links where url = ?) " +
            "and telegram_chat_id = (select id as tg_chat_id from telegram_chat where chat_id = ?);";

        int rowCount = jdbcTemplate.queryForObject(
            queryToCheckBoundingInfo,
            Integer.class,
            testLink.uri().toURL().toString(), testChat.id()
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairTgChatAndLinkRequired() {
        // preconditions: empty
        final Link testLink = new Link(URI.create("https://example/link6"));
        final TelegramChat testChat = new TelegramChat(6L);

        assertThatThrownBy(() -> chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink))).isInstanceOf(
            NoExpectedEntityInDataBaseException.class);
    }

    @Test
    @Transactional
    @Rollback
    void removeCorrectPair() throws MalformedURLException {
        // preconditions
        final Link testLink = new Link(URI.create("https://stackoverflow.com/questions/3838242/minimum-date-in-java"));
        final TelegramChat testChat = new TelegramChat(7L);
        telegramChatRepository.add(testChat);
        linkRepository.add(testLink);
        chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));

        // remove
        final ChatLinkBound removedExpected = new ChatLinkBound(testChat, testLink);
        final ChatLinkBound removedActual = chatLinkBoundRepository.remove(removedExpected);

        // checkout equivalency
        assertThat(removedExpected).isEqualTo(removedActual);

        // checkout records count
        String query =
            "select count(*) " +
                "from track_info " +
                "where link_id = ((select id as tg_chat_id from telegram_chat where chat_id = ?)) " +
                "and telegram_chat_id = (select id as link_id from links where url = ?);";
        int rowCount = jdbcTemplate.queryForObject(
            query,
            Integer.class,
            testChat.id() , testLink.uri().toURL().toString()
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairTgChatNotExists() {
        // preconditions
        final Link testLink = new Link(URI.create("https://stackoverflow.com/questions/3838242/minimum-date-in-java"));
        final TelegramChat testChat = new TelegramChat(8L);
        linkRepository.add(testLink);

        assertThatThrownBy(() -> chatLinkBoundRepository.remove(new ChatLinkBound(testChat, testLink))).isInstanceOf(
            NoExpectedEntityInDataBaseException.class);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairLinkNotExists() {
        // preconditions
        final Link testLink = new Link(URI.create("https://example/link9"));
        final TelegramChat testChat = new TelegramChat(9L);
        var res = telegramChatRepository.remove(testChat);
        assertThat(res).isEqualTo(null);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairLinkAndChatNotExists() {
        // preconditions
        final Link testLink = new Link(URI.create("https://example/link10"));
        final TelegramChat testChat = new TelegramChat(10L);

        // if no link and chat - firstly need to notify that chat not exists
        assertThatThrownBy(() -> chatLinkBoundRepository.remove(new ChatLinkBound(testChat, testLink))).isInstanceOf(
            NoExpectedEntityInDataBaseException.class);
    }
}
