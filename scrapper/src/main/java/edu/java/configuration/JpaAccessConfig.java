package edu.java.configuration;

import edu.java.data.domain.repositories.jpa.implementations.JpaLinkRepository;
import edu.java.data.domain.repositories.jpa.implementations.JpaSupportedServicesRepository;
import edu.java.data.domain.repositories.jpa.implementations.JpaTelegramChatRepository;
import edu.java.data.domain.repositories.jpa.implementations.JpaTrackInfoRepository;
import edu.java.data.services.ExternalServicesObserver;
import edu.java.data.services.interfaces.LinkService;
import edu.java.data.services.interfaces.LinkUpdater;
import edu.java.data.services.interfaces.TgChatService;
import edu.java.data.services.jpa.JpaLinkService;
import edu.java.data.services.jpa.JpaLinkUpdater;
import edu.java.data.services.jpa.JpaTgChatService;
import edu.java.updateproducing.ScrapperUpdateProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JpaAccessConfig {
    @Bean
    public LinkService linkService(
        JpaLinkRepository jpaLinkRepository,
        JpaTelegramChatRepository jpaTelegramChatRepository,
        JpaSupportedServicesRepository jpaSupportedServicesRepository,
        JpaTrackInfoRepository jpaTrackInfoRepository,
        ExternalServicesObserver externalServicesObserver
    ) {
        return new JpaLinkService(
            jpaLinkRepository,
            jpaTelegramChatRepository,
            jpaSupportedServicesRepository,
            jpaTrackInfoRepository,
            externalServicesObserver
        );
    }

    @Bean
    public LinkUpdater linkUpdater(
        JpaLinkRepository linkRepository,
        ExternalServicesObserver externalServicesObserver,

        @Autowired
        ScrapperUpdateProducer scrapperUpdateProducer
    ) {
        return new JpaLinkUpdater(linkRepository, externalServicesObserver, scrapperUpdateProducer);
    }

    @Bean
    public TgChatService tgChatService(JpaTelegramChatRepository jpaTelegramChatRepository) {
        return new JpaTgChatService(jpaTelegramChatRepository);
    }
}
