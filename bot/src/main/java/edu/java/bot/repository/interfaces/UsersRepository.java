package edu.java.bot.repository.interfaces;

import java.util.List;

public interface UsersRepository {
    void storeLinksForUser(Long userTelegramId, List<String> links);

    List<String> getLinksForUser(Long userTelegramId);

    void deleteLinksForUser(Long userTelegramId, List<String> links);
}
