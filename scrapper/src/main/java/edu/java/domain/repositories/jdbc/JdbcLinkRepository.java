package edu.java.domain.repositories.jdbc;

import edu.java.domain.entities.Link;
import edu.java.domain.exceptions.DataBaseInteractingException;
import edu.java.domain.exceptions.InvalidArgumentForTypeInDataBase;
import edu.java.domain.repositories.BaseEntityRepository;
import edu.java.services.ExternalServicesObserver;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JdbcLinkRepository implements BaseEntityRepository<String> {

    private final JdbcTemplate jdbcTemplate;
    private final ExternalServicesObserver servicesObserver;
    private final RowMapper<String> linkUrlRowMapper;
    private final RowMapper<Link> linkRowMapper;

    @Override
    @Transactional
    public int add(String link) {
        try {
            final String serviceName = servicesObserver.getRelativeServiceNameInDatabase(link);
            checkServiceExistsInDatabaseElseThrowException(serviceName);
            int serviceIndex = jdbcTemplate.queryForObject(
                "select id from supported_services where name = ?",
                Integer.class,
                serviceName
            );

            final String snapshot = servicesObserver.getActualSnapshot(link);

            return jdbcTemplate.update(
                "insert "
                    + "into links(url, last_check_time, last_update_time, service, snapshot) "
                    + "values (?, ?, ?, ?, cast(? as json)) on conflict do nothing",
                link,
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.ofEpochSecond(0)),
                serviceIndex,
                snapshot
            );
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public int remove(String link) {
        try {
            int affectedRowCount = jdbcTemplate.update(
                "delete from links where url = (?)",
                link
            );
            return affectedRowCount;
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public Collection<String> findAll() {
        try {
            String sql = "select * from links";
            return jdbcTemplate.query(sql, linkUrlRowMapper);
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

    /**
     * Get id of url in table 'links'.
     * @param entity Url for search
     * @return Positive Long value with record id on success, negative value if record not found in database.
     */
    @Override
    @Transactional(readOnly = true)
    public Long getEntityId(String entity) {
        try {
            return jdbcTemplate.queryForObject(
                "select id from links where url = ?",
                Long.class, entity
            );
        } catch (EmptyResultDataAccessException e) {
            return -1L;
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Transactional
    public Collection<Link> getOutdated(Duration thresholdDuration) {
        try {
            Instant thresholdTime = Instant.now().minus(thresholdDuration);

            return jdbcTemplate.query(
                "select id as link_id, url from links where links.last_check_time < ?",
                ps -> ps.setTimestamp(1, Timestamp.from(thresholdTime)),
                linkRowMapper
            );
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Transactional
    public int updateLastCheckTime(String url, Timestamp actualTime) {
        try {
            return jdbcTemplate.update(
                "update links set last_check_time = ? where url = ?",
                actualTime, url
            );
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Transactional
    public int updateLastUpdateTime(String url, Timestamp actualTime) {
        try {
            return jdbcTemplate.update(
                "update links set last_update_time = ? where url = ?",
                actualTime, url
            );
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    private void checkServiceExistsInDatabaseElseThrowException(String serviceName) {
        boolean serviceExists = jdbcTemplate.queryForObject(
            "select count(*) from supported_services where name = ?",
            Integer.class,
            serviceName
        ) > 0;

        if (!serviceExists) {
            throw new InvalidArgumentForTypeInDataBase("Unexpected service with name " + serviceName);
        }
    }
}
