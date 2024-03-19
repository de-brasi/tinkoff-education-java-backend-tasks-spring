package edu.java.services;

import edu.java.clients.ExternalServiceClient;
import edu.java.clients.exceptions.EmptyResponseBodyException;
import edu.java.clients.exceptions.FieldNotFoundException;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public class ExternalServicesObserver {
    private final List<ExternalServiceClient> services;

    public ExternalServicesObserver(Collection<ExternalServiceClient> services) {
        this.services = services
            .stream()
            .toList();
    }

    public boolean checkURLSupported(String url) {
        return services
            .stream()
            .anyMatch(
                s -> s.checkURLSupportedByService(url)
            );
    }

    public boolean checkUpdateTimeChanged(String url, OffsetDateTime storedTime) {
        final ExternalServiceClient relatedService = services
            .stream()
            .filter(s -> s.checkURLSupportedByService(url))
            .findFirst()
            .orElseThrow();

        try {
            final OffsetDateTime actualUpdateTime = relatedService.fetchUpdate(url).updateTime();
            return storedTime.isBefore(actualUpdateTime);
        } catch (EmptyResponseBodyException | FieldNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getChangingDescription(String url, /* todo: type */ String oldSnapshot) {
        // todo:
        return null;
    }

    public /* todo: type */ String getActualSnapshot(String url) {
        // todo
        return null;
    }
}
