package edu.java.api;

import edu.common.dtos.AddLinkRequest;
import edu.common.dtos.LinkResponse;
import edu.common.dtos.ListLinksResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import edu.java.entities.Link;
import edu.java.services.interfaces.LinkService;
import edu.java.services.interfaces.TgChatService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/scrapper/api/links", produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings({"MultipleStringLiterals"})
public class LinksController {
    private final LinkService linkService;

    public LinksController(@Autowired LinkService linkService) {
        this.linkService = linkService;
    }

    private static final ListLinksResponse LIST_LINKS_RESPONSE_STUB = new ListLinksResponse(
        List.of(
            new LinkResponse(1, "https://www.wikipedia.org/"),
            new LinkResponse(2, "https://en.wikipedia.org/wiki/Main_Page")
        ),
        2
    );

    private static final LinkResponse LINKS_RESPONSE_STUB = new LinkResponse(
        0L, "https://www.wikipedia.org/"
    );

    @GetMapping()
    public ResponseEntity<ListLinksResponse> handleGetLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        // todo проверять на:
        //  - некорректные параметры 400

        Collection<Link> allLinks = linkService.listAll(tgChatId);
        // todo: добавить id в сущность Link, брать id оттуда
        List<LinkResponse> linkResponseList = allLinks
            .stream()
            .map(e -> {
                try {
                    return new LinkResponse(1, e.uri().toURL().toString());
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
            })
            .collect(Collectors.toList());

        ListLinksResponse response = new ListLinksResponse(
            linkResponseList, linkResponseList.size()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // todo: better naming for all handlers (in this change name pattern from linkS to link_)
    @PostMapping()
    public ResponseEntity<?> handlePostLinks(
        @RequestHeader("Tg-Chat-Id") Long tgChatId,
        @RequestBody AddLinkRequest request
    ) throws MalformedURLException {
        // todo проверять на:
        //  - некорректные параметры 400

        LOGGER.info(tgChatId);
        Link added = linkService.add(tgChatId, URI.create(request.getLink()));
        // todo: добавить id в сущность Link, брать id оттуда
        LinkResponse response = new LinkResponse(1, added.uri().toURL().toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity<?> handleDeleteLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        // todo проверять на:
        //  - некорректные параметры 400
        //  - чат не существует 404
        LOGGER.info(tgChatId);

        return new ResponseEntity<>(LINKS_RESPONSE_STUB, HttpStatus.OK);
    }

    private final static Logger LOGGER = LogManager.getLogger();
}

