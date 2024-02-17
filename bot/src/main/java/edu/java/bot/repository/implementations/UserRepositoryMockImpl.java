package edu.java.bot.repository.implementations;

import edu.java.bot.repository.interfaces.UsersRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;

public class UserRepositoryMockImpl implements UsersRepository {
    @Override
    public void storeLinksForUser(Long userTelegramId, List<String> links) {
        // do nothing
        LOGGER.info(String.format("For user with Telegram id %s store links: %s", userTelegramId, links));
    }

    @Override
    public List<String> getLinksForUser(Long userTelegramId) {
        List<String> exampleLinks = List.of("test-link-1", "test-link-2", "test-link-3");
        LOGGER.info(String.format("For user with Telegram id %s get links: " + exampleLinks, userTelegramId));
        return exampleLinks;
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
