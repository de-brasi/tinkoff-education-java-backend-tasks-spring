package edu.java.domain;

import edu.common.exceptions.IncorrectRequestException;
import edu.java.domain.entities.Link;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcLinkRepository implements BaseEntityRepository<Link> {
    private final JdbcTemplate jdbcTemplate;

    public JdbcLinkRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public boolean add(Link link) {

        // TODO:
        //  Почему-то если пытаться вставлять новую запись и ловить
        //  исключение DataAccessException
        //  (когда добавляется повторная запись; возникает из-за ограничение на уникальность ссылок)
        //  исключение обрабатывается (проверяется логирующим принтом),
        //  однако потом возникает снова в вызывающем коде (в тестах например).
        //  Как будто бы прокси объект обрабатывает исключение но пробрасывает его дальше.
        //  Для решения проблемы пришлось сначала проверять число записей с таким url.

        try {
            int equalLinksCount = jdbcTemplate.queryForObject(
                "select count(*) from links where url = ?",
                Integer.class,
                link.uri().toURL().toString()
            );

            if (equalLinksCount == 0) {
                int affectedRowCount = jdbcTemplate.update(
                    "insert into links(url, last_check_time, last_update_time) values (?, ?, ?)",
                    link.uri().toURL().toString(),
                    Timestamp.from(Instant.now()),
                    Timestamp.from(Instant.ofEpochSecond(0))
                );

                return (affectedRowCount == 1);
            } else {
                return false;
            }
        } catch (DataAccessException e) {
            LOGGER.info("hi");
            return false;
        } catch (MalformedURLException e) {
            throw new IncorrectRequestException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public @Nullable Link remove(Link link) {
        try {
            int affectedRowCount = jdbcTemplate.update(
                "delete from links where url = (?)",
                link.uri().toURL().toString()
            );
            return (affectedRowCount == 1) ? link : null;
        } catch (MalformedURLException e) {
            throw new IncorrectRequestException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Collection<Link> findAll() {
        String sql = "select * from links";
        return jdbcTemplate.query(sql, new LinkRowMapper());
    }

    private final static Logger LOGGER = LogManager.getLogger();

    private static class LinkRowMapper implements RowMapper<Link> {
        @Override
        public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
            String url = rs.getString("url");
            return new Link(URI.create(url));
        }
    }
}
