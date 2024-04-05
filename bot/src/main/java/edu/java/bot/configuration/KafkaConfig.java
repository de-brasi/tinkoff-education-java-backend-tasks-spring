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
    public NewTopic topic(@Value("${app.scrapper-topic.name}") String topicName) {
        return TopicBuilder.name(topicName)
            .partitions(10)
            .replicas(1)
            .build();
    }

    @KafkaListener(id = "consumer-group-1", topics = "${app.scrapper-topic.name}")
    public void listen(LinkUpdateRequest in) {
        log.warn("Начало обработки");
        log.warn(in.toString());
        updateHandler.handleUpdate(in);
        log.warn("Конец обработки");
    }
}
