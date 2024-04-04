package edu.java;

import edu.java.services.interfaces.LinkUpdater;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class LinkUpdaterScheduler {
    private final LinkUpdater linkUpdater;

    @SuppressWarnings("RegexpSinglelineJava")
    @Scheduled(fixedDelayString = "#{@scheduler.interval()}")
    public void update() {
        log.info("Update");

        Duration checkingDeadline = Duration.ofMinutes(1);

        int updated = linkUpdater.update(checkingDeadline);
        log.info("Updated %d links.".formatted(updated));
    }
}
