package edu.java.bot.configuration;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.api.util.UpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
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
        @Qualifier("scrapperTopic")
        ApplicationConfig.KafkaTopicSettings scrapperTopic
    ) {
        return TopicBuilder.name(scrapperTopic.name())
            .partitions(scrapperTopic.partitionsCount())
            .replicas(scrapperTopic.replicasCount())
            .build();
    }

    @Bean
    public NewTopic deadLetterQueue(
        @Qualifier("deadLetterQueueTopic")
        ApplicationConfig.KafkaTopicSettings deadLetterQueueTopic
    ) {
        return TopicBuilder.name(deadLetterQueueTopic.name())
            .partitions(deadLetterQueueTopic.partitionsCount())
            .replicas(deadLetterQueueTopic.replicasCount())
            .build();
    }

    @KafkaListener(id = "consumer-group-1", topics = "${app.scrapper-topic.name}")
    public void listen(LinkUpdateRequest in) {
        log.info("Got message from kafka:" + in.toString());
        updateHandler.handleUpdate(in);
    }
}
