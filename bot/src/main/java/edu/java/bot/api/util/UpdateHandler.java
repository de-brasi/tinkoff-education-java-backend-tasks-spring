package edu.java.bot.api.util;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.services.TelegramBotWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateHandler {
    private final TelegramBotWrapper telegramBot;
    private final KafkaTemplate<String, LinkUpdateRequest> template;

    @Value("${app.dead-letter-queue-topic.name}")
    private String faultyLettersTopicName;

    public void handleUpdate(LinkUpdateRequest updateRequest) {
        final String messageToClient = "Update in link %s with description: '%s'"
            .formatted(updateRequest.getUrl(), updateRequest.getDescription());

        try {
            for (Long chatId : updateRequest.getTgChatIds()) {
                telegramBot.sendPlainTextMessage(chatId, messageToClient);
            }
        } catch (Exception e) {
            template.send(faultyLettersTopicName, updateRequest);
        }
    }
}
