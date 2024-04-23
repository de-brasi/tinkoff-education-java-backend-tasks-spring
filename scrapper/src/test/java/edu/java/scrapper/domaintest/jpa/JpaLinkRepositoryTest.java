package edu.java.scrapper.domaintest.jpa;

import edu.java.domain.entities.Link;
import edu.java.domain.repositories.jpa.entities.SupportedService;
import edu.java.domain.repositories.jpa.implementations.JpaLinkRepository;
import edu.java.domain.repositories.jpa.implementations.JpaSupportedServicesRepository;
import edu.java.scrapper.IntegrationTest;
import edu.java.services.ExternalServicesObserver;
import edu.java.services.jpa.JpaLinkUpdater;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class JpaLinkRepositoryTest extends IntegrationTest {
    @Autowired JpaLinkRepository linkRepository;

    @MockBean
    ExternalServicesObserver externalServicesObserver;

    @MockBean
    JpaLinkUpdater jpaLinkUpdater;

    @PersistenceContext
    private EntityManager entityManager;

    final OffsetDateTime INITIAL_TIME = OffsetDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC);

    final Link testLink = new Link(URI.create(
        "https://stackoverflow.com/questions/70914106/" +
            "show-multiple-descriptions-for-a-response-code-using-springdoc-openapi-for-a-spr"
    ));

    @Test
    @Transactional
    @Rollback
    void addURLTest() throws MalformedURLException {
        SupportedService stackoverflowService = entityManager.createQuery(
            "select service from SupportedService service where service.name = 'stackoverflow'",
            SupportedService.class
        ).getSingleResult();

        final String urlString = testLink.uri().toURL().toString();

        linkRepository.saveIfNotExists(urlString, stackoverflowService, INITIAL_TIME, INITIAL_TIME, "");

        Long suchRowCount = entityManager
            .createQuery("SELECT count(*) from Link link where link.url = :url", Long.class)
            .setParameter("url", testLink.uri().toURL().toString())
            .getSingleResult();
        assertThat(suchRowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addURLTwiceTest() throws MalformedURLException {
        SupportedService stackoverflowService = entityManager.createQuery(
            "select service from SupportedService service where service.name = 'stackoverflow'",
            SupportedService.class
        ).getSingleResult();

        final String urlString = testLink.uri().toURL().toString();

        linkRepository.saveIfNotExists(urlString, stackoverflowService, INITIAL_TIME, INITIAL_TIME, "");
        linkRepository.saveIfNotExists(urlString, stackoverflowService, INITIAL_TIME, INITIAL_TIME, "");

        Long suchRowCount = entityManager
            .createQuery("SELECT count(*) from Link link where link.url = :url", Long.class)
            .setParameter("url", testLink.uri().toURL().toString())
            .getSingleResult();
        assertThat(suchRowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() throws MalformedURLException {
        SupportedService stackoverflowService = entityManager.createQuery(
            "select service from SupportedService service where service.name = 'stackoverflow'",
            SupportedService.class
        ).getSingleResult();

        final String urlString = testLink.uri().toURL().toString();
        linkRepository.saveIfNotExists(urlString, stackoverflowService, INITIAL_TIME, INITIAL_TIME, "");
        linkRepository.removeLinkByUrl(urlString);

        Long suchRowCount = entityManager
            .createQuery("SELECT count(*) from Link link where link.url = :url", Long.class)
            .setParameter("url", urlString)
            .getSingleResult();
        assertThat(suchRowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void getTest() throws MalformedURLException {
        SupportedService stackoverflowService = entityManager.createQuery(
            "select service from SupportedService service where service.name = 'stackoverflow'",
            SupportedService.class
        ).getSingleResult();

        final String urlString = testLink.uri().toURL().toString();
        linkRepository.saveIfNotExists(urlString, stackoverflowService, INITIAL_TIME, INITIAL_TIME, "");
        var got = linkRepository.getLinkByUrl(urlString);

        assertThat(got.orElseThrow().getUrl()).isEqualTo(urlString);
    }

    @Test
    @Transactional
    @Rollback
    void getNotExistsTest() throws MalformedURLException {
        final String urlString = testLink.uri().toURL().toString();
        var got = linkRepository.getLinkByUrl(urlString);

        assertThat(got.isPresent()).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void findAllTest() throws MalformedURLException {
        final Link testLink1 = new Link(URI.create(
            "https://stackoverflow.com/questions/2"
        ));
        final Link testLink2 = new Link(URI.create(
            "https://stackoverflow.com/questions/1"
        ));

        final String testLinkUrl1 = testLink1.uri().toURL().toString();
        final String testLinkUrl2 = testLink2.uri().toURL().toString();

        SupportedService stackoverflowService = entityManager.createQuery(
            "select service from SupportedService service where service.name = 'stackoverflow'",
            SupportedService.class
        ).getSingleResult();

        when(externalServicesObserver.getActualSnapshot(any(String.class))).thenReturn("example");

        linkRepository.saveIfNotExists(testLinkUrl1, stackoverflowService, INITIAL_TIME, INITIAL_TIME, "");
        linkRepository.saveIfNotExists(testLinkUrl2, stackoverflowService, INITIAL_TIME, INITIAL_TIME, "");

        var got = linkRepository.findAll()
            .stream()
            .map(e -> new Link(URI.create(e.getUrl())))
            .collect(Collectors.toList());

        final List<Link> expected = List.of(testLink1, testLink2);

        assertThat(got).containsAll(expected);
    }
}
