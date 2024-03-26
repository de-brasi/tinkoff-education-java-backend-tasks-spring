package edu.java.configuration;

import edu.java.clients.BotClient;
import edu.java.domain.repositories.jpa.implementations.JpaLinkRepository;
import edu.java.domain.repositories.jpa.implementations.JpaSupportedServicesRepository;
import edu.java.domain.repositories.jpa.implementations.JpaTelegramChatRepository;
import edu.java.services.ExternalServicesObserver;
import edu.java.services.interfaces.LinkService;
import edu.java.services.interfaces.LinkUpdater;
import edu.java.services.interfaces.TgChatService;
import edu.java.services.jpa.JpaLinkService;
import edu.java.services.jpa.JpaLinkUpdater;
import edu.java.services.jpa.JpaTgChatService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JpaAccessConfiguration {
    @Bean
    public LinkService linkService(
        JpaLinkRepository jpaLinkRepository,
        JpaTelegramChatRepository jpaTelegramChatRepository,
        JpaSupportedServicesRepository jpaSupportedServicesRepository
    ) {
        return new JpaLinkService(jpaLinkRepository, jpaTelegramChatRepository, jpaSupportedServicesRepository);
    }

    @Bean
    public LinkUpdater linkUpdater(
        JpaLinkRepository linkRepository,
        ExternalServicesObserver externalServicesObserver,
        BotClient botClient
    ) {
        return new JpaLinkUpdater(linkRepository, externalServicesObserver, botClient);
    }

    @Bean
    public TgChatService tgChatService(JpaTelegramChatRepository jpaTelegramChatRepository) {
        return new JpaTgChatService(jpaTelegramChatRepository);
    }
}
