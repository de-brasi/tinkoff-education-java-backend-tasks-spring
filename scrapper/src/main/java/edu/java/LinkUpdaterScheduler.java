package edu.java;

import edu.java.services.interfaces.LinkUpdater;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;
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
        log.info("Updating...");

        Duration checkingDeadline = Duration.ofMinutes(1);

        try {
            int updated = linkUpdater.update(checkingDeadline);
            log.info("Updated %d links.".formatted(updated));
        } catch (Exception e) {
            log.error(("""
                Exception when updating links.
                Failed when getting links for updating with exception: %s
                Message: %s
                Stack trace:
                %s
                """)
                .formatted(
                    e.getClass().getCanonicalName(),
                    e.getMessage(),
                    Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n"))
                )
            );
        }
    }
}
