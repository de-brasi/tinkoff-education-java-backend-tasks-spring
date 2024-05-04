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
        final int createdRowsCount = chatRepository.add(tgChatId);
        final int expectedCreatedRowsCount = 1;

        if (createdRowsCount != expectedCreatedRowsCount) {
            throw new ReRegistrationException();
        }
    }

    @Override
    public void unregister(long tgChatId) {
        final int removedRecordsCount = chatRepository.remove(tgChatId);
        final int expectedRemovedRecordsCount = 1;

        if (removedRecordsCount != expectedRemovedRecordsCount) {
            throw new ChatIdNotExistsException();
        }
    }
}
