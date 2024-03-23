package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.TelegramChat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaTelegramChatRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void add(Long id) {
        try {
            TelegramChat chat = new TelegramChat();
            chat.setChatId(id);
            entityManager.persist(chat);
            entityManager.flush();
        } catch (PersistenceException ignored) {
        }
    }

    @Transactional
    public TelegramChat get(Long id) {
        try {
            return entityManager.createQuery("SELECT chat FROM TelegramChat chat WHERE chat.chatId = :id", TelegramChat.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public void remove(Long id) {
        TelegramChat chat =
            entityManager.createQuery("SELECT chat FROM TelegramChat chat WHERE chat.chatId = :id", TelegramChat.class)
                .setParameter("id", id)
                .getSingleResult();
        entityManager.remove(chat);
    }
}
