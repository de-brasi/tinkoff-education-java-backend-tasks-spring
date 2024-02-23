package edu.java.bot.services;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import edu.java.bot.customexceptions.NullTelegramTokenException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TelegramBotWrapper extends TelegramBot {
    public TelegramBotWrapper(String botToken) {
        super(botToken);
    }

    public static TelegramBotWrapper createBotWithTokenFromEnv(String variableName) throws NullTelegramTokenException {
        String token = System.getenv(variableName);

        if (token == null) {
            throw new NullTelegramTokenException(String.format(
                "Environment variable with name %s not found! "
                    + "Make sure you set the Telegram token value in the environment variables by this name",
                variableName
            ));
        }

        return new TelegramBotWrapper(token);
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
