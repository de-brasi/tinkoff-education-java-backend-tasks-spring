package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.Link;
import edu.java.domain.repositories.jpa.entities.SupportedService;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JpaLinkRepository extends JpaRepository<Link, Long> {

    @Transactional
    default void saveIfNotExists(
        String url,
        SupportedService service,
        OffsetDateTime lastCheckTime,
        OffsetDateTime lastUpdateTime,
        String snapshot
    ) {
        saveIfNotExists(url, service.getId(),
            Timestamp.from(lastCheckTime.toInstant()),
            Timestamp.from(lastUpdateTime.toInstant()),
            snapshot);
    }

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO links(url, last_check_time, last_update_time, service, snapshot) "
        + "VALUES (:url, :lastCheckTime, :lastUpdateTime, :serviceId, :snapshot) ON CONFLICT DO NOTHING",
           nativeQuery = true)
    void saveIfNotExists(String url, Long serviceId,
        Timestamp lastCheckTime,
        Timestamp lastUpdateTime,
        String snapshot);

    @Transactional
    Optional<Link> getLinkByUrl(String url);

    @Transactional
    void removeLinkByUrl(String url);
}
