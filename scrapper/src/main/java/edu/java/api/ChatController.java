package edu.java.api;

import edu.common.dtos.ApiErrorResponse;
import edu.common.dtos.LinkResponse;
import edu.common.dtos.ListLinksResponse;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "/scrapper/api",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@SuppressWarnings({"MultipleStringLiterals"})
public class ChatController {
    @PostMapping(value = "/tg-chat/{id}")
    public ResponseEntity<ApiErrorResponse> handleRegistryChat(@PathVariable Long id) {
        // todo проверять на:
        //  - некорректные параметры 400
        LOGGER.info(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/tg-chat/{id}")
    public ResponseEntity<ApiErrorResponse> handleDeleteChat(@PathVariable Long id) {
        // todo проверять на:
        //  - некорректные параметры 400
        //  - чат не существует 404
        LOGGER.info(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
