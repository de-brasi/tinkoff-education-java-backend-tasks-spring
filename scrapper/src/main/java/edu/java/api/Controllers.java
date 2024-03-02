package edu.java.api;

import edu.common.dtos.ApiErrorResponse;
import edu.common.dtos.LinkResponse;
import edu.common.dtos.ListLinksResponse;
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
import java.util.List;

@RestController
@RequestMapping(
    value = "/scrapper/api",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class Controllers {

    private static final ListLinksResponse listLinksResponseStub = new ListLinksResponse(
        List.of(
            new LinkResponse(1, "https://www.wikipedia.org/"),
            new LinkResponse(2, "https://en.wikipedia.org/wiki/Main_Page")
        ),
        2
    );

    private static final LinkResponse linksResponseStub = new LinkResponse(
        0L, "https://www.wikipedia.org/"
    );

    @PostMapping(value = "/tg-chat/{id}")
    public ResponseEntity<ApiErrorResponse> handleRegistryChat(@PathVariable Long id) {
        // todo проверять на:
        //  - некорректные параметры 400
        System.out.println(id);

        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

    @DeleteMapping(value = "/tg-chat/{id}")
    public ResponseEntity<ApiErrorResponse> handleDeleteChat(@PathVariable Long id) {
        // todo проверять на:
        //  - некорректные параметры 400
        //  - чат не существует 404
        System.out.println(id);

        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

    // TODO: параметр в Header
    @GetMapping(value = "/links")
    public ResponseEntity<ListLinksResponse> handleGetLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        // todo проверять на:
        //  - некорректные параметры 400
        System.out.println(tgChatId);

        return new ResponseEntity<>(listLinksResponseStub, HttpStatusCode.valueOf(200));
    }

    @PostMapping(value = "/links")
    public ResponseEntity<?> handlePostLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        // todo проверять на:
        //  - некорректные параметры 400
        System.out.println(tgChatId);

        return new ResponseEntity<>(linksResponseStub, HttpStatusCode.valueOf(200));
    }

    @DeleteMapping(value = "/links")
    public ResponseEntity<?> handleDeleteLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        // todo проверять на:
        //  - некорректные параметры 400
        //  - чат не существует 404
        System.out.println(tgChatId);

        return new ResponseEntity<>(linksResponseStub, HttpStatusCode.valueOf(200));
    }

}
