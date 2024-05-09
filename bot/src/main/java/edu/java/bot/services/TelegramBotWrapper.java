package edu.java.bot.services;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
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
            log.info("Failure when try to send message {} to chat {}", message, chatId);
        }
    }

    public void sendMarkdownV2Message(Long chatId, String markdownLayout) {
        assert chatId != null;
        assert markdownLayout != null;

        SendMessage request = new SendMessage(chatId, markdownLayout).parseMode(ParseMode.MarkdownV2);
        BaseResponse response = this.execute(request);

        if (!response.isOk()) {
            log.info(
                """
                    Failure when try to send MarkdownV2 message {} to chat {}.
                    Error code: {}
                    Description: {}
                    """,
                markdownLayout,
                chatId,
                response.errorCode(),
                response.description()
            );
        }
    }
}
