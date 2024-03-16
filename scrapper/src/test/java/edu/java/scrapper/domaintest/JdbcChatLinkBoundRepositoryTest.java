package edu.java.scrapper.domaintest;

import edu.common.exceptions.ChatIdNotExistsException;
import edu.common.exceptions.IncorrectRequestException;
import edu.java.domain.JdbcChatLinkBoundRepository;
import edu.java.domain.JdbcLinkRepository;
import edu.java.domain.JdbcTelegramChatRepository;
import edu.java.entities.ChatLinkBound;
import edu.java.entities.Link;
import edu.java.entities.TelegramChat;
import edu.java.scrapper.IntegrationTest;
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
        String allFromChatTableQuery = "select * from telegram_chat";
        String allFromLinksTableQuery = "select * from links";
        String allFromBoundsTableQuery = "select * from track_info";

        int chatRecordsCount = jdbcTemplate.queryForObject(allFromChatTableQuery, Integer.class);
        int linksRecordsCount = jdbcTemplate.queryForObject(allFromLinksTableQuery, Integer.class);
        int trackingRecordsCount = jdbcTemplate.queryForObject(allFromBoundsTableQuery, Integer.class);

        assertThat(chatRecordsCount).isEqualTo(0);
        assertThat(linksRecordsCount).isEqualTo(0);
        assertThat(trackingRecordsCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void addCorrectPair() throws MalformedURLException {
        // preconditions
        final Link testLink = new Link(URI.create("https://example/link"));
        final TelegramChat testChat = new TelegramChat(1L);
        telegramChatRepository.add(testChat);
        linkRepository.add(testLink);

        // bound
        boolean res = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
        assertThat(res).isTrue();

        // checkout records count
        String query =
            "select * " +
            "from track_info " +
            "where link_id = ((select id as tg_chat_id from telegram_chat where chat_id = ?)) " +
            "and telegram_chat_id = (select id as link_id from links where url = ?);";
        int rowCount = jdbcTemplate.queryForObject(
            query,
            Integer.class,
            testChat.id() , testLink.uri().toURL().toString()
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addCorrectPairTwice() throws MalformedURLException {
        // preconditions
        final Link testLink = new Link(URI.create("https://example/link"));
        final TelegramChat testChat = new TelegramChat(1L);
        telegramChatRepository.add(testChat);
        linkRepository.add(testLink);

        // bound
        boolean res = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
        assertThat(res).isTrue();

        boolean secondRes = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
        assertThat(secondRes).isFalse();

        // checkout records count
        String query =
            "select * " +
                "from track_info " +
                "where link_id = ((select id as tg_chat_id from telegram_chat where chat_id = ?)) " +
                "and telegram_chat_id = (select id as link_id from links where url = ?);";
        int rowCount = jdbcTemplate.queryForObject(
            query,
            Integer.class,
            testChat.id() , testLink.uri().toURL().toString()
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairTgChatRequired() {
        // preconditions
        final Link testLink = new Link(URI.create("https://example/link"));
        final TelegramChat testChat = new TelegramChat(1L);
        linkRepository.add(testLink);

        assertThatThrownBy(() -> chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink))).isInstanceOf(
            ChatIdNotExistsException.class);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairLinkRequiredTestLinkAddedToTable() throws MalformedURLException {
        // preconditions
        final TelegramChat testChat = new TelegramChat(1L);
        final Link testLink = new Link(URI.create("https://example/link"));
        telegramChatRepository.add(testChat);

        // bound
        chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));

        // check link actually added
        String queryToCheckTestLink = "select * from links where url = ?";
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
    void addInCorrectPairLinkRequiredTestBoundingInformationCreated() throws MalformedURLException {
        // preconditions
        final TelegramChat testChat = new TelegramChat(1L);
        final Link testLink = new Link(URI.create("https://example/link"));
        telegramChatRepository.add(testChat);

        // bound
        boolean res = chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink));
        assertThat(res).isTrue();

        // check link actually added
        String queryToCheckBoundingInfo =
            "select * " +
            "from track_info " +
            "where link_id = ((select id as tg_chat_id from telegram_chat where chat_id = ?)) " +
            "and telegram_chat_id = (select id as link_id from links where url = ?);";
        int rowCount = jdbcTemplate.queryForObject(
            queryToCheckBoundingInfo,
            Integer.class,
            testChat.id() , testLink.uri().toURL().toString()
        );
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addInCorrectPairTgChatAndLinkRequired() {
        // preconditions: empty
        final Link testLink = new Link(URI.create("https://example/link"));
        final TelegramChat testChat = new TelegramChat(1L);

        assertThatThrownBy(() -> chatLinkBoundRepository.add(new ChatLinkBound(testChat, testLink))).isInstanceOf(
            ChatIdNotExistsException.class);
    }

    @Test
    @Transactional
    @Rollback
    void removeCorrectPair() throws MalformedURLException {
        // preconditions
        final Link testLink = new Link(URI.create("https://example/link"));
        final TelegramChat testChat = new TelegramChat(1L);
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
            "select * " +
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
        final Link testLink = new Link(URI.create("https://example/link"));
        final TelegramChat testChat = new TelegramChat(1L);
        linkRepository.add(testLink);

        assertThatThrownBy(() -> chatLinkBoundRepository.remove(new ChatLinkBound(testChat, testLink))).isInstanceOf(
            ChatIdNotExistsException.class);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairLinkNotExists() {
        // preconditions
        final Link testLink = new Link(URI.create("https://example/link"));
        final TelegramChat testChat = new TelegramChat(1L);
        telegramChatRepository.add(testChat);

        // TODO:
        //  надо для слоя сервиса создать свои ошибки,
        //  которые будет перехватывать контроллер и пробрасывать уже ошибку IncorrectRequestException
//        assertThatThrownBy(() -> chatLinkBoundRepository.remove(new ChatLinkBound(testChat, testLink))).isInstanceOf(IncorrectRequestException.class);

        // todo: намеренно дурацкий класс ошибки чтоб тест упал и я не забыл про это
        assertThatThrownBy(() -> chatLinkBoundRepository.remove(new ChatLinkBound(testChat, testLink))).isInstanceOf(
            Link.class);
    }

    @Test
    @Transactional
    @Rollback
    void removeInCorrectPairLinkAndChatNotExists() {
        // preconditions
        final Link testLink = new Link(URI.create("https://example/link"));
        final TelegramChat testChat = new TelegramChat(1L);

        // if no link and chat - firstly need to notify that chat not exists
        assertThatThrownBy(() -> chatLinkBoundRepository.remove(new ChatLinkBound(testChat, testLink))).isInstanceOf(
            ChatIdNotExistsException.class);
    }
}
