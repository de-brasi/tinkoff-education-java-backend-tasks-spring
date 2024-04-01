package edu.java.bot.services;

import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LinkTrackerErrorHandler implements ExceptionHandler {
    @Override
    public void onException(TelegramException e) {
        if (e.response() != null) {
            e.response().errorCode();
            e.response().description();
        } else {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }
}
