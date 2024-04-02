package edu.java;

import edu.java.services.interfaces.LinkUpdater;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class LinkUpdaterScheduler {
    private final LinkUpdater linkUpdater;
    private final KafkaTemplate<String, String> template;

    @SuppressWarnings("RegexpSinglelineJava")
    @Scheduled(fixedDelayString = "#{@scheduler.interval()}")
    public void update() {
        // kafka message
        LOGGER.warn("Send message to topic '%s' with data '%s'".formatted("topic1", "test"));
        template.send("topic1", "test");

        LOGGER.info("Update");

        Duration checkingDeadline = Duration.ofMinutes(1);

        int updated = linkUpdater.update(checkingDeadline);
        LOGGER.info("Updated %d links.".formatted(updated));
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
