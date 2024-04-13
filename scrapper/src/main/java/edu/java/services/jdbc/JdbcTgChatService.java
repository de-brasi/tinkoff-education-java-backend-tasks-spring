package edu.java.services.jdbc;

import edu.common.exceptions.ChatIdNotExistsException;
import edu.common.exceptions.ReRegistrationException;
import edu.java.domain.BaseEntityRepository;
import edu.java.domain.JdbcTelegramChatRepository;
import edu.java.services.interfaces.TgChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JdbcTgChatService implements TgChatService {
    private final BaseEntityRepository<Long> chatRepository;

    public JdbcTgChatService(@Autowired JdbcTelegramChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public void register(long tgChatId) {
        boolean successRegistration = chatRepository.add(tgChatId);
        if (!successRegistration) {
            throw new ReRegistrationException();
        }
    }

    @Override
    public void unregister(long tgChatId) {
        Long actuallyDeleted = chatRepository.remove(tgChatId);
        if (actuallyDeleted == null) {
            throw new ChatIdNotExistsException();
        }
    }
}
