package edu.java.updateproducing;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScrapperQueueProducer implements ScrapperUpdateProducer {
//    public void send(LinkUpdate update) {
//        // TODO
//    }

    private final KafkaTemplate<String, String> template;

    @Value("#{@kafkaTopic}")
    private String topicName;

    public void send(String someMessage) {
        // TODO: topic name
        template.send(topicName, someMessage);
    }

}
