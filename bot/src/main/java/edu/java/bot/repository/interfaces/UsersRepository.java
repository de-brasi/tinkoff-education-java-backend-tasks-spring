package edu.java.bot.repository.interfaces;

import java.util.List;

public interface UsersRepository {
    public void storeLinksForUser(Long userTelegramId, List<String> links);

    public List<String> getLinksForUser(Long userTelegramId);

    public void deleteLinksForUser(Long userTelegramId, List<String> links);
}
