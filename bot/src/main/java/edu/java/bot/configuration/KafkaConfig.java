package edu.java.bot.configuration;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.api.util.UpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

@Configuration
@ConditionalOnProperty(prefix = "app.kafka-settings", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class KafkaConfig {
    private final UpdateHandler updateHandler;
    private final KafkaTemplate<String, LinkUpdateRequest> dlqKafkaTemplate;

    @Bean
    @ConditionalOnProperty(prefix = "app.kafka-settings.topics.scrapper-topic", name = "enabled", havingValue = "true")
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
    @ConditionalOnProperty(prefix = "app.kafka-settings.topics.dead-letter-queue-topic",
                           name = "enabled",
                           havingValue = "true")
    public NewTopic deadLetterQueue(
        ApplicationConfig.KafkaSettings dlqSettings
    ) {
        var topicSettings = dlqSettings.topics().scrapperTopic();

        return TopicBuilder.name(topicSettings.name())
            .partitions(topicSettings.partitionsCount())
            .replicas(topicSettings.replicasCount())
            .build();
    }

    @RetryableTopic(attempts = "1", dltStrategy = DltStrategy.FAIL_ON_ERROR, dltTopicSuffix = "_dlq")
    @KafkaListener(id = "consumer-group-1", topics = "${app.kafka-settings.topics.scrapper-topic.name}")
    public void processLinkUpdateRequest(LinkUpdateRequest in) {
        log.info("Got message from kafka:" + in.toString());
        updateHandler.handleUpdate(in);
    }

    @DltHandler
    public void handleDltMessage(
        LinkUpdateRequest linkUpdateRequest,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topicName
    ) {
       log.info("LinkUpdateRequest on dead letters queue; topic={}, content={}", topicName, linkUpdateRequest);
       dlqKafkaTemplate.send(topicName + "_dlq", linkUpdateRequest);
    }
}
