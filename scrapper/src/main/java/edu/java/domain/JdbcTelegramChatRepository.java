package edu.java.domain;

import edu.java.domain.entities.TelegramChat;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTelegramChatRepository implements BaseEntityRepository<TelegramChat> {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTelegramChatRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean add(TelegramChat telegramChat) {
        try {
            int affectedRowCount = jdbcTemplate.update(
                "INSERT into telegram_chat(chat_id) values (?)",
                telegramChat.id()
            );
            return (affectedRowCount == 1);
        } catch (DataAccessException e) {
            LOGGER.info("hi");
            return false;
        }
    }

    @Override
    public @Nullable TelegramChat remove(TelegramChat telegramChat) {
        int affectedRowCount = jdbcTemplate.update("delete from telegram_chat where chat_id = (?)", telegramChat.id());
        return (affectedRowCount == 1) ? telegramChat : null;
    }

    @Override
    public Collection<TelegramChat> findAll() {
        String sql = "select * from telegram_chat";
        return jdbcTemplate.query(sql, new JdbcTelegramChatRepository.LinkRowMapper());
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
