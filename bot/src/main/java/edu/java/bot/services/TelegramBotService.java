package edu.java.bot.services;

import com.pengrad.telegrambot.ExceptionHandler;
import edu.java.bot.core.commands.TelegramBotCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TelegramBotService {

    public TelegramBotService(
        @Autowired
        TelegramBotWrapper bot,

        @Autowired
        @Qualifier("unexpectedCommand")
        TelegramBotCommand unexpected,

        @Autowired
        @Qualifier("commandStart")
        TelegramBotCommand start,

        @Autowired
        @Qualifier("commandHelp")
        TelegramBotCommand help,

        @Autowired
        @Qualifier("commandTrack")
        TelegramBotCommand track,

        @Autowired
        @Qualifier("commandUntrack")
        TelegramBotCommand untrack,

        @Autowired
        @Qualifier("commandList")
        TelegramBotCommand list
    ) {

        LinkTrackerObserver listener = new LinkTrackerObserver(bot);
        listener.setCommands(
            unexpected, start,
            help, track,
            untrack, list
        );
        ExceptionHandler errorHandler = new LinkTrackerErrorHandler();
        bot.setUpdatesListener(listener, errorHandler);

    }
}
