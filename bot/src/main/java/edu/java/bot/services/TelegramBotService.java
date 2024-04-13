package edu.java.bot.services;

import com.pengrad.telegrambot.ExceptionHandler;
import edu.java.bot.core.commands.TelegramBotCommand;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("ParameterNumber")
public class TelegramBotService {

    public TelegramBotService(
        TelegramBotWrapper bot,

        @Qualifier("unexpectedCommand")
        TelegramBotCommand unexpected,

        @Qualifier("commandStart")
        TelegramBotCommand start,

        @Qualifier("commandHelp")
        TelegramBotCommand help,

        @Qualifier("commandTrack")
        TelegramBotCommand track,

        @Qualifier("commandUntrack")
        TelegramBotCommand untrack,

        @Qualifier("commandList")
        TelegramBotCommand list,

        MeterRegistry registry
    ) {

        LinkTrackerObserver listener = new LinkTrackerObserver(bot, registry);
        listener.setCommands(
            unexpected, start,
            help, track,
            untrack, list
        );
        ExceptionHandler errorHandler = new LinkTrackerErrorHandler();
        bot.setUpdatesListener(listener, errorHandler);

    }
}
