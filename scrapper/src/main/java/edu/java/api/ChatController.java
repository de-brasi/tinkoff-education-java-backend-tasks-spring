package edu.java.api;

import edu.common.dtos.ApiErrorResponse;
import edu.java.services.interfaces.TgChatService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/scrapper/api/tg-chat", produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings({"MultipleStringLiterals"})
public class ChatController {
    private final TgChatService tgChatService;

    public ChatController(
        @Autowired
        @Qualifier("jdbcTgChatService")
        TgChatService tgChatService
    ) {
        this.tgChatService = tgChatService;

    }

    @PostMapping(value = "/{id}")
    public ResponseEntity<ApiErrorResponse> handleRegistryChat(@PathVariable Long id) {
        // todo проверять на:
        //  - некорректные параметры 400

        tgChatService.register(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ApiErrorResponse> handleDeleteChat(@PathVariable Long id) {
        // todo проверять на:
        //  - некорректные параметры 400
        //  - чат не существует 404

        tgChatService.unregister(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
