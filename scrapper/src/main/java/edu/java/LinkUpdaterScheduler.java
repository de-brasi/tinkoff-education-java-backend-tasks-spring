package edu.java;

import edu.java.domain.entities.Link;
import edu.java.services.interfaces.LinkUpdater;
import edu.java.services.jdbc.JdbcLinkUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Collection;

@Service
@EnableScheduling
public class LinkUpdaterScheduler {
    private final LinkUpdater linkUpdater;

    public LinkUpdaterScheduler(@Autowired JdbcLinkUpdater linkUpdater) {
        this.linkUpdater = linkUpdater;
    }

    @SuppressWarnings("RegexpSinglelineJava")
    @Scheduled(fixedDelayString = "#{@scheduler.interval()}")
    public void update() {
        LOGGER.info("Update");

        Duration checkingDeadline = Duration.ofMinutes(1);
        Collection<Link> linksToCheckForUpdate = linkUpdater.getNotCheckedForAWhile(checkingDeadline);

        System.out.println("Checked links: " + linksToCheckForUpdate);

        int updated = linkUpdater.update(linksToCheckForUpdate);
        LOGGER.info("Updated %d links.".formatted(updated));
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
