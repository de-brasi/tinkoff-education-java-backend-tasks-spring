package edu.java.services.jdbc;

import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
import edu.java.services.interfaces.LinkUpdater;
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
import java.util.Collection;

@Service
public class JdbcLinkUpdater implements LinkUpdater {
    private final JdbcTemplate jdbcTemplate;

    public JdbcLinkUpdater(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public int update(Collection<Link> toUpdate) {
        return 0;
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
    @Transactional
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
}
