package edu.java.services.jpa;

import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
import edu.java.domain.exceptions.UnexpectedDataBaseStateException;
import edu.java.domain.repositories.jpa.entities.TrackInfo;
import edu.java.domain.repositories.jpa.implementations.JpaLinkRepository;
import edu.java.services.ExternalServicesObserver;
import edu.java.services.enteties.LinkUpdate;
import edu.java.services.interfaces.LinkUpdater;
import edu.java.updateproducing.ScrapperUpdateProducer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JpaLinkUpdater implements LinkUpdater {
    @PersistenceContext
    private EntityManager entityManager;

    private final JpaLinkRepository linkRepository;
    private final ExternalServicesObserver servicesObserver;
    private final ScrapperUpdateProducer scrapperUpdateProducer;

    @Override
    @Transactional
    public int update(Duration updateInterval) {
        List<Link> allLinksToCheck = linkRepository.findAll()
            .stream()
            .filter(
                item -> Duration.between(
                    OffsetDateTime.now(),
                    item.getLastCheckTime()
                ).compareTo(updateInterval) < 0
            ).map(
                e -> new Link(
                    URI.create(e.getUrl())
                )
            )
            .toList();

        LOGGER.info("Links need to update: " + allLinksToCheck);
        int updatedLinks = 0;

        for (Link link : allLinksToCheck) {
            try {
                final String currentLinkUrl = link.uri().toURL().toString();

                if (servicesObserver.checkURLSupported(currentLinkUrl)) {
                    final OffsetDateTime actualUpdateTime =
                        servicesObserver.getActualUpdateTime(currentLinkUrl);
                    boolean linkUpdated = notifyClientsIfUpdatedTimeChanged(actualUpdateTime, link);

                    if (linkUpdated) {
                        updatedLinks++;
                    }
                } else {
                    throw new RuntimeException(
                        "Unexpected API for fetching update for URL %s".formatted(currentLinkUrl)
                    );
                }

            } catch (Exception e) {
                LOGGER.info(("""
                    Exception when checking update of link %s;
                    Exception: %s
                    Message: %s
                    Stacktrace:
                    %s
                    """)
                    .formatted(
                        link,
                        e.getClass().getCanonicalName(),
                        e.getMessage(),
                        Arrays.stream(e.getStackTrace())
                            .map(StackTraceElement::toString)
                            .collect(Collectors.joining("\n"))
                    ));
            }
        }

        return updatedLinks;
    }

    private boolean notifyClientsIfUpdatedTimeChanged(
        OffsetDateTime actualUpdateTime,
        Link link
    ) throws MalformedURLException {
        /*
        Return:
        - true: time updated
        - false: time not updated
        */
        final boolean resourceUpdated = checkLastUpdateTimeChanged(link, actualUpdateTime);
        final String currentLinkUrl = link.uri().toURL().toString();

        if (resourceUpdated) {
            final String oldSnapshot = linkRepository.get(currentLinkUrl).getSnapshot();
            final String actualSnapshot = servicesObserver.getActualSnapshot(currentLinkUrl);
            final String changesDescription =
                servicesObserver.getChangingDescription(currentLinkUrl, oldSnapshot, actualSnapshot);

            List<Long> subscribers =
                getSubscribers(link)
                    .stream()
                    .map(TelegramChat::id)
                    .toList();

            // todo: использовать id ссылки, пока заглушка
            LinkUpdate linkUpdate = new LinkUpdate(
                -1, link.uri().toURL().toString(),
                changesDescription, subscribers
            );
            scrapperUpdateProducer.send(linkUpdate);

            edu.java.domain.repositories.jpa.entities.Link updatedLink = linkRepository.get(currentLinkUrl);
            updatedLink.setSnapshot(actualSnapshot);
            updatedLink.setLastUpdateTime(actualUpdateTime);
            updatedLink.setLastCheckTime(OffsetDateTime.now());
            linkRepository.save(updatedLink);
        }

        return resourceUpdated;
    }

    public boolean checkLastUpdateTimeChanged(Link target, OffsetDateTime actualTime) throws MalformedURLException {
        /*
        Return:
        - true: time updated
        - false: time not updated
        */
        final String url = target.uri().toURL().toString();

        OffsetDateTime storedLastUpdateTime = linkRepository.get(target.uri().toURL().toString()).getLastUpdateTime();

        if (storedLastUpdateTime == null) {
            throw new UnexpectedDataBaseStateException(
                "Stored 'last_update_time' value not retrieved from database!"
            );
        }

        return storedLastUpdateTime.isBefore(actualTime);
    }

    private Collection<TelegramChat> getSubscribers(Link link) throws MalformedURLException {
        return entityManager.createQuery(
                "SELECT trackInfo FROM TrackInfo trackInfo WHERE trackInfo.link.url = :url",
                TrackInfo.class
            ).setParameter("url", link.uri().toURL().toString())
            .getResultList()
            .stream()
            .map(e -> new TelegramChat(e.getChat().getChatId()))
            .toList();
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
