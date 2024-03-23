package edu.java.services.jdbc;

import edu.java.clients.BotClient;
import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
import edu.java.domain.exceptions.UnexpectedDataBaseStateException;
import edu.java.domain.repositories.BaseEntityRepository;
import edu.java.domain.repositories.jdbc.JdbcLinkRepository;
import edu.java.services.ExternalServicesObserver;
import edu.java.services.interfaces.LinkUpdater;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("MultipleStringLiterals")
public class JdbcLinkUpdater implements LinkUpdater {
    private final JdbcTemplate jdbcTemplate;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;
    private final BaseEntityRepository<Link> linkRepository;
    private final ExternalServicesObserver servicesObserver;

    public JdbcLinkUpdater(
        @Autowired JdbcTemplate jdbcTemplate,
        @Autowired GitHubClient gitHubClient,
        @Autowired StackOverflowClient stackOverflowClient,
        @Autowired BotClient botClient,
        @Autowired JdbcLinkRepository jdbcLinkRepository,
        @Autowired ExternalServicesObserver externalServicesObserver
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.stackOverflowClient = stackOverflowClient;
        this.gitHubClient = gitHubClient;
        this.botClient = botClient;
        this.linkRepository = jdbcLinkRepository;
        this.servicesObserver = externalServicesObserver;
    }

    @Override
    @Transactional
    public int update(Duration updateInterval) {
//        Collection<Link> toUpdate = getNotCheckedForAWhile(updateInterval);

        Predicate<Link> needToBeCheckedLinkPredicate = link -> {
            try {
                // todo: нужно сравнивать на > 0; почему же это работает?
                return Duration.between(OffsetDateTime.now(), getLinkCheckTime(link)).compareTo(updateInterval) < 0;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        };
        Collection<Link> toUpdate = getAllLinksFilteredByPredicate(needToBeCheckedLinkPredicate);

        LOGGER.info("Links need to update: " + toUpdate);

        int updatedLinks = 0;

        for (Link link : toUpdate) {
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

    public Collection<Link> getNotCheckedForAWhile(Duration duration) {
        Instant threshold = Instant.now().minus(duration);

        String getNotCheckedLinkQuery = "select id, url from links where links.last_check_time < ?";
        Collection<Link> result = jdbcTemplate.query(
            getNotCheckedLinkQuery,
            ps -> ps.setTimestamp(1, Timestamp.from(threshold)),
            (rs, rowNum) -> new Link(URI.create(rs.getString("url")))
        );

        return result
            .stream()
            .toList();
    }

    public Collection<Link> getAllLinksFilteredByPredicate(Predicate<Link> predicate) {
        return linkRepository.search(predicate);
    }

    private OffsetDateTime getLinkCheckTime(Link link) throws MalformedURLException {
        return jdbcTemplate.queryForObject(
            "select last_check_time from links where url = ?",
            OffsetDateTime.class,
            link.uri().toURL().toString()
        );
    }

    private Collection<TelegramChat> getSubscribers(Link link) {
        String getSubsQuery =
            "select chat_id "
                + "from track_info "
                + "join public.links l on l.id = track_info.link_id and url = ? "
                + "join public.telegram_chat tc on tc.id = track_info.telegram_chat_id";
        Collection<TelegramChat> result = jdbcTemplate.query(
            getSubsQuery,
            ps -> {
                try {
                    ps.setString(1, link.uri().toURL().toString());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            },
            (rs, rowNum) -> new TelegramChat(rs.getLong("chat_id"))
        );

        return result
            .stream()
            .toList();
    }

    public boolean checkLastUpdateTimeChanged(Link target, OffsetDateTime actualTime) throws MalformedURLException {
        /*
        Return:
        - true: time updated
        - false: time not updated
        */
        final String url = target.uri().toURL().toString();

        OffsetDateTime storedLastUpdateTime = jdbcTemplate.queryForObject(
            "select last_update_time from links where url = ?",
            OffsetDateTime.class,
            url
        );

        if (storedLastUpdateTime == null) {
            throw new UnexpectedDataBaseStateException(
                "Stored 'last_update_time' value not retrieved from database!"
            );
        }

        return storedLastUpdateTime.isBefore(actualTime);
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
            final String oldSnapshot = getSnapshot(currentLinkUrl);
            final String actualSnapshot = servicesObserver.getActualSnapshot(currentLinkUrl);
            final String changesDescription =
                servicesObserver.getChangingDescription(currentLinkUrl, oldSnapshot, actualSnapshot);

            List<Long> subscribers =
                getSubscribers(link)
                    .stream()
                    .map(TelegramChat::id)
                    .toList();

            // todo: использовать id ссылки, пока заглушка
            botClient.sendUpdates(
                -1,
                link.uri().toURL().toString(),
                changesDescription,
                subscribers
            );

            actualizeSnapshot(currentLinkUrl, actualSnapshot);
            actualizeLastUpdateTimeForLink(currentLinkUrl, actualUpdateTime);
            actualizeCheckingTimeForLink(currentLinkUrl);
        }

        return resourceUpdated;
    }

    private String getSnapshot(String url) {
        return jdbcTemplate.queryForObject(
            "select snapshot from links where url = ?",
            String.class,
            url
        );
    }

    private void actualizeSnapshot(String url, String snapshot) {
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

    private void actualizeCheckingTimeForLink(String url) {
        final String query = "update links set last_check_time = ? where url = ?";
        int affectedRowsCount = jdbcTemplate.update(
            query,
            Timestamp.from(Instant.now()),
            url
        );

        if (affectedRowsCount != 1) {
            throw new UnexpectedDataBaseStateException(
                "Expected to update field 'last_check_time' one row with current time but no one row changed!"
            );
        }
    }

    private void actualizeLastUpdateTimeForLink(String url, OffsetDateTime actualTime) {
        final String query =
            "update links "
                + "set last_update_time = ? "
                + "where url = ?";

        int affectedRowsCount = jdbcTemplate.update(
            query,
            Timestamp.from(actualTime.toInstant()),
            url
        );

        if (affectedRowsCount != 1) {
            throw new UnexpectedDataBaseStateException(
                "Saving new 'last_update_time' not affect to database!"
            );
        }
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
