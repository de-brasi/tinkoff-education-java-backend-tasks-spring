package edu.java.api;

import edu.java.api.dtos.ApiErrorResponse;
import edu.java.api.dtos.LinkResponse;
import edu.java.api.dtos.ListLinksResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "/scrapper/api",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class Controllers {

    @PostMapping(value = "/tg-chat/{id}")
    public ResponseEntity<ApiErrorResponse> handleRegistryChat(@PathVariable Long id) {
        // todo:
        //  - некорректные параметры 400
        System.out.println(id);

        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

    @DeleteMapping(value = "/tg-chat/{id}")
    public ResponseEntity<ApiErrorResponse> handleDeleteChat(@PathVariable Long id) {
        // todo:
        //  - некорректные параметры 400
        //  - чат не существует 404
        System.out.println(id);

        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

    // TODO: параметр в Header
    @GetMapping(value = "/links")
    public ResponseEntity<?> handleGetLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        // todo:
        //  - некорректные параметры 400
        System.out.println(tgChatId);

        return new ResponseEntity<>(new ListLinksResponse(), HttpStatusCode.valueOf(200));
    }

    @PostMapping(value = "/links")
    public ResponseEntity<?> handlePostLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        // todo:
        //  - некорректные параметры 400
        System.out.println(tgChatId);

        return new ResponseEntity<>(new LinkResponse(), HttpStatusCode.valueOf(200));
    }

    @DeleteMapping(value = "/links")
    public ResponseEntity<?> handleDeleteLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        // todo:
        //  - некорректные параметры 400
        //  - чат не существует 404
        System.out.println(tgChatId);

        return new ResponseEntity<>(new LinkResponse(), HttpStatusCode.valueOf(200));
    }

}