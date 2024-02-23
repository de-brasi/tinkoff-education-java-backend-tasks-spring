package edu.java.bot;

import com.pengrad.telegrambot.ExceptionHandler;
import edu.java.bot.core.commands.ReadyToUseCommands;
import edu.java.bot.customexceptions.NullTelegramTokenException;
import edu.java.bot.repository.implementations.UserRepositoryMockImpl;
import edu.java.bot.repository.interfaces.UsersRepository;
import edu.java.bot.services.LinkTrackerErrorHandler;
import edu.java.bot.services.LinkTrackerObserver;
import edu.java.bot.services.TelegramBotWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private Main() {}

    public static void main(String[] args) throws NullTelegramTokenException {
        TelegramBotWrapper bot = TelegramBotWrapper.createBotWithTokenFromEnv("TELEGRAM_TOKEN");

        UsersRepository mockRepo = new UserRepositoryMockImpl();

        LinkTrackerObserver listener = new LinkTrackerObserver(bot);
        listener.setCommands(
            ReadyToUseCommands.unexpectedCommand(),
            ReadyToUseCommands.help(),
            ReadyToUseCommands.track(mockRepo),
            ReadyToUseCommands.untrack(mockRepo),
            ReadyToUseCommands.list(mockRepo)
        );

        ExceptionHandler errorHandler = new LinkTrackerErrorHandler();

        bot.setUpdatesListener(listener, errorHandler);

        LOGGER.info("Bot starts to listen");
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
