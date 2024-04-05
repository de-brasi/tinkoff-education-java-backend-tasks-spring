package edu.java.bot.configuration;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.api.util.UpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaConfig {
    private final UpdateHandler updateHandler;

    @Bean
    public NewTopic mainTopic(
        @Value("${app.scrapper-topic.name}")
        String topicName,

        @Value("${app.scrapper-topic.partitions-count}")
        int partitionsCount,

        @Value("${app.scrapper-topic.replicas-count}")
        int replicasCount
    ) {
        return TopicBuilder.name(topicName)
            .partitions(partitionsCount)
            .replicas(replicasCount)
            .build();
    }

    @Bean
    public NewTopic deadLetterQueue(
        @Value("${app.scrapper-topic.name}")
        String topicName,

        @Value("${app.scrapper-topic.partitions-count}")
        int partitionsCount,

        @Value("${app.scrapper-topic.replicas-count}")
        int replicasCount
    ) {
        return TopicBuilder.name(topicName)
            .partitions(partitionsCount)
            .replicas(replicasCount)
            .build();
    }

    @KafkaListener(id = "consumer-group-1", topics = "${app.scrapper-topic.name}")
    public void listen(LinkUpdateRequest in) {
        log.info("Got message from kafka:" + in.toString());
        updateHandler.handleUpdate(in);
    }
}
