package edu.java.domain;

import edu.java.domain.entities.Link;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Repository
public class JdbcLinkRepository implements BaseEntityRepository<Link> {
    private final JdbcTemplate jdbcTemplate;

    public JdbcLinkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public boolean add(Link link) {
        try {
            int affectedRowCount = jdbcTemplate.update("insert into links(url) values (?)", link.uri().toURL().toString());
            return (affectedRowCount == 1);
        } catch (DataAccessException e) {
            System.out.println("hi");
            return false;
        } catch (MalformedURLException e) {
            // todo: better exception; may be custom or smth else
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public @Nullable Link remove(Link link) {
        try {
            int affectedRowCount = jdbcTemplate.update("delete from links where url = (?)", link.uri().toURL().toString());
            return (affectedRowCount == 1) ? link : null;
        } catch (MalformedURLException e) {
            // todo: better exception; may be custom or smth else
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public Collection<Link> findAll() {
        String sql = "select * from links";
        return jdbcTemplate.query(sql, new LinkRowMapper());
    }

    private static class LinkRowMapper implements RowMapper<Link> {
        @Override
        public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
            String url = rs.getString("url");
            return new Link(URI.create(url));
        }
    }
}
