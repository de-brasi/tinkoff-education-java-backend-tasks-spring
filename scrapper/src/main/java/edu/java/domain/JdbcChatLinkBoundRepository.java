package edu.java.domain;

import edu.java.entities.ChatLinkBound;
import edu.java.entities.Link;
import edu.java.entities.TelegramChat;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Repository
public class JdbcChatLinkBoundRepository implements BaseEntityRepository<ChatLinkBound> {
    private final JdbcTemplate jdbcTemplate;

    public JdbcChatLinkBoundRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public boolean add(ChatLinkBound chatLinkBound) {
        // todo:
        //  проверить что есть чат -
        //      если нет, то ошибка що нет чата;
        //  проверить что есть ссылка -
        //      если нет, то создать ссылку
        //  попробовать вставить пару -
        //      если ошибка повторной вставки, то вернуть false,
        //      иначе проверить (affectedRows == 1),
        //          если false -
        //              выкинуть какую нибудь кастомную ошибку взаимодействия с бд,
        //          иначе true

        // todo:
        //  контракт:
        //      return true - успешная вставка
        //      return false - повторная вставка
        //      срем разные кастомные ошибки: просто ОшибкаРаботыСБД (пока только одна)
        return false;
    }

    @Override
    @Transactional
    public @Nullable ChatLinkBound remove(ChatLinkBound chatLinkBound) {
        // todo:
        //  проверить что есть чат -
        //      если нет, то ошибка що нет чата;
        //  попробовать удалить пару -
        //      если ошибка удаления несуществующего, то кастомная ошибка ОшибкаЧтоНетЧатаСтакимИД (потом пробросить InvalidRequest),
        //      иначе проверить (affectedRows == 1),
        //          если false -
        //              выкинуть какую нибудь кастомную ошибку взаимодействия с бд,
        //          иначе -
        //              вернуть ChatLinkBound

        // todo:
        //  контракт:
        //      return true - успешное удаление
        //      return false - повторная вставка
        //      срем разные кастомные ошибки: просто ОшибкаРаботыСБД, ОшибкаЧтоНетЧатаСтакимИД
        return null;
    }

    @Override
    @Transactional
    public Collection<ChatLinkBound> findAll() {
        String sql = "select * from track_info";
        return jdbcTemplate.query(sql, new JdbcChatLinkBoundRepository.LinkRowMapper());
    }

    private static class LinkRowMapper implements RowMapper<ChatLinkBound> {
        @Override
        public ChatLinkBound mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long tgChatId = rs.getLong("chat_id");
            String url = rs.getString("url");
            return new ChatLinkBound(new TelegramChat(tgChatId), new Link(URI.create(url)));
        }
    }
}
