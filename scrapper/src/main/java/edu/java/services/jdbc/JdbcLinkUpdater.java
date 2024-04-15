package edu.java.services.jdbc;

import edu.java.clients.BotClient;
import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import edu.java.domain.JdbcLinkRepository;
import edu.java.domain.entities.TelegramChat;
import edu.java.domain.exceptions.UnexpectedDataBaseStateException;
import edu.java.services.interfaces.LinkUpdater;
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
        Collection<String> linksToUpdate = linkRepository.getOutdated(updateInterval);
        log.info("Links need to update: " + linksToUpdate);

        int updatedLinks = 0;

        for (String link : linksToUpdate) {
            try {
                List<Long> subscribers =
                    getSubscribers(link)
                        .stream()
                        .map(TelegramChat::id)
                        .toList();

                boolean linkUpdated;
                if (gitHubClient.checkURLSupportedByService(link)) {
                    final OffsetDateTime actualUpdateTime =
                        gitHubClient.fetchUpdate(link).updateTime();
                    linkUpdated = notifyClientsIfUpdatedTimeChanged(actualUpdateTime, link, subscribers);
                } else if (stackOverflowClient.checkURLSupportedByService(link)) {
                    final OffsetDateTime actualUpdateTime =
                        stackOverflowClient.fetchUpdate(link).updateTime();
                    linkUpdated = notifyClientsIfUpdatedTimeChanged(actualUpdateTime, link, subscribers);
                } else {
                    throw new RuntimeException(
                        "Unexpected API for fetching update for URL %s".formatted(link)
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

    public Collection<String> getAllLinksFilteredByPredicate(Predicate<String> predicate) {
        return linkRepository.search(predicate);
    }

    private OffsetDateTime getLinkCheckTime(String link) {
        return jdbcTemplate.queryForObject(
            "select last_check_time from links where url = ?",
            OffsetDateTime.class,
            link
        );
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
     * @param actualTime Actual last update time.
     * @param link {@link edu.java.domain.entities.Link} For checking update time.
     * @param subscribersId Link subscribers.
     * @return true if time updated, otherwise false.
     */
    private boolean notifyClientsIfUpdatedTimeChanged(
        OffsetDateTime actualTime,
        String link,
        List<Long> subscribersId
    ) {
        final boolean timeUpdated = checkLastUpdateTimeChanged(link, actualTime);
        if (timeUpdated) {
            // todo: использовать id ссылки, пока заглушка
            botClient.sendUpdates(-1, link, "updated", subscribersId);

            int updatedCheckTimeRowsCount = linkRepository.updateLastCheckTime(link, Timestamp.from(Instant.now()));
            int updatedUpdateTimeRowsCount =
                linkRepository.updateLastUpdateTime(link, Timestamp.from(actualTime.toInstant()));

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
}
