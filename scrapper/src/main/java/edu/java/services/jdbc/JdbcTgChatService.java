package edu.java.services.jdbc;

import edu.common.exceptions.ChatIdNotExistsException;
import edu.common.exceptions.ReRegistrationException;
import edu.java.domain.repositories.jdbc.JdbcTelegramChatRepository;
import edu.java.services.interfaces.TgChatService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JdbcTgChatService implements TgChatService {

    private final JdbcTelegramChatRepository chatRepository;

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
