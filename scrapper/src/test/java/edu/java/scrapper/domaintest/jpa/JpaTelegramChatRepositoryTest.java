package edu.java.scrapper.domaintest.jpa;

import edu.java.domain.repositories.jpa.implementations.JpaTelegramChatRepository;
import edu.java.scrapper.IntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JpaTelegramChatRepositoryTest extends IntegrationTest {
    @Autowired JpaTelegramChatRepository telegramChatRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    void addTest() {
        telegramChatRepository.add(1L);

        Long suchRowCount = entityManager
            .createQuery("SELECT count(*) from TelegramChat chat where chat.chatId = :chatId", Long.class)
            .setParameter("chatId", 1L)
            .getSingleResult();
        assertThat(suchRowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() {
        telegramChatRepository.add(1L);
        telegramChatRepository.remove(1L);

        Long suchRowCount = entityManager
            .createQuery("SELECT count(*) from TelegramChat chat where chat.chatId = :chatId", Long.class)
            .setParameter("chatId", 1L)
            .getSingleResult();
        assertThat(suchRowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void getTest() {
        telegramChatRepository.add(1L);
        var got = telegramChatRepository.get(1L);

        assertThat(got.getChatId()).isEqualTo(1L);
    }
}