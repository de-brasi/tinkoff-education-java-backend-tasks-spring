package edu.java.configuration;

import edu.java.domain.entities.ChatLinkBound;
import edu.java.domain.entities.Link;
import edu.java.domain.repositories.jdbc.JdbcChatLinkBoundRepository;
import edu.java.domain.repositories.jdbc.JdbcLinkRepository;
import edu.java.domain.repositories.jdbc.JdbcTelegramChatRepository;
import edu.java.services.ExternalServicesObserver;
import edu.java.services.interfaces.LinkService;
import edu.java.services.interfaces.LinkUpdater;
import edu.java.services.interfaces.TgChatService;
import edu.java.services.jdbc.JdbcLinkService;
import edu.java.services.jdbc.JdbcLinkUpdater;
import edu.java.services.jdbc.JdbcTgChatService;
import edu.java.updateproducing.ScrapperUpdateProducer;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@Configuration
@SuppressWarnings("MultipleStringLiterals")
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc", matchIfMissing = true)
public class JdbcAccessConfig {
    @Bean
    JdbcLinkRepository jdbcLinkRepository(
        JdbcTemplate jdbcTemplate,
        ExternalServicesObserver servicesObserver,
        RowMapper<String> linkUrlStringRowMapper,
        RowMapper<Link> linkUrlRowMapper
    ) {
        return new JdbcLinkRepository(jdbcTemplate, servicesObserver,
            linkUrlStringRowMapper, linkUrlRowMapper);
    }

    @Bean
    JdbcTelegramChatRepository jdbcTelegramChatRepository(
        JdbcTemplate jdbcTemplate,
        RowMapper<Long> chatIdRowMapper
    ) {
        return new JdbcTelegramChatRepository(jdbcTemplate, chatIdRowMapper);
    }

    @Bean
    JdbcChatLinkBoundRepository jdbcChatLinkBoundRepository(
        JdbcTemplate jdbcTemplate,
        JdbcLinkRepository jdbcLinkRepository,
        RowMapper<ChatLinkBound> chatLinkBoundRowMapper,
        RowMapper<Link> linkRowMapper
    ) {
        return new JdbcChatLinkBoundRepository(jdbcTemplate, jdbcLinkRepository,
            chatLinkBoundRowMapper, linkRowMapper);
    }

    @Bean
    public LinkService linkService(JdbcChatLinkBoundRepository linkBoundRepository, JdbcLinkRepository linkRepository) {
        return new JdbcLinkService(linkBoundRepository, linkRepository);
    }

    @Bean
    public LinkUpdater linkUpdater(
        JdbcTemplate jdbcTemplate,
        JdbcLinkRepository jdbcLinkRepository,
        ExternalServicesObserver externalServicesObserver,

        @Autowired
        ScrapperUpdateProducer scrapperUpdateProducer
    ) {
        return new JdbcLinkUpdater(
            jdbcTemplate,
            jdbcLinkRepository,
            externalServicesObserver,
            scrapperUpdateProducer
        );
    }

    @Bean
    public TgChatService tgChatService(JdbcTelegramChatRepository chatRepository) {
        return new JdbcTgChatService(chatRepository);
    }

    @Bean
    public RowMapper<ChatLinkBound> chatLinkBoundRowMapper() {
        return (rs, rowNum) -> {
            Long tgChatId = rs.getLong("chat_id");
            String url = rs.getString("url");
            return new ChatLinkBound(tgChatId, url);
        };
    }

    @Bean
    public RowMapper<String> linkUrlRowMapper() {
        return (rs, rowNum) -> rs.getString("url");
    }

    @Bean
    public RowMapper<Long> chatIdRowMapper() {
        return (rs, rowNum) -> rs.getLong("chat_id");
    }

    @Bean
    public RowMapper<Link> linkRowMapper() {
        return (rs, rowNum) -> {
            final String url = rs.getString("url");
            final Long urlId = rs.getLong("link_id");
            return new Link(urlId, URI.create(url));
        };
    }
}
