package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.Link;
import edu.java.domain.repositories.jpa.entities.SupportedService;
import jakarta.persistence.PersistenceException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JpaLinkRepository extends JpaRepository<Link, Long> {

    @Transactional
    default Optional<Link> add(String url, SupportedService service, OffsetDateTime lastCheckTime, OffsetDateTime lastUpdateTime, String snapshot) {
        try {
            Link link = new Link();
            link.setUrl(url);
            link.setLastCheckTime(lastCheckTime);
            link.setLastUpdateTime(lastUpdateTime);
            link.setService(service);
            link.setSnapshot(snapshot);

            save(link);
            return Optional.of(link);
        } catch (PersistenceException ignored) {
            return Optional.empty();
        }
    }

    @Transactional
    Optional<Link> getLinkByUrl(String url);

    @Transactional
    void removeLinkByUrl(String url);

    @Transactional
    void removeLink(Link link);

    @Transactional
    List<Link> getAll();

}
