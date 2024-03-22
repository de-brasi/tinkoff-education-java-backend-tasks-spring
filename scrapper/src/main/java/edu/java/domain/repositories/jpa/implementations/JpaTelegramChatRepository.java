package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.TelegramChat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

public class JpaTelegramChatRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void add(Long id) {
        TelegramChat chat = new TelegramChat();
        chat.setChatId(id);
        entityManager.persist(chat);
        entityManager.flush();
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
