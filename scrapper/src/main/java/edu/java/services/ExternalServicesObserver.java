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

    public OffsetDateTime getActualUpdateTime(String url) {
        final ExternalServiceClient relatedService = services
            .stream()
            .filter(s -> s.checkURLSupportedByService(url))
            .findFirst()
            .orElseThrow();

        try {
            return relatedService.fetchUpdate(url).updateTime();
        } catch (EmptyResponseBodyException | FieldNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRelativeServiceNameInDatabase(String url) {
        final ExternalServiceClient relatedService = services
            .stream()
            .filter(s -> s.checkURLSupportedByService(url))
            .findFirst()
            .orElseThrow();

        return relatedService.getServiceNameInDatabase();
    }

    public String getChangingDescription(String url, String oldSnapshot) {
        // todo:
        return null;
    }

    public String getActualSnapshot(String url) {
        final ExternalServiceClient relatedService = services
            .stream()
            .filter(s -> s.checkURLSupportedByService(url))
            .findFirst()
            .orElseThrow();

        return relatedService.getBodyJSONContent(url);
    }
}
