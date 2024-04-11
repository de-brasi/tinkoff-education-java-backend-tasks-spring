package edu.java.domain;

import edu.java.domain.entities.ChatLinkBound;
import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
import edu.java.domain.exceptions.DataBaseInteractingException;
import edu.java.domain.exceptions.InvalidArgumentForTypeInDataBase;
import edu.java.domain.exceptions.NoExpectedEntityInDataBaseException;
import edu.java.domain.exceptions.UnexpectedDataBaseStateException;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
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
     * @throws InvalidArgumentForTypeInDataBase In case of invalid url value passed {@link edu.java.domain.exceptions.InvalidArgumentForTypeInDataBase} will be thrown
     * @throws NoExpectedEntityInDataBaseException In case of telegram chat not saved yet in table 'telegram_chat' {@link edu.java.domain.exceptions.NoExpectedEntityInDataBaseException} will be thrown
     * @throws DataBaseInteractingException In case of some error occurs when working with JdbcTemplate {@link edu.java.domain.exceptions.DataBaseInteractingException} will be thrown
     * @return adding result: false in case such bound already exists returns false, otherwise - true.
     */
    @Override
    @Transactional
    public boolean add(ChatLinkBound chatLinkBound) {
        try {
            int linkIdInDatabase;
            int telegramChatIdInDatabase;

            // get chat id
            try {
                final String getTgChatIdQuery = "select id from telegram_chat where chat_id = ? limit 1";
                telegramChatIdInDatabase = jdbcTemplate.queryForObject(
                    getTgChatIdQuery,
                    Integer.class,
                    chatLinkBound.chat().id()
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
                    chatLinkBound.link().uri().toURL().toString()
                );
            } catch(EmptyResultDataAccessException e) {
                // create if not exists
                linkIdInDatabase = jdbcTemplate.queryForObject(
                    "insert into links(url, last_check_time, last_update_time) values (?, ?, ?) returning id",
                    Integer.class,
                    chatLinkBound.link().uri().toURL().toString(),
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
            return (affectedRowsCount == 1);
        } catch (MalformedURLException e) {
            throw new InvalidArgumentForTypeInDataBase(e);
        } catch (DataAccessException | NullPointerException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    /**
     * Remove bound between chat and link in table 'track_info'.
     *
     * @param chatLinkBound object via info about chat and tracked link
     * @throws InvalidArgumentForTypeInDataBase In case of invalid url value passed {@link edu.java.domain.exceptions.InvalidArgumentForTypeInDataBase} will be thrown
     * @throws NoExpectedEntityInDataBaseException In case of telegram chat not saved yet in table 'telegram_chat' {@link edu.java.domain.exceptions.NoExpectedEntityInDataBaseException} will be thrown
     * @throws DataBaseInteractingException In case of some error occurs when working with JdbcTemplate {@link edu.java.domain.exceptions.DataBaseInteractingException} will be thrown
     * @return argument of type {@link edu.java.domain.entities.ChatLinkBound} if removed, null otherwise
     */
    @Override
    @Transactional
    public @Nullable ChatLinkBound remove(ChatLinkBound chatLinkBound) {
        try {
            if (!checkChatExists(chatLinkBound.chat())) {
                throw new NoExpectedEntityInDataBaseException(CHAT_ENTITY_NAME);
            }

            final String deleteBoundaryRecordQuery =
                "delete from track_info "
                + "where telegram_chat_id = (select id from telegram_chat where chat_id = ?) "
                    + "and link_id = (select id from links where url = ?)";
            int affectedRowsCount = jdbcTemplate.update(
                deleteBoundaryRecordQuery,
                chatLinkBound.chat().id(), chatLinkBound.link().uri().toURL().toString()
            );

            if (affectedRowsCount > 1) {
                throw new UnexpectedDataBaseStateException(
                    "Too many records in table 'track_info' for unique pair of telegram_chat id and link id"
                );
            }

            return (affectedRowsCount == 1) ? chatLinkBound : null;
        } catch (MalformedURLException e) {
            throw new InvalidArgumentForTypeInDataBase(e);
        } catch (DataAccessException | NullPointerException e) {
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
        String sql =
            "select tg_chats.id, chat_id, link_id, url "
            + "from track_info "
            + "join telegram_chat tg_chats on track_info.telegram_chat_id = tg_chats.id "
            + "join links links_table on links_table.id = track_info.link_id;";
        return jdbcTemplate.query(sql, new JdbcChatLinkBoundRepository.LinkRowMapper());
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

    private boolean checkChatExists(TelegramChat telegramChat) {
        final String queryToCountChatWithId = "select count(*) from telegram_chat where chat_id = ?";

        int recordsCount = jdbcTemplate.queryForObject(
            queryToCountChatWithId, Integer.class,
            telegramChat.id()
        );

        if (recordsCount > 1) {
            throw new UnexpectedDataBaseStateException(
                "Too many records in table 'telegram_chat' for unique telegram chat id value"
            );
        }

        return (recordsCount == 1);
    }

    private boolean checkLinkExists(Link link) throws MalformedURLException {
        final String queryToCountLinksWithUrl = "select count(*) from links where url = ?";
        int recordsCount = jdbcTemplate.queryForObject(
            queryToCountLinksWithUrl,
            Integer.class,
            link.uri().toURL().toString()
        );

        if (recordsCount > 1) {
            throw new UnexpectedDataBaseStateException(
                "Too many records in table 'links' for unique link's url value"
            );
        }

        return (recordsCount == 1);
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
