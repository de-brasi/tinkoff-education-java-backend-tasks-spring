package edu.java.api;

import edu.common.datatypes.dtos.AddLinkRequest;
import edu.common.datatypes.dtos.LinkResponse;
import edu.common.datatypes.dtos.ListLinksResponse;
import edu.common.datatypes.dtos.RemoveLinkRequest;
import edu.java.domain.entities.Link;
import edu.java.services.interfaces.LinkService;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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

    public LinksController(
        @Autowired
        LinkService linkService
    ) {
        this.linkService = linkService;
    }

    @GetMapping()
    public ResponseEntity<ListLinksResponse> getAllTrackedLinkForChat(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
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

    @PostMapping()
    public ResponseEntity<?> addTrackingLinkForChat(
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
    public ResponseEntity<?> untrackLinkForChat(
        @RequestHeader("Tg-Chat-Id") Long tgChatId,
        @RequestBody RemoveLinkRequest request
    ) throws MalformedURLException {
        // todo проверять на:
        //  - некорректные параметры 400
        //  - чат не существует 404
        LOGGER.info(tgChatId);
        Link removed = linkService.remove(tgChatId, URI.create(request.getLink()));
        // todo: добавить id в сущность Link, брать id оттуда
        LinkResponse response = new LinkResponse(1, removed.uri().toURL().toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private final static Logger LOGGER = LogManager.getLogger();
}

