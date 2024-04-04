package edu.java.configuration;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.clients.BotClient;
import edu.java.updateproducing.ScrapperHttpProducer;
import edu.java.updateproducing.ScrapperQueueProducer;
import edu.java.updateproducing.ScrapperUpdateProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class UpdateProducerConfig {
    @Bean("scrapperKafkaProducer")
    ScrapperUpdateProducer scrapperKafkaProducer(
        @Value("#{@kafkaTopic}")
        String topicName,

        KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate
    ) {
        return new ScrapperQueueProducer(topicName, kafkaTemplate);
    }

    @Bean("scrapperHttpProducer")
    ScrapperUpdateProducer scrapperHttpProducer(BotClient botClient) {
        return new ScrapperHttpProducer(botClient);
    }
}
