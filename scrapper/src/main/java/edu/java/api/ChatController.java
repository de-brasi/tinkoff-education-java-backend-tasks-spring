package edu.java.api;

import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.java.services.interfaces.TgChatService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@SuppressWarnings({"MultipleStringLiterals"})
public class ChatController {

    private final TgChatService tgChatService;

    @PostMapping(value = "/{id}")
    public ResponseEntity<ApiErrorResponse> handleRegistryChat(@PathVariable Long id) {
        tgChatService.register(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ApiErrorResponse> handleDeleteChat(@PathVariable Long id) {
        tgChatService.unregister(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
