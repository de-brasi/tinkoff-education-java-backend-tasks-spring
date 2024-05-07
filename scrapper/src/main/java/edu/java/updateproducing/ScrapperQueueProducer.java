package edu.java.updateproducing;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
@Slf4j
public class ScrapperQueueProducer implements ScrapperUpdateProducer {

    private final String topicName;
    private final KafkaTemplate<String, LinkUpdateRequest> template;

    public void send(LinkUpdateRequest linkUpdateRequest) {
        log.info("Kafka producer got linkUpdateRequest object: " + linkUpdateRequest);
        template.send(topicName, linkUpdateRequest);
    }

}
