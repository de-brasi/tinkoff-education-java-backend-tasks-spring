package edu.java.updateproducing;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
public class ScrapperQueueProducer implements ScrapperUpdateProducer {
//    public void send(LinkUpdate update) {
//        // TODO
//    }

    private final String topicName;
    private final KafkaTemplate<String, String> template;

    public void send(String someMessage) {
        // TODO: topic name
        template.send(topicName, someMessage);
    }

}
