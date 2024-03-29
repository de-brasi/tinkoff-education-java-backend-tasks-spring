package edu.java.bot.api;

import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.services.TelegramBotWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    public UpdateController(@Autowired TelegramBotWrapper telegramBotWrapper) {
        this.telegramBot = telegramBotWrapper;
    }

    private final TelegramBotWrapper telegramBot;

    @PostMapping(value = "/updates", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @SuppressWarnings("RegexpSinglelineJava")
    public ResponseEntity<ApiErrorResponse> handleUpdateRequest(@RequestBody LinkUpdateRequest request) {
        LOGGER.info("Update request: " + request);

        final String messageToClient = "Update in link %s with description: '%s'"
            .formatted(request.getUrl(), request.getDescription());

        for (Long chatId : request.getTgChatIds()) {
            // todo: обработка ошибок
            telegramBot.sendPlainTextMessage(chatId, messageToClient);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
