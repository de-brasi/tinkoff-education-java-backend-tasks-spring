package edu.java.services.jdbc;

import edu.common.exceptions.ChatIdNotExistsException;
import edu.common.exceptions.ReRegistrationException;
import edu.java.domain.repositories.BaseEntityRepository;
import edu.java.domain.repositories.JdbcTelegramChatRepository;
import edu.java.domain.entities.TelegramChat;
import edu.java.services.interfaces.TgChatService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class JdbcTgChatService implements TgChatService {
    private final BaseEntityRepository<TelegramChat> chatRepository;

    public JdbcTgChatService(@Autowired JdbcTelegramChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public void register(long tgChatId) {
        LOGGER.info(
            "ТГ_СЕРВИС: Регистрация пользователя с id %d".formatted(tgChatId)
        );

        try {
            // todo:
            //  разграничивать когда вернулось false из-за повторного добавления,
            //  а когда - из-за внутренней ошибки;
            //  для этого как то переработать цепочку обработки ошибок в JdbcTelegramChatRepository::add
            boolean successRegistration = chatRepository.add(new TelegramChat(tgChatId));
            if (!successRegistration) {
                throw new ReRegistrationException();
            }
        } catch (DataAccessException e) {
            // todo:
            //  решить проблему с тем что ошибка DataAccessException
            //  не перехватывается в самом методе репозитория!
            final String message = "Проблема с необработанной ошибкой DataAccessException!";
            LOGGER.info(message);
            throw new RuntimeException(message);
        }
    }

    @Override
    public void unregister(long tgChatId) {
        LOGGER.info(
            "ТГ_СЕРВИС: Удаление регистрации пользователя с id %d".formatted(tgChatId)
        );

        final TelegramChat deletedChat = new TelegramChat(tgChatId);

        TelegramChat actuallyDeleted = chatRepository.remove(deletedChat);
        LOGGER.info(actuallyDeleted);
        if (actuallyDeleted == null) {
            throw new ChatIdNotExistsException();
        }
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
