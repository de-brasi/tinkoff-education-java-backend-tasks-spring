package edu.java;

import edu.java.clients.BotClient;
import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
import edu.java.services.interfaces.LinkUpdater;
import edu.java.services.jdbc.JdbcLinkUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
@EnableScheduling
public class LinkUpdaterScheduler {
    private final LinkUpdater linkUpdater;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;

    public LinkUpdaterScheduler(
        @Autowired JdbcLinkUpdater linkUpdater,
        @Autowired GitHubClient gitHubClient,
        @Autowired StackOverflowClient stackOverflowClient,
        @Autowired BotClient botClient
    ) {
        this.linkUpdater = linkUpdater;
        this.stackOverflowClient = stackOverflowClient;
        this.gitHubClient = gitHubClient;
        this.botClient = botClient;
    }

    @SuppressWarnings("RegexpSinglelineJava")
    @Scheduled(fixedDelayString = "#{@scheduler.interval()}")
    public void update() {
        LOGGER.info("Update");

        Duration checkinDeadline = Duration.ofMinutes(1);
        Collection<Link> linksToCheckForUpdate = linkUpdater.getNotCheckedForAWhile(checkinDeadline);

        for (Link link: linksToCheckForUpdate) {
            try {
                final String currentLinkUrl = link.uri().toURL().toString();

                List<Long> subscribers =
                    linkUpdater.getSubscribers(link)
                        .stream()
                        .map(TelegramChat::id)
                        .toList();

                if (currentLinkUrl.startsWith(gitHubClient.getDefaultBaseUrl())) {
                    final OffsetDateTime actualUpdateTime = gitHubClient.fetchUpdate(currentLinkUrl).updateTime();
                    notifyClientsIfUpdatedTimeChanged(actualUpdateTime, link, subscribers);
                } else if (currentLinkUrl.startsWith(stackOverflowClient.getDefaultBaseUrl())) {
                    final OffsetDateTime actualUpdateTime = stackOverflowClient.fetchUpdate(currentLinkUrl).updateTime();
                    notifyClientsIfUpdatedTimeChanged(actualUpdateTime, link, subscribers);
                } else {
                    throw new RuntimeException(
                        "Unexpected API for fetching update for URL %s".formatted(currentLinkUrl)
                    );
                }
            } catch (Exception e) {
                LOGGER.info(
                    ("""
                        Exception when checking update of link %s;
                        Exception: %s
                        Message: %s
                        Stacktrace:
                        %s
                        """).formatted(link, e.getClass().getCanonicalName(), e.getMessage(),
                        Arrays.stream(e.getStackTrace())
                            .map(StackTraceElement::toString)
                            .toList()
                    ));
            }
        }
    }

    private void notifyClientsIfUpdatedTimeChanged(OffsetDateTime actualTime, Link link, List<Long> subscribersId)
        throws MalformedURLException {
        final boolean timesEqual = linkUpdater.compareAndSetLastUpdateTime(link, actualTime);
        if (!timesEqual) {
            // todo: использовать id ссылки, пока заглушка
            botClient.sendUpdates(
                -1,
                link.uri().toURL().toString(),
                "updated",
                subscribersId
            );
        }
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
