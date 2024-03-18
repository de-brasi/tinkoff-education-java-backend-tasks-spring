package edu.java.services.jdbc;

import edu.java.clients.BotClient;
import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
import edu.java.services.interfaces.LinkUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class JdbcLinkUpdater implements LinkUpdater {
    private final JdbcTemplate jdbcTemplate;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;

    public JdbcLinkUpdater(
        @Autowired JdbcTemplate jdbcTemplate,
        @Autowired GitHubClient gitHubClient,
        @Autowired StackOverflowClient stackOverflowClient,
        @Autowired BotClient botClient
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.stackOverflowClient = stackOverflowClient;
        this.gitHubClient = gitHubClient;
        this.botClient = botClient;
    }

    @Override
    @Transactional
    public int update(Collection<Link> toUpdate) {
        int updatedLinks = 0;

        for (Link link : toUpdate) {
            try {

                final String currentLinkUrl = link.uri().toURL().toString();
                List<Long> subscribers =
                    getSubscribers(link)
                        .stream()
                        .map(TelegramChat::id)
                        .toList();

                boolean linkUpdated;
                if (currentLinkUrl.startsWith(gitHubClient.getDefaultBaseUrl())) {
                    final OffsetDateTime actualUpdateTime =
                        gitHubClient.fetchUpdate(currentLinkUrl).updateTime();
                    linkUpdated = notifyClientsIfUpdatedTimeChanged(actualUpdateTime, link, subscribers);
                } else if (currentLinkUrl.startsWith(stackOverflowClient.getDefaultBaseUrl())) {
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
                LOGGER.info(("""
                    Exception when checking update of link %s;
                    Exception: %s
                    Message: %s
                    Stacktrace:
                    %s
                    """).formatted(link, e.getClass().getCanonicalName(), e.getMessage(),
                    Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList()
                ));
            }
        }

        return updatedLinks;
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public Collection<TelegramChat> getSubscribers(Link link) {
        String getSubsQuery =
            "select chat_id " +
                "from track_info " +
                "join public.links l on l.id = track_info.link_id and url = ? " +
                "join public.telegram_chat tc on tc.id = track_info.telegram_chat_id";
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

    @Override
    public boolean compareAndSetLastUpdateTime(Link target, OffsetDateTime actualTime) throws MalformedURLException {
        final String query =
            "update links " +
                "set last_update_time = case " +
                "when last_update_time < ? then ? " +
                "else last_update_time " +
                "end " +
                "where url = ?";

        final String url = target.uri().toURL().toString();

        int affectedRowsCount = jdbcTemplate.update(
            query,
            Timestamp.from(actualTime.toInstant()),
            Timestamp.from(actualTime.toInstant()),
            url
        );

        return affectedRowsCount > 0;
    }

    private boolean notifyClientsIfUpdatedTimeChanged(OffsetDateTime actualTime, Link link, List<Long> subscribersId)
        throws MalformedURLException {
        final boolean timesEqual = compareAndSetLastUpdateTime(link, actualTime);
        if (!timesEqual) {
            // todo: использовать id ссылки, пока заглушка
            botClient.sendUpdates(
                -1,
                link.uri().toURL().toString(),
                "updated",
                subscribersId
            );
        }

        return !timesEqual;
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
