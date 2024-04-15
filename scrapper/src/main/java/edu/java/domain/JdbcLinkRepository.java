package edu.java.domain;

import edu.java.domain.entities.Link;
import edu.java.domain.exceptions.DataBaseInteractingException;
import edu.java.domain.exceptions.UnexpectedDataBaseStateException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcLinkRepository implements BaseEntityRepository<String> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public int add(String link) {
        try {
            return jdbcTemplate.update(
                "insert into links(url, last_check_time, last_update_time) values (?, ?, ?) on conflict do nothing",
                link,
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.ofEpochSecond(0))
            );
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public int remove(String link) {
        try {
            return jdbcTemplate.update("delete from links where url = (?)", link);
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public Collection<String> findAll() {
        try {
            return jdbcTemplate.query("select * from links", new LinkRowMapper());
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public Collection<String> search(Predicate<String> condition) {
        return findAll()
            .stream()
            .filter(condition)
            .collect(Collectors.toList());
    }

    @Transactional
    public Collection<String> getOutdated(Duration thresholdDuration) {
        try {
            Instant thresholdTime = Instant.now().minus(thresholdDuration);

            return jdbcTemplate.query(
                "select id, url from links where links.last_check_time < ?",
                ps -> ps.setTimestamp(1, Timestamp.from(thresholdTime)),
                (rs, rowNum) -> rs.getString("url")
            );
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Transactional
    public void updateLastCheckTime(String url, Timestamp actualTime) {
        final String query = "update links set last_check_time = ? where url = ?";
        int affectedRowsCount = jdbcTemplate.update(query, actualTime, url);

        if (affectedRowsCount != 1) {
            throw new UnexpectedDataBaseStateException(
                "Expected to update field 'last_check_time' one row with current time but no one row changed!"
            );
        }
    }

    @Transactional
    public void updateLastUpdateTime(String url, Timestamp actualTime) {
        final String query = "update links set last_update_time = ? where url = ?";
        int affectedRowsCount = jdbcTemplate.update(query, actualTime, url);

        if (affectedRowsCount != 1) {
            throw new UnexpectedDataBaseStateException(
                "Expected to update field 'last_update_time' one row with current time but no one row changed!"
            );
        }
    }

    private static class LinkRowMapper implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("url");
        }
    }
}
