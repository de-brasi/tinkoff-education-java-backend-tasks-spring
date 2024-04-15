package edu.java.api;

import edu.common.dtos.AddLinkRequest;
import edu.common.dtos.LinkResponse;
import edu.common.dtos.ListLinksResponse;
import edu.common.dtos.RemoveLinkRequest;
import edu.java.domain.entities.Link;
import edu.java.services.interfaces.LinkService;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"MultipleStringLiterals"})
public class LinkController {

    private final LinkService linkService;

    @GetMapping()
    public ResponseEntity<ListLinksResponse> getAllTrackedLinkForChat(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        log.info("All tracked links command for chat with chat-id " + tgChatId);

        Collection<Link> allLinks = linkService.listAll(tgChatId);
        List<LinkResponse> linkResponseList = allLinks
            .stream()
            .map(e -> {
                try {
                    return new LinkResponse(e.id(), e.uri().toURL().toString());
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
    public ResponseEntity<LinkResponse> addTrackingLinkForChat(
        @RequestHeader("Tg-Chat-Id") Long tgChatId,
        @RequestBody AddLinkRequest request
    ) throws MalformedURLException {
        log.info("Add link command for chat with chat-id " + tgChatId + " and request " + request);
        Link added = linkService.add(tgChatId, URI.create(request.getLink()));
        LinkResponse response = new LinkResponse(added.id(), added.uri().toURL().toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity<LinkResponse> untrackLinkForChat(
        @RequestHeader("Tg-Chat-Id") Long tgChatId,
        @RequestBody RemoveLinkRequest request
    ) throws MalformedURLException {
        log.info("Delete link command for chat with chat-id " + tgChatId + " and request " + request);
        Link removed = linkService.remove(tgChatId, URI.create(request.getLink()));
        LinkResponse response = new LinkResponse(removed.id(), removed.uri().toURL().toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

