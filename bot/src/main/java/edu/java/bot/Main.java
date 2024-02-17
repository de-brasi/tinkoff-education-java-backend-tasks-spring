package edu.java.bot;

import com.pengrad.telegrambot.ExceptionHandler;
import edu.java.bot.repository.implementations.UserRepositoryMockImpl;
import edu.java.bot.repository.interfaces.UsersRepository;
import edu.java.bot.services.LinkTrackerObserver;
import edu.java.bot.services.LinkTrackerErrorHandler;
import edu.java.bot.services.TelegramBotWrapper;
import edu.java.bot.core.commands.ReadyToUseCommands;

public class Main {
    public static void main(String[] args) {
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

        System.out.println("i am here");
    }
}
