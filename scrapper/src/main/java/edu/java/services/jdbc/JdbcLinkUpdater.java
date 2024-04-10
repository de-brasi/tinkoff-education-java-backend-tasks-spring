package edu.java.services.jdbc;

import edu.java.clients.BotClient;
import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import edu.java.domain.JdbcLinkRepository;
import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
import edu.java.domain.exceptions.UnexpectedDataBaseStateException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@SuppressWarnings("MultipleStringLiterals")
public class JdbcLinkUpdater implements LinkUpdater {
    private final JdbcTemplate jdbcTemplate;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;
    private final JdbcLinkRepository linkRepository;

    public JdbcLinkUpdater(
        @Autowired JdbcTemplate jdbcTemplate,
        @Autowired GitHubClient gitHubClient,
        @Autowired StackOverflowClient stackOverflowClient,
        @Autowired BotClient botClient,
        @Autowired JdbcLinkRepository jdbcLinkRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.stackOverflowClient = stackOverflowClient;
        this.gitHubClient = gitHubClient;
        this.botClient = botClient;
        this.linkRepository = jdbcLinkRepository;
    }

    @Override
    public int update(Duration updateInterval) {
//        Collection<Link> linksToUpdate = getNotCheckedForAWhile(updateInterval);

        Predicate<Link> outdatedLinkPredicate = link -> {
            try {
                return Duration.between(OffsetDateTime.now(), getLinkCheckTime(link)).compareTo(updateInterval) < 0;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        };

        Collection<Link> linksToUpdate = getAllLinksFilteredByPredicate(outdatedLinkPredicate);
        log.info("Links need to update: " + linksToUpdate);

        int updatedLinks = 0;

        for (Link link : linksToUpdate) {
            try {
                final String currentLinkUrl = link.uri().toURL().toString();

                List<Long> subscribers =
                    getSubscribers(link)
                        .stream()
                        .map(TelegramChat::id)
                        .toList();

                boolean linkUpdated;
                if (gitHubClient.checkURLSupportedByService(currentLinkUrl)) {
                    final OffsetDateTime actualUpdateTime =
                        gitHubClient.fetchUpdate(currentLinkUrl).updateTime();
                    linkUpdated = notifyClientsIfUpdatedTimeChanged(actualUpdateTime, link, subscribers);
                } else if (stackOverflowClient.checkURLSupportedByService(currentLinkUrl)) {
                    final OffsetDateTime actualUpdateTime =
                        stackOverflowClient.fetchUpdate(currentLinkUrl).updateTime();
                    linkUpdated = notifyClientsIfUpdatedTimeChanged(actualUpdateTime, link, subscribers);
                } else {
                    throw new RuntimeException(
                        "Unexpected API for fetching update for URL %s".formatted(currentLinkUrl)
                    );
                }

                if (linkUpdated) {
                    updatedLinks++;
                }

            } catch (Exception e) {
                log.info(("""
                    Exception when checking update of link %s;
                    Exception: %s
                    Message: %s
                    Stacktrace:
                    %s
                    """)
                    .formatted(link, e.getClass().getCanonicalName(), e.getMessage(),
                        Arrays.stream(e.getStackTrace())
                            .map(StackTraceElement::toString)
                            .toList()
                    )
                );
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

    /**
     * Check if database stores last_update_time value different with time in parameter;
     * @param target with {@link edu.java.domain.entities.Link} to check time
     * @param actualTime that will be compared with last saved time
     * @return true if stored time before actual
     * @throws MalformedURLException if {@link  edu.java.domain.entities.Link} stores incorrect URI
     */
    public boolean checkLastUpdateTimeChanged(Link target, OffsetDateTime actualTime) throws MalformedURLException {
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

    /**
     *
     * @param actualTime actual last update time
     * @param link {@link edu.java.domain.entities.Link} for checking update time
     * @param subscribersId link subscribers
     * @return true if time updated, otherwise false
     * @throws MalformedURLException if {@link edu.java.domain.entities.Link} stores incorrect URI
     */
    private boolean notifyClientsIfUpdatedTimeChanged(OffsetDateTime actualTime, Link link, List<Long> subscribersId)
        throws MalformedURLException {
        final boolean timeUpdated = checkLastUpdateTimeChanged(link, actualTime);
        final String currentLinkUrl = link.uri().toURL().toString();

        if (timeUpdated) {
            // todo: использовать id ссылки, пока заглушка
            botClient.sendUpdates(
                -1,
                link.uri().toURL().toString(),
                "updated",
                subscribersId
            );

            linkRepository.updateLastCheckTime(currentLinkUrl, Timestamp.from(Instant.now()));
            linkRepository.updateLastUpdateTime(currentLinkUrl, Timestamp.from(actualTime.toInstant()));
        }

        return timeUpdated;
    }
}
