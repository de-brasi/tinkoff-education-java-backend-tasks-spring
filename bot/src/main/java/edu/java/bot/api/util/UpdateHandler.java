package edu.java.bot.api.util;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.services.TelegramBotWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateHandler {
    private final TelegramBotWrapper telegramBot;

    public void handleUpdate(LinkUpdateRequest updateRequest) {
        final String messageToClient = "Update in link %s with description: '%s'"
            .formatted(updateRequest.getUrl(), updateRequest.getDescription());

        for (Long chatId : updateRequest.getTgChatIds()) {
            // todo: какие ошибки кидает?
            telegramBot.sendPlainTextMessage(chatId, messageToClient);
        }
    }
}
