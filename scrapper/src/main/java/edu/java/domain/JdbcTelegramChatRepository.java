package edu.java.domain;

import edu.java.domain.exceptions.DataBaseInteractingException;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcTelegramChatRepository implements BaseEntityRepository<Long> {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Long> chatIdRowMapper;

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
            return jdbcTemplate.query("select * from telegram_chat", chatIdRowMapper);
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

    /**
     * Get id of chat in table 'telegram_chat'.
     * @param entity telegram chat id for search
     * @return Positive Long value with record id on success, negative value if record not found in database.
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getEntityId(Long entity) {
        try {
            return jdbcTemplate.queryForObject(
                "select id from telegram_chat where chat_id = ?",
                Integer.class, entity
            );
        } catch (EmptyResultDataAccessException e) {
            return -1;
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }
}
