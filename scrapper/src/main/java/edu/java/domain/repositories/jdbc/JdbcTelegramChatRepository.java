package edu.java.domain.repositories.jdbc;

import edu.java.domain.entities.TelegramChat;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import edu.java.domain.repositories.BaseEntityRepository;
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
public class JdbcTelegramChatRepository implements BaseEntityRepository<TelegramChat> {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTelegramChatRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public boolean add(TelegramChat telegramChat) {

        // TODO:
        //  Почему-то если пытаться вставлять новую запись и ловить
        //  исключение DataAccessException
        //  (когда добавляется повторная запись; возникает из-за ограничение на уникальность ссылок)
        //  исключение обрабатывается (проверяется логирующим принтом),
        //  однако потом возникает снова в вызывающем коде (в тестах например).
        //  Как будто бы прокси объект обрабатывает исключение но пробрасывает его дальше.
        //  Для решения проблемы пришлось сначала проверять число записей с таким chat_id.

        try {
            int equalLinksCount = jdbcTemplate.queryForObject(
                "select count(*) from telegram_chat where chat_id = ?",
                Integer.class,
                telegramChat.id()
            );

            if (equalLinksCount == 0) {
                int affectedRowCount = jdbcTemplate.update(
                    "insert into telegram_chat(chat_id) values (?)",
                    telegramChat.id()
                );

                return (affectedRowCount == 1);
            } else {
                return false;
            }
        } catch (DataAccessException e) {
            LOGGER.info("hi");
            return false;
        }
    }

    @Override
    @Transactional
    public @Nullable TelegramChat remove(TelegramChat telegramChat) {
        int affectedRowCount = jdbcTemplate.update("delete from telegram_chat where chat_id = (?)", telegramChat.id());
        return (affectedRowCount == 1) ? telegramChat : null;
    }

    @Override
    @Transactional
    public Collection<TelegramChat> findAll() {
        String sql = "select * from telegram_chat";
        return jdbcTemplate.query(sql, new JdbcTelegramChatRepository.LinkRowMapper());
    }

    @Override
    @Transactional
    public Collection<TelegramChat> search(Predicate<TelegramChat> condition) {
        return findAll()
            .stream()
            .filter(condition)
            .collect(Collectors.toList());
    }

    private final static Logger LOGGER = LogManager.getLogger();

    private static class LinkRowMapper implements RowMapper<TelegramChat> {
        @Override
        public TelegramChat mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long tgChatId = rs.getLong("chat_id");
            return new TelegramChat(tgChatId);
        }
    }
}
