package edu.java.data.services;

import edu.java.clients.ExternalServiceClient;
import edu.java.clients.exceptions.EmptyResponseBodyException;
import edu.java.clients.exceptions.FieldNotFoundException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
        final ExternalServiceClient relatedService = getSuitableServiceOrThrow(url);

        try {
            return relatedService.fetchUpdate(url).updateTime().withOffsetSameInstant(ZoneOffset.UTC);
        } catch (EmptyResponseBodyException | FieldNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRelativeServiceNameInDatabase(String url) {
        return getSuitableServiceOrThrow(url).getServiceNameInDatabase();
    }

    public String getChangingDescription(String url, String oldSnapshot, String actualSnapshot) {
        return getSuitableServiceOrThrow(url).getChangeDescriptionFromResponseBodies(oldSnapshot, actualSnapshot);
    }

    public String getActualSnapshot(String url) {
        return getSuitableServiceOrThrow(url).getBodyJSONContent(url);
    }

    private ExternalServiceClient getSuitableServiceOrThrow(String url) {
        return services
            .stream()
            .filter(s -> s.checkURLSupportedByService(url))
            .findFirst()
            .orElseThrow();
    }
}
