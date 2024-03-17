package edu.java.services.interfaces;

import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;

public interface LinkUpdater {
    int update(Collection<Link> toUpdate);

    Collection<Link> getNotCheckedForAWhile(Duration duration);

    Collection<TelegramChat> getSubscribers(Link link);

    boolean compareAndSetLastUpdateTime(Link target, OffsetDateTime actualTime);
}
