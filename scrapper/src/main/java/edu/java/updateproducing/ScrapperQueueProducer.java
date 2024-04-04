package edu.java.updateproducing;

import edu.java.services.enteties.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
@Slf4j
public class ScrapperQueueProducer implements ScrapperUpdateProducer {

    private final String topicName;
    private final KafkaTemplate<String, String> template;

    public void send(LinkUpdate linkUpdate) {
        log.warn("Link update got: " + linkUpdate);
        // todo: данные нормально передавать
        template.send(topicName, "someMessage");
    }

}
