package edu.java.services.jpa;

import edu.java.domain.repositories.jpa.implementations.JpaTelegramChatRepository;
import edu.java.services.interfaces.TgChatService;
import org.springframework.transaction.annotation.Transactional;

public class JpaTgChatService implements TgChatService {

    public JpaTgChatService(JpaTelegramChatRepository jpaTelegramChatRepository) {
        this.chatRepository = jpaTelegramChatRepository;
    }

    private final JpaTelegramChatRepository chatRepository;

    @Override
    @Transactional
    public void register(long tgChatId) {
        chatRepository.saveByChatId(tgChatId);
    }

    @Override
    @Transactional
    public void unregister(long tgChatId) {
        chatRepository.removeByChatId(tgChatId);
    }
}
