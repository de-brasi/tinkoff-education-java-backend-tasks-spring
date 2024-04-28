package edu.java.bot.api.util;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.services.TelegramBotWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateHandler {
    private final TelegramBotWrapper telegramBot;

    public void handleUpdate(LinkUpdateRequest updateRequest) {

        final String messageToClient = "Update in link %s with description: '%s'"
            .formatted(updateRequest.getUrl(), updateRequest.getDescription());

        for (Long chatId : updateRequest.getTgChatIds()) {
            try {
                telegramBot.sendPlainTextMessage(chatId, messageToClient);
            } catch (Exception e) {
                log.error("""
                        Cant send message '{}' to client with chat id {};
                        Reason:
                        Exception {}
                        with message {}
                        with stack trace
                        {}
                        """,
                    messageToClient,
                    chatId,
                    e.getClass().getCanonicalName(),
                    e.getMessage(),
                    Arrays.stream(e.getStackTrace())
                        .map(Objects::toString)
                        .collect(Collectors.joining("\n"))
                );
            }
        }
    }
}
