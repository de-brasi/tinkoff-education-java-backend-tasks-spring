package edu.java.bot.services;

import com.pengrad.telegrambot.ExceptionHandler;
import edu.java.bot.core.commands.ReadyToUseCommands;
import edu.java.bot.repository.interfaces.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelegramBotService {

    public TelegramBotService(@Autowired TelegramBotWrapper bot, @Autowired UsersRepository usersRepository) {
        LinkTrackerObserver listener = new LinkTrackerObserver(bot);
        listener.setCommands(
            ReadyToUseCommands.unexpectedCommand(),
            ReadyToUseCommands.help(),
            ReadyToUseCommands.track(usersRepository),
            ReadyToUseCommands.untrack(usersRepository),
            ReadyToUseCommands.list(usersRepository)
        );

        ExceptionHandler errorHandler = new LinkTrackerErrorHandler();

        bot.setUpdatesListener(listener, errorHandler);
    }
}
