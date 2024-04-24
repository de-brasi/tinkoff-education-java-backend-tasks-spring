package edu.java.bot.api;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.services.TelegramBotWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/bot/api")
public class UpdateController {
    private final TelegramBotWrapper telegramBot;

    public UpdateController(@Autowired TelegramBotWrapper telegramBotWrapper) {
        this.telegramBot = telegramBotWrapper;
    }

    @PostMapping(value = "/updates", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @SuppressWarnings("RegexpSinglelineJava")
    public ResponseEntity<?> handleUpdateRequest(@RequestBody LinkUpdateRequest requestBody) {
        final String messageToClient = "Update in link %s with description: '%s'"
            .formatted(requestBody.getUrl(), requestBody.getDescription());

        for (Long chatId : requestBody.getTgChatIds()) {
            telegramBot.sendPlainTextMessage(chatId, messageToClient);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
