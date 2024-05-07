package edu.java.bot.repository.implementations;

import edu.java.bot.repository.interfaces.UsersRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserRepositoryMockImpl implements UsersRepository {
    @Override
    public void storeLinksForUser(Long userTelegramId, List<String> links) {
        // do nothing
        log.info(String.format("For user with Telegram id %s store links: %s", userTelegramId, links));
    }

    @Override
    public List<String> getLinksForUser(Long userTelegramId) {
        List<String> exampleLinks = List.of("test-link-1", "test-link-2", "test-link-3");
        log.info(String.format("For user with Telegram id %s get links: " + exampleLinks, userTelegramId));
        return exampleLinks;
    }

    @Override
    public void deleteLinksForUser(Long userTelegramId, List<String> links) {
        log.info(String.format("For user with Telegram id %s untrack links: %s", userTelegramId, links));
    }
}
