package edu.java.bot.services;

import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LinkTrackerErrorHandler implements ExceptionHandler {
    @Override
    public void onException(TelegramException e) {
        if (e.response() != null) {
            e.response().errorCode();
            e.response().description();
        } else {
            LOGGER.error(e.getStackTrace());
        }
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
