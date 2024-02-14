package edu.java.bot.interfaces;

import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramException;

public class LinkTrackerErrorHandler implements ExceptionHandler {
    @Override
    public void onException(TelegramException e) {
        if (e.response() != null) {
            e.response().errorCode();
            e.response().description();
        } else {
            // TODO: better logging
            e.printStackTrace();
        }
    }
}
