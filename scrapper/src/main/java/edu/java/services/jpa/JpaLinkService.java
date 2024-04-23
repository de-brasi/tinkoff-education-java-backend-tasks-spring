package edu.java.services.jpa;

import edu.java.domain.entities.Link;
import edu.java.domain.repositories.jpa.entities.SupportedService;
import edu.java.domain.repositories.jpa.entities.TelegramChat;
import edu.java.domain.repositories.jpa.entities.TrackInfo;
import edu.java.domain.repositories.jpa.implementations.JpaLinkRepository;
import edu.java.domain.repositories.jpa.implementations.JpaSupportedServicesRepository;
import edu.java.domain.repositories.jpa.implementations.JpaTelegramChatRepository;
import edu.java.domain.repositories.jpa.implementations.JpaTrackInfoRepository;
import edu.java.services.ExternalServicesObserver;
import edu.java.services.interfaces.LinkService;
import jakarta.persistence.NoResultException;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final JpaTelegramChatRepository chatRepository;
    private final JpaSupportedServicesRepository servicesRepository;
    private final JpaTrackInfoRepository trackInfoRepository;
    private final ExternalServicesObserver externalServicesObserver;

    @Override
    @Transactional
    public Link add(long tgChatId, URI url) {
        try {
            final OffsetDateTime initialTime = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(0), ZoneOffset.UTC);

            TelegramChat chat = chatRepository.get(tgChatId);
            final String urlString = url.toURL().toString();

            Optional<edu.java.domain.repositories.jpa.entities.Link> link =
                linkRepository.getLinkByUrl(urlString);

            if (link.isEmpty()) {
                final SupportedService service = getLinksService(urlString);
                final String actualSnapshot = externalServicesObserver.getActualSnapshot(urlString);
                link = linkRepository.add(urlString, service, initialTime, initialTime, actualSnapshot);
            }

            TrackInfo trackInfo = new TrackInfo();
            trackInfo.setLink(link.orElseThrow());
            trackInfo.setChat(chat);

            trackInfoRepository.save(trackInfo);

            return new Link(url);
        } catch (MalformedURLException e) {
            // todo
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public Link remove(long tgChatId, URI url) {
        try {
            TelegramChat chat = chatRepository.get(tgChatId);
            String urlString = url.toURL().toString();

            Optional<edu.java.domain.repositories.jpa.entities.Link> link = linkRepository.getLinkByUrl(urlString);
            TrackInfo trackInfo = new TrackInfo();
            trackInfo.setLink(link.orElseThrow());
            trackInfo.setChat(chat);

            trackInfoRepository.delete(trackInfo);

            return new Link(url);
        } catch (NoResultException e) {
            return null;
        } catch (MalformedURLException e) {
            // todo
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public Collection<Link> listAll(long tgChatId) {

        Collection<TrackInfo> trackInfos = trackInfoRepository.findTrackInfoByChat_Id(tgChatId);

        return trackInfos.stream()
            .map(e -> new Link(
                    URI.create(e.getLink().getUrl())
            ))
            .collect(Collectors.toList());
    }

    private SupportedService getLinksService(String urlString) {
        if (urlString.startsWith("https://stackoverflow.com/")) {
            return servicesRepository.getService("stackoverflow");
        } else if (urlString.startsWith("https://github.com/")) {
            return servicesRepository.getService("github");
        } else {
            throw new RuntimeException("Unexpected link %s; cant find relative service".formatted(urlString));
        }
    }
}
