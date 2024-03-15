package edu.java.scrapper.domaintest;

import edu.java.domain.JdbcTelegramChatRepository;
import edu.java.domain.entities.TelegramChat;
import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(
    properties = {
        // exclude nothing - for correct jdbc.DataSourceAutoConfiguration working till main property file exclude it
        "spring.autoconfigure.exclude="
    }
)
public class JdbcTelegramChatRepositoryTest extends IntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcTelegramChatRepository telegramChatRepository;

    @Test
    @Transactional
    @Rollback
    void addChatTest() {
        final TelegramChat testChat = new TelegramChat(1L);

        final boolean res = telegramChatRepository.add(testChat);
        assertThat(res).isTrue();

        String query = "SELECT COUNT(*) FROM telegram_chat WHERE chat_id = ?";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, testChat.id());
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addEqualChatsTest() {
        final TelegramChat testChat = new TelegramChat(1L);

        final boolean res = telegramChatRepository.add(testChat);
        assertThat(res).isTrue();

        // TODO: какого черта не отлавливается ошибка в "catch" внутри метода JdbcLinkRepository::add?
        final boolean resOneMore = telegramChatRepository.add(testChat);
        assertThat(resOneMore).isFalse();

        String query = "SELECT COUNT(*) FROM telegram_chat WHERE chat_id = ?";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, testChat.id());
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void removeChatTest() {
        final TelegramChat testChat = new TelegramChat(1L);

        // add record
        final boolean addResult = telegramChatRepository.add(testChat);
        assertThat(addResult).isTrue();

        // clear record
        final TelegramChat removeResult = telegramChatRepository.remove(testChat);
        assertThat(removeResult).isEqualTo(testChat);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM telegram_chat WHERE id = ?",
            Integer.class,
            testChat.id()
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeChatTwiceTest() {
        final TelegramChat testChat = new TelegramChat(1L);

        // add record
        final boolean addResult = telegramChatRepository.add(testChat);
        assertThat(addResult).isTrue();

        // clear record once
        final TelegramChat firstRemoveResult = telegramChatRepository.remove(testChat);
        assertThat(firstRemoveResult).isEqualTo(testChat);

        // clear record twice
        final TelegramChat secondRemoveResult = telegramChatRepository.remove(testChat);
        assertThat(secondRemoveResult).isNull();

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM telegram_chat WHERE id = ?",
            Integer.class,
            testChat.id()
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void getAllChatsTest() {
        final TelegramChat testChat1 = new TelegramChat(1L);
        final TelegramChat testChat2 = new TelegramChat(2L);
        final TelegramChat testChat3 = new TelegramChat(3L);

        // add records
        final boolean addResult1 = telegramChatRepository.add(testChat1);
        assertThat(addResult1).isTrue();
        final boolean addResult2 = telegramChatRepository.add(testChat2);
        assertThat(addResult2).isTrue();
        final boolean addResult3 = telegramChatRepository.add(testChat3);
        assertThat(addResult3).isTrue();

        // check result contains all
        Collection<TelegramChat> gettingAll = telegramChatRepository.findAll();
        assertThat(gettingAll).containsExactlyInAnyOrder(testChat1, testChat2, testChat3);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM telegram_chat WHERE chat_id IN (?, ?, ?)",
            Integer.class,
            testChat1.id(),
            testChat2.id(),
            testChat3.id()
        );
        assertThat(rowCount).isEqualTo(3);
    }
}
