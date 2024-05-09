package edu.java.data.domain.repositories.jdbc;

import edu.java.data.domain.entities.ChatLinkBound;
import edu.java.data.domain.entities.Link;
import edu.java.data.domain.exceptions.DataBaseInteractingException;
import edu.java.data.domain.repositories.BaseEntityRepository;
import edu.java.data.domain.exceptions.NoExpectedEntityInDataBaseException;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for working with entities from database representing bound between chat and tracked link.
 */
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("MultipleStringLiterals")
public class JdbcChatLinkBoundRepository implements BaseEntityRepository<ChatLinkBound> {

    private final JdbcTemplate jdbcTemplate;
    private final JdbcLinkRepository linkRepository;
    private final RowMapper<ChatLinkBound> chatLinkBoundRowMapper;
    private final RowMapper<Link> linkRowMapper;

    private static final String CHAT_ENTITY_NAME = "telegram_chat";

    /**
     * Add bound between chat and link in table 'track_info'.
     * If link with passed url not saved in table 'links' create record in 'links' table
     * and try to store pair (chat_id, link_id) in table 'track_info'.
     *
     * @param chatLinkBound object via info about chat and tracked link
     * @return affected rows count
     * @throws NoExpectedEntityInDataBaseException In case of telegram chat not saved yet in table 'telegram_chat'
     * {@link NoExpectedEntityInDataBaseException} will be thrown
     * @throws DataBaseInteractingException        In case of some error occurs when working with JdbcTemplate
     * {@link DataBaseInteractingException} will be thrown
     */
    @Override
    @Transactional
    public int add(ChatLinkBound chatLinkBound) {
        /*
        Add bound between chat and link in table 'track_info'.
        If link with passed url not saved in table 'links' creates record in 'links' table and
            try to store pair <chat_id, link_id> in table 'track_info'.

        In case such bound already exists returns false,
        otherwise - true.

        Throws:
        - InvalidArgumentForTypeInDataBase: in case of invalid url value passed;
        - NoExpectedEntityInDataBaseException: if telegram chat not saved yet in table 'telegram_chat';
        - DataBaseInteractingException: if some error occurs when working with JdbcTemplate;
        */

        try {
            int telegramChatIdInDatabase;

            // get chat id or throw exception
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

            // try to add link, do nothing on conflict
            linkRepository.add(chatLinkBound.linkURL());
            final String getLinkIdQuery = "select id from links where url = ? limit 1";
            int linkIdInDatabase = jdbcTemplate.queryForObject(
                getLinkIdQuery,
                Integer.class,
                chatLinkBound.linkURL()
            );

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
     * @return collection of {@link ChatLinkBound} represented existing bounds
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
            return jdbcTemplate.query(sql, chatLinkBoundRowMapper);
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    /**
     * Get all information about chats and their tracked links filtered with {@link java.util.function.Predicate}.
     *
     * @return collection of {@link ChatLinkBound} represented existing bounds
     */
    @Override
    @Transactional
    public Collection<ChatLinkBound> search(Predicate<ChatLinkBound> condition) {
        return findAll()
            .stream()
            .filter(condition)
            .collect(Collectors.toList());
    }

    /**
     * Get id of entity record in table 'track_info'.
     * @param entity ChatLinkBound for search.
     * @return Positive Long value with id on success, negative value if record not found in database.
     */
    @Override
    @Transactional(readOnly = true)
    public Long getEntityId(ChatLinkBound entity) {
        try {
            final String query =
                "select id from track_info "
                    + "where link_id = (select id from links where url = ?) "
                    + "and telegram_chat_id = (select id from telegram_chat where chat_id = ?)";
            return jdbcTemplate.queryForObject(query, Long.class, entity.linkURL(), entity.chatId());
        } catch (EmptyResultDataAccessException e) {
            return -1L;
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Transactional
    public Collection<Link> getLinksTrackedBy(Long chatId) {
        try {
            String sql =
                "select link_id, url "
                    + "from track_info "
                    + "join telegram_chat tg_chats "
                    + "on (track_info.telegram_chat_id = tg_chats.id) "
                    + "and (tg_chats.chat_id = ?) "
                    + "join links links_table "
                    + "on links_table.id = track_info.link_id;";
            return jdbcTemplate.query(
                sql,
                ps -> ps.setLong(1, chatId),
                linkRowMapper
            );
        } catch (DataAccessException e) {
            throw new DataBaseInteractingException(e);
        }
    }
}
