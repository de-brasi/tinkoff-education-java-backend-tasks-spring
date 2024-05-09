package edu.java.scrapper.domaintest.jdbc;

import edu.java.configuration.KafkaConfig;
import edu.java.data.domain.repositories.jdbc.JdbcTelegramChatRepository;
import edu.java.scrapper.IntegrationTest;
import edu.java.data.services.jpa.JpaLinkUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "app.database-access-type=jdbc",
    "third-party-web-clients.github-properties.timeout-in-milliseconds=1000",
    "third-party-web-clients.stackoverflow-properties.timeout-in-milliseconds=1000",
    "app.use-queue=false",
    "app.topic.name='some'",
    "app.topic.partitions-count=1",
    "app.topic.replicas-count=2",
    "app.scheduler.enable=false",
    "app.scheduler.force-check-delay=1000",
    "app.scheduler.interval=1000"
})
public class JdbcTelegramChatRepositoryTest extends IntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcTelegramChatRepository telegramChatRepository;

    @MockBean
    KafkaConfig kafkaConfig;

    @MockBean
    JpaLinkUpdater jpaLinkUpdater;

    @Test
    @Transactional
    @Rollback
    void addChatTest() {
        final Long testChat = 1L;

        final int res = telegramChatRepository.add(testChat);
        assertThat(res).isEqualTo(1);

        String query = "SELECT COUNT(*) FROM telegram_chat WHERE chat_id = ?";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, testChat);
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addEqualChatsTest() {
        final Long testChat = 1L;

        final int res = telegramChatRepository.add(testChat);
        assertThat(res).isEqualTo(1);

        final int secondTryResult = telegramChatRepository.add(testChat);
        assertThat(secondTryResult).isEqualTo(0);

        String query = "SELECT COUNT(*) FROM telegram_chat WHERE chat_id = ?";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, testChat);
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void removeChatTest() {
        final Long testChat = 1L;

        // add record
        final int addResult = telegramChatRepository.add(testChat);
        assertThat(addResult).isEqualTo(1);

        // clear record
        final int removeResult = telegramChatRepository.remove(testChat);
        assertThat(removeResult).isEqualTo(1);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM telegram_chat WHERE id = ?",
            Integer.class,
            testChat
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeChatTwiceTest() {
        final Long testChat = 1L;

        // add record
        final int addResult = telegramChatRepository.add(testChat);
        assertThat(addResult).isEqualTo(1);

        // clear record once
        final int firstRemoveResult = telegramChatRepository.remove(testChat);
        assertThat(firstRemoveResult).isEqualTo(1);

        // clear record twice
        int secondRemoveResult = telegramChatRepository.remove(testChat);
        assertThat(secondRemoveResult).isEqualTo(0);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM telegram_chat WHERE id = ?",
            Integer.class,
            testChat
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void getAllChatsTest() {
        final Long testChat1 = 1L;
        final Long testChat2 = 2L;
        final Long testChat3 = 3L;

        // add records
        final int addResult1 = telegramChatRepository.add(testChat1);
        assertThat(addResult1).isEqualTo(1);
        final int addResult2 = telegramChatRepository.add(testChat2);
        assertThat(addResult2).isEqualTo(1);
        final int addResult3 = telegramChatRepository.add(testChat3);
        assertThat(addResult3).isEqualTo(1);

        // check result contains all
        Collection<Long> gettingAll = telegramChatRepository.findAll();
        assertThat(gettingAll).containsExactlyInAnyOrder(testChat1, testChat2, testChat3);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM telegram_chat WHERE chat_id IN (?, ?, ?)",
            Integer.class,
            testChat1, testChat2, testChat3
        );
        assertThat(rowCount).isEqualTo(3);
    }
}
