package edu.java.bot.configuration;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Slf4j
public class KafkaConfig {
    @Bean
    public NewTopic topic(@Value("#{@kafkaTopic}") String topicName) {
        return TopicBuilder.name(topicName)
            .partitions(10)
            .replicas(1)
            .build();
    }

    @KafkaListener(id = "myId", topics = "topic1")
    public void listen(LinkUpdateRequest in) {
        log.warn(in.toString());
    }
}
