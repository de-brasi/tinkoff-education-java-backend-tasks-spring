package edu.java.domain;

import edu.java.entities.TelegramChat;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Repository
public class JdbcTelegramChatRepository implements BaseEntityRepository<TelegramChat> {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTelegramChatRepository(JdbcTemplate jdbcTemplate) {
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
            System.out.println("hi");
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

    private static class LinkRowMapper implements RowMapper<TelegramChat> {
        @Override
        public TelegramChat mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long tgChatId = rs.getLong("chat_id");
            return new TelegramChat(tgChatId);
        }
    }
}
