package edu.java.bot.services;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;

public class TelegramBotWrapper extends TelegramBot {
    public TelegramBotWrapper(String botToken) {
        super(botToken);
    }

    public static TelegramBotWrapper createBotWithTokenFromEnv(String variableName) {
        String token = System.getenv(variableName);

        if (token == null) {
            throw new RuntimeException(String.format(
                "Environment variable with name %s not found! " +
                    "Make sure you set the Telegram token value in the environment variables by this name",
                variableName
            ));
        }

        return new TelegramBotWrapper(token);
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
