package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.Link;
import edu.java.domain.repositories.jpa.entities.SupportedService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class JpaLinkRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final OffsetDateTime DEFAULT_TIME =
        OffsetDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC);

    public void add(String url, SupportedService service) {
        Link link = new Link();
        link.setUrl(url);
        link.setLastCheckTime(DEFAULT_TIME);
        link.setLastUpdateTime(DEFAULT_TIME);
        link.setService(service);
        entityManager.persist(link);
        entityManager.flush();
    }

    @Transactional
    public void remove(String url) {
        Link link = entityManager.createQuery("SELECT link FROM Link link WHERE link.url = :url", Link.class)
            .setParameter("url", url)
            .getSingleResult();
        entityManager.remove(link);
    }

    @Transactional
    public void remove(Link link) {
        entityManager.remove(link);
    }

    @Transactional
    public List<Link> findAll() {
        return entityManager.createQuery("SELECT link from Link link", Link.class).getResultList();
    }
}
