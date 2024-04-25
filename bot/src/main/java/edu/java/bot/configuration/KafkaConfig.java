package edu.java.bot.configuration;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.api.util.UpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
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
        ApplicationConfig.KafkaSettings kafkaSettings
    ) {
        var topicSettings = kafkaSettings.topics().scrapperTopic();

        return TopicBuilder.name(topicSettings.name())
            .partitions(topicSettings.partitionsCount())
            .replicas(topicSettings.replicasCount())
            .build();
    }

    @Bean
    public NewTopic deadLetterQueue(
        ApplicationConfig.KafkaSettings dlqSettings
    ) {
        var topicSettings = dlqSettings.topics().scrapperTopic();

        return TopicBuilder.name(topicSettings.name())
            .partitions(topicSettings.partitionsCount())
            .replicas(topicSettings.replicasCount())
            .build();
    }

//    @KafkaListener(id = "consumer-group-1", topics = "${app.scrapper-topic.name}")
    @KafkaListener(id = "consumer-group-1", topics = "topic1")
    public void listen(LinkUpdateRequest in) {
        log.info("Got message from kafka:" + in.toString());
        updateHandler.handleUpdate(in);
    }
}
