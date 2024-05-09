package edu.java.data.domain.repositories.jpa.implementations;

import edu.java.data.domain.repositories.jpa.entities.TelegramChat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JpaTelegramChatRepository extends JpaRepository<TelegramChat, Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO telegram_chat (chat_id) VALUES (:chatId) ON CONFLICT DO NOTHING ", nativeQuery = true)
    void saveByChatId(Long chatId);

    @Transactional(readOnly = true)
    Optional<TelegramChat> getTelegramChatByChatId(Long chatId);

    @Transactional
    void removeByChatId(Long id);
}
