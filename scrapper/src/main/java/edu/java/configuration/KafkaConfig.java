package edu.java.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// producer
@Configuration
@Slf4j
public class KafkaConfig {
    @Bean
    public NewTopic topic(
        @Value("${app.topic.name}")
        String topicName,

        @Value("${app.topic.partitions-count}")
        int partitionsCount,

        @Value("${app.topic.replicas-count}")
        int replicasCount
    ) {
        return TopicBuilder.name(topicName)
            .partitions(partitionsCount)
            .replicas(replicasCount)
            .build();
    }
}
