package edu.java.domain;

import edu.java.domain.exceptions.DataBaseInteractingException;
import edu.java.domain.exceptions.InvalidArgumentForTypeInDataBase;
import edu.java.domain.exceptions.NoExpectedEntityInDataBaseException;
import edu.java.domain.exceptions.UnexpectedDataBaseStateException;
import edu.java.entities.ChatLinkBound;
import edu.java.entities.Link;
import edu.java.entities.TelegramChat;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Repository
public class JdbcChatLinkBoundRepository implements BaseEntityRepository<ChatLinkBound> {
    private final JdbcTemplate jdbcTemplate;

    private static final String CHAT_ENTITY_NAME = "telegram_chat";

    public JdbcChatLinkBoundRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public boolean add(ChatLinkBound chatLinkBound) {
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
            if (!checkChatExists(chatLinkBound.chat())) {
                throw new NoExpectedEntityInDataBaseException(CHAT_ENTITY_NAME);
            }
            if (!checkLinkExists(chatLinkBound.link())) {
                saveNewLink(chatLinkBound.link());
            }

            final String getTgChatIdQuery = "select id from telegram_chat where chat_id = ? limit 1";
            int telegramChatIdInDatabase = jdbcTemplate.queryForObject(
                getTgChatIdQuery,
                Integer.class,
                chatLinkBound.chat().id()
            );

            final String getLinkIdQuery = "select id from links where url = ? limit 1";
            int linkIdInDatabase = jdbcTemplate.queryForObject(
                getLinkIdQuery,
                Integer.class,
                chatLinkBound.link().uri().toURL().toString()
            );

            final String countSuchPairQuery =
                "select count(*) from track_info where link_id = ? and telegram_chat_id = ?";
            int pairsCount = jdbcTemplate.queryForObject(
                countSuchPairQuery,
                Integer.class,
                linkIdInDatabase, telegramChatIdInDatabase
            );

            if (pairsCount == 1) {
                return false;
            } else if (pairsCount > 1) {
                throw new UnexpectedDataBaseStateException("There are unique pairs in 'track_info' tables!");
            }

            final String insertBoundQuery =
                "insert into track_info(telegram_chat_id, link_id) values (?, ?)";
            System.out.println("Inserting into track_info: [chat_id: " + telegramChatIdInDatabase + ", link_id: " + linkIdInDatabase + "]");
            int affectedRowsCount = jdbcTemplate.update(
                insertBoundQuery,
                telegramChatIdInDatabase, linkIdInDatabase
            );
            return (affectedRowsCount == 1);
        } catch (DuplicateKeyException e) {
            return false;
        } catch (MalformedURLException e) {
            throw new InvalidArgumentForTypeInDataBase(e);
        } catch (DataAccessException | NullPointerException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public @Nullable ChatLinkBound remove(ChatLinkBound chatLinkBound) {
        /*
        Remove bound between chat and link in table 'track_info'.

        Returns:
        - chatLinkBound: if removed,
        - null: otherwise.

        Throws:
        - InvalidArgumentForTypeInDataBase: in case of invalid url value passed;
        - NoExpectedEntityInDataBaseException: if telegram chat not saved yet in table 'telegram_chat';
        - DataBaseInteractingException: if some error occurs when working with JdbcTemplate;
        */

        try {
            if (!checkChatExists(chatLinkBound.chat())) {
                throw new NoExpectedEntityInDataBaseException(CHAT_ENTITY_NAME);
            }

            final String deleteBoundaryRecordQuery =
                "delete " +
                "from track_info " +
                "where telegram_chat_id = (select id from telegram_chat where chat_id = ?) " +
                    "and link_id = (select id from links where url = ?)";
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

    private void saveNewLink(Link link) throws MalformedURLException {
        final String queryToAddLink = "insert into links(url) values (?)";
        jdbcTemplate.update(queryToAddLink, link.uri().toURL().toString());
    }
}
