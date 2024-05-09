package edu.java.services.jdbc;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
import edu.java.domain.exceptions.UnexpectedDataBaseStateException;
import edu.java.domain.repositories.jdbc.JdbcLinkRepository;
import edu.java.services.ExternalServicesObserver;
import edu.java.services.interfaces.LinkUpdater;
import edu.java.updateproducing.ScrapperUpdateProducer;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("MultipleStringLiterals")
public class JdbcLinkUpdater implements LinkUpdater {
    private final JdbcTemplate jdbcTemplate;
    private final JdbcLinkRepository linkRepository;
    private final ExternalServicesObserver servicesObserver;
    private final ScrapperUpdateProducer scrapperUpdateProducer;

    @Override
    public int update(Duration updateInterval) {
        Collection<Link> linksToUpdate = linkRepository.getOutdated(updateInterval);
        log.info("Links need to update: " + linksToUpdate);

        int updatedLinks = 0;

        for (Link link : linksToUpdate) {
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
                log.error(("""
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

    private Collection<TelegramChat> getSubscribers(String link) {
        String getSubsQuery =
            "select chat_id "
                + "from track_info "
                + "join public.links l on l.id = track_info.link_id and url = ? "
                + "join public.telegram_chat tc on tc.id = track_info.telegram_chat_id";
        Collection<TelegramChat> result = jdbcTemplate.query(
            getSubsQuery,
            ps -> ps.setString(1, link),
            (rs, rowNum) -> new TelegramChat(rs.getLong("chat_id"))
        );

        return result
            .stream()
            .toList();
    }

    /**
     * Check if database stores last_update_time value different with time in parameter;
     * @param targetLink with {@link edu.java.domain.entities.Link} to check time
     * @param actualTime that will be compared with last saved time
     * @return true if stored time before actual
     */
    public boolean checkLastUpdateTimeChanged(String targetLink, OffsetDateTime actualTime) {
        OffsetDateTime storedLastUpdateTime = jdbcTemplate.queryForObject(
            "select last_update_time from links where url = ?",
            OffsetDateTime.class,
            targetLink
        );

        if (storedLastUpdateTime == null) {
            throw new UnexpectedDataBaseStateException(
                "Stored 'last_update_time' value not retrieved from database!"
            );
        }

        return storedLastUpdateTime.isBefore(actualTime);
    }

    /**
     * Notify clients if time of last update changed
     * @param actualUpdateTime Actual last update time.
     * @param link {@link edu.java.domain.entities.Link} For checking update time.
     * @return true if time updated, otherwise false.
     */
    private boolean notifyClientsIfUpdatedTimeChanged(
        OffsetDateTime actualUpdateTime,
        Link link
    ) throws MalformedURLException {
        final String linkUrl = link.uri().toURL().toString();
        final boolean timeUpdated = checkLastUpdateTimeChanged(linkUrl, actualUpdateTime);

        if (timeUpdated) {
            final String oldSnapshot = getSnapshot(linkUrl);
            final String actualSnapshot = servicesObserver.getActualSnapshot(linkUrl);
            final String changesDescription =
                servicesObserver.getChangingDescription(linkUrl, oldSnapshot, actualSnapshot);

            List<Long> subscribersId =
                getSubscribers(linkUrl)
                    .stream()
                    .map(TelegramChat::id)
                    .toList();

            LinkUpdateRequest updateRequest =
                new LinkUpdateRequest(link.id(), linkUrl, changesDescription, subscribersId);
            scrapperUpdateProducer.send(updateRequest);

            int updatedCheckTimeRowsCount = linkRepository.updateLastCheckTime(linkUrl, Timestamp.from(Instant.now()));
            int updatedUpdateTimeRowsCount =
                linkRepository.updateLastUpdateTime(linkUrl, Timestamp.from(actualUpdateTime.toInstant()));
            actualizeSnapshot(linkUrl, actualSnapshot);

            if (updatedCheckTimeRowsCount != 1) {
                throw new UnexpectedDataBaseStateException(
                    "Expected to update field 'last_check_time' one row with current time but no one row changed!"
                );
            }

            if (updatedUpdateTimeRowsCount != 1) {
                throw new UnexpectedDataBaseStateException(
                    "Expected to update field 'last_update_time' one row with current time but no one row changed!"
                );
            }
        }

        return timeUpdated;
    }

    private String getSnapshot(String url) {
        return jdbcTemplate.queryForObject(
            "select snapshot from links where url = ?",
            String.class,
            url
        );
    }

    // todo: вынести как метод link-updater'а
    @Transactional
    public void actualizeSnapshot(String url, String snapshot) {
        int affectedRows = jdbcTemplate.update(
            "update links set snapshot = ? where url = ?",
            snapshot,
            url
        );

        if (affectedRows != 1) {
            throw new UnexpectedDataBaseStateException(
                "Expected to update field 'snapshot' one row with actual value for link with url "
                    + url
                    + " but no one row changed!"
            );
        }
    }

}
