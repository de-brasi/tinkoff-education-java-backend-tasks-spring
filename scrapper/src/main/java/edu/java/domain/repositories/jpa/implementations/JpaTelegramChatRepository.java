package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.TelegramChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface JpaTelegramChatRepository extends JpaRepository<TelegramChat, Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO telegram_chat (chat_id) VALUES (:chatId)", nativeQuery = true)
    void saveByChatId(Long chatId);

    @Transactional(readOnly = true)
    Optional<TelegramChat> getTelegramChatByChatId(Long chatId);

    @Transactional
    void removeByChatId(Long id);
}
