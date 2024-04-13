package edu.java.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import edu.java.domain.exceptions.DataBaseInteractingException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
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
    public boolean add(Long telegramChat) {
        try {
            int equalLinksCount = jdbcTemplate.queryForObject(
                "select count(*) from telegram_chat where chat_id = ?",
                Integer.class,
                telegramChat
            );

            if (equalLinksCount == 0) {
                int affectedRowCount = jdbcTemplate.update(
                    "insert into telegram_chat(chat_id) values (?)",
                    telegramChat
                );

                return (affectedRowCount == 1);
            } else {
                return false;
            }
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public @Nullable Long remove(Long telegramChat) {
        int affectedRowCount = jdbcTemplate.update("delete from telegram_chat where chat_id = (?)", telegramChat);
        return (affectedRowCount == 1) ? telegramChat : null;
    }

    @Override
    @Transactional
    public Collection<Long> findAll() {
        String sql = "select * from telegram_chat";
        return jdbcTemplate.query(sql, new JdbcTelegramChatRepository.LinkRowMapper());
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
            Long tgChatId = rs.getLong("chat_id");
            return tgChatId;
        }
    }
}
