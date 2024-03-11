package edu.java.bot.services;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TelegramBotWrapper extends TelegramBot {
    public TelegramBotWrapper(@Value("${app.telegram-token}") String botToken) {
        super(botToken);
    }

    public void sendPlainTextMessage(Long chatId, String message) {
        assert chatId != null;
        assert message != null;

        SendMessage request = new SendMessage(chatId, message);
        BaseResponse response = this.execute(request);

        if (!response.isOk()) {
            LOGGER.info("Failure when try to send message " + message + " to chat " + chatId);
        }
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
