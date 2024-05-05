package edu.java.configuration;

import edu.java.clients.BotClient;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc", matchIfMissing = true)
public class JdbcAccessConfiguration {
    @Bean
    public LinkService linkService(JdbcChatLinkBoundRepository linkBoundRepository, JdbcLinkRepository linkRepository) {
        return new JdbcLinkService(linkBoundRepository, linkRepository);
    }

    @Bean
    public LinkUpdater linkUpdater(
        JdbcTemplate jdbcTemplate,
        BotClient botClient,
        JdbcLinkRepository jdbcLinkRepository,
        ExternalServicesObserver externalServicesObserver
    ) {
        return new JdbcLinkUpdater(
            jdbcTemplate,
            botClient,
            jdbcLinkRepository,
            externalServicesObserver
        );
    }

    @Bean
    public TgChatService tgChatService(JdbcTelegramChatRepository chatRepository) {
        return new JdbcTgChatService(chatRepository);
    }
}