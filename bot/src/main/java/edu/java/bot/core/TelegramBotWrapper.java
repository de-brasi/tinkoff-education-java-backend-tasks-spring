package edu.java.bot.core;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;

public class TelegramBotWrapper extends TelegramBot {
    public TelegramBotWrapper(String botToken) {
        super(botToken);
    }

    public void sendPlainTextMessage(Long chatId, String message) {
        SendMessage request = new SendMessage(chatId, message);
        BaseResponse response = this.execute(request);

        if (!response.isOk()) {
            // todo: better logging
            System.out.println("Failure when try to send message " + message + " to chat " + chatId);
        }
    }
}
