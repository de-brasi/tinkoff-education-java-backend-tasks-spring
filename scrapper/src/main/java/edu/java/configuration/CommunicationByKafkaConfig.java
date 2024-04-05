package edu.java.configuration;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.updateproducing.ScrapperQueueProducer;
import edu.java.updateproducing.ScrapperUpdateProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
public class CommunicationByKafkaConfig {
    @Bean
    ScrapperUpdateProducer scrapperKafkaProducer(
        @Value("#{@kafkaTopic}")
        String topicName,

        KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate
    ) {
        System.out.println("Use queue");
        return new ScrapperQueueProducer(topicName, kafkaTemplate);
    }
}
