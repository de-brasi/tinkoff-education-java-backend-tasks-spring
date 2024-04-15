package edu.java.domain;

import edu.java.domain.entities.ChatLinkBound;
import edu.java.domain.entities.TelegramChat;
import edu.java.domain.exceptions.DataBaseInteractingException;
import edu.java.domain.exceptions.NoExpectedEntityInDataBaseException;
import edu.java.domain.exceptions.UnexpectedDataBaseStateException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for working with entities from database representing bound between chat and tracked link.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class JdbcChatLinkBoundRepository implements BaseEntityRepository<ChatLinkBound> {

    private final JdbcTemplate jdbcTemplate;

    private static final String CHAT_ENTITY_NAME = "telegram_chat";

    /**
     * Add bound between chat and link in table 'track_info'. If link with passed url not saved in table 'links' creates record in 'links' table and try to store pair <chat_id, link_id> in table 'track_info'.
     *
     * @param chatLinkBound object via info about chat and tracked link
     * @return affected rows count
     * @throws NoExpectedEntityInDataBaseException In case of telegram chat not saved yet in table 'telegram_chat' {@link edu.java.domain.exceptions.NoExpectedEntityInDataBaseException} will be thrown
     * @throws DataBaseInteractingException        In case of some error occurs when working with JdbcTemplate {@link edu.java.domain.exceptions.DataBaseInteractingException} will be thrown
     */
    @Override
    @Transactional
    public int add(ChatLinkBound chatLinkBound) {
        int linkIdInDatabase;
        int telegramChatIdInDatabase;

        try {
            // get chat id
            try {
                final String getTgChatIdQuery = "select id from telegram_chat where chat_id = ? limit 1";
                telegramChatIdInDatabase = jdbcTemplate.queryForObject(
                    getTgChatIdQuery,
                    Integer.class,
                    chatLinkBound.chatId()
                );
            } catch (EmptyResultDataAccessException e) {
                throw new NoExpectedEntityInDataBaseException(CHAT_ENTITY_NAME);
            }

            // get link id
            try {
                final String getLinkIdQuery = "select id from links where url = ? limit 1";
                linkIdInDatabase = jdbcTemplate.queryForObject(
                    getLinkIdQuery,
                    Integer.class,
                    chatLinkBound.linkURL()
                );
            } catch (EmptyResultDataAccessException e) {
                // create if not exists
                linkIdInDatabase = jdbcTemplate.queryForObject(
                    "insert into links(url, last_check_time, last_update_time) values (?, ?, ?) returning id",
                    Integer.class,
                    chatLinkBound.linkURL(),
                    Timestamp.from(Instant.now()),
                    Timestamp.from(Instant.ofEpochSecond(0))
                );
            }

            final String insertBoundQuery =
                "insert into track_info(telegram_chat_id, link_id) values (?, ?) on conflict do nothing";
            log.info(
                "Try to insert into track_info: [chat_id: "
                    + telegramChatIdInDatabase + ", link_id: "
                    + linkIdInDatabase + "]"
            );
            int affectedRowsCount = jdbcTemplate.update(
                insertBoundQuery,
                telegramChatIdInDatabase, linkIdInDatabase
            );
            if (affectedRowsCount == 1) {
                log.info(
                    "Success inserting into track_info: [chat_id: "
                        + telegramChatIdInDatabase + ", link_id: "
                        + linkIdInDatabase + "]"
                );
            }
            return affectedRowsCount;
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    /**
     * Remove bound between chat and link in table 'track_info'.
     *
     * @param chatLinkBound object via info about chat and tracked link
     * @return affected rows count
     */
    @Override
    @Transactional
    public int remove(ChatLinkBound chatLinkBound) {
        try {
            final String deleteBoundaryRecordQuery =
                "delete from track_info "
                    + "where telegram_chat_id = (select id from telegram_chat where chat_id = ?) "
                    + "and link_id = (select id from links where url = ?)";

            return jdbcTemplate.update(
                deleteBoundaryRecordQuery,
                chatLinkBound.chatId(), chatLinkBound.linkURL()
            );
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    /**
     * Get all information about chats and their tracked links.
     *
     * @return collection of {@link edu.java.domain.entities.ChatLinkBound} represented existing bounds
     */
    @Override
    @Transactional
    public Collection<ChatLinkBound> findAll() {
        try {
            String sql =
                "select tg_chats.id, chat_id, link_id, url "
                    + "from track_info "
                    + "join telegram_chat tg_chats on track_info.telegram_chat_id = tg_chats.id "
                    + "join links links_table on links_table.id = track_info.link_id;";
            return jdbcTemplate.query(sql, new JdbcChatLinkBoundRepository.LinkRowMapper());
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    /**
     * Get all information about chats and their tracked links filtered with {@link java.util.function.Predicate}.
     *
     * @return collection of {@link edu.java.domain.entities.ChatLinkBound} represented existing bounds
     */
    @Override
    @Transactional
    public Collection<ChatLinkBound> search(Predicate<ChatLinkBound> condition) {
        return findAll()
            .stream()
            .filter(condition)
            .collect(Collectors.toList());
    }

    private static class LinkRowMapper implements RowMapper<ChatLinkBound> {
        @Override
        public ChatLinkBound mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long tgChatId = rs.getLong("chat_id");
            String url = rs.getString("url");
            return new ChatLinkBound(tgChatId, url);
        }
    }
}
