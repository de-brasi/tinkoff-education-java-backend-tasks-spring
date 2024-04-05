package edu.java.configuration;

import edu.java.clients.BotClient;
import edu.java.updateproducing.ScrapperHttpProducer;
import edu.java.updateproducing.ScrapperUpdateProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "false")
public class CommunicationByHttpConfig {
    @Bean
    ScrapperUpdateProducer scrapperHttpProducer(BotClient botClient) {
        System.out.println("Use http");
        return new ScrapperHttpProducer(botClient);
    }
}
