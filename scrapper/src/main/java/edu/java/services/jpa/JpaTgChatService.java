package edu.java.services.jpa;

import edu.java.domain.repositories.jpa.implementations.JpaTelegramChatRepository;
import edu.java.services.interfaces.TgChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaTgChatService implements TgChatService {

    public JpaTgChatService(@Autowired JpaTelegramChatRepository jpaTelegramChatRepository) {
        this.chatRepository = jpaTelegramChatRepository;
    }

    private final JpaTelegramChatRepository chatRepository;

    @Override
    @Transactional
    public void register(long tgChatId) {
        chatRepository.add(tgChatId);
    }

    @Override
    @Transactional
    public void unregister(long tgChatId) {
        chatRepository.remove(tgChatId);
    }
}
