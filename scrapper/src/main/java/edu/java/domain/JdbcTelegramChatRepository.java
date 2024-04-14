package edu.java.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import edu.java.domain.exceptions.DataBaseInteractingException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcTelegramChatRepository implements BaseEntityRepository<Long> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public int add(Long telegramChat) {
        try {
            return jdbcTemplate.update(
                "insert into telegram_chat(chat_id) values (?) on conflict do nothing ",
                telegramChat
            );
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public int remove(Long telegramChat) {
        try {
            return jdbcTemplate.update("delete from telegram_chat where chat_id = (?)", telegramChat);
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public Collection<Long> findAll() {
        try {
            return jdbcTemplate.query("select * from telegram_chat", new JdbcTelegramChatRepository.LinkRowMapper());
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public Collection<Long> search(Predicate<Long> condition) {
        return findAll()
            .stream()
            .filter(condition)
            .collect(Collectors.toList());
    }

    private static class LinkRowMapper implements RowMapper<Long> {
        @Override
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getLong("chat_id");
        }
    }
}
