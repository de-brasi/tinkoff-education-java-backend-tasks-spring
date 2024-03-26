package edu.java.scrapper.domaintest.jpa;

import edu.java.domain.entities.Link;
import edu.java.domain.repositories.jpa.entities.SupportedService;
import edu.java.domain.repositories.jpa.implementations.JpaLinkRepository;
import edu.java.scrapper.IntegrationTest;
import edu.java.services.ExternalServicesObserver;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.net.MalformedURLException;
import java.net.URI;
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

    @PersistenceContext
    private EntityManager entityManager;

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

        linkRepository.add(testLink.uri().toURL().toString(), stackoverflowService);

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

        linkRepository.add(testLink.uri().toURL().toString(), stackoverflowService);
        linkRepository.remove(testLink.uri().toURL().toString());

        Long suchRowCount = entityManager
            .createQuery("SELECT count(*) from Link link where link.url = :url", Long.class)
            .setParameter("url", testLink.uri().toURL().toString())
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

        linkRepository.add(testLink.uri().toURL().toString(), stackoverflowService);
        var got = linkRepository.get(testLink.uri().toURL().toString());

        assertThat(got.getUrl()).isEqualTo(testLink.uri().toURL().toString());
    }

    @Test
    @Transactional
    @Rollback
    void getNotExistsTest() throws MalformedURLException {
        var got = linkRepository.get(testLink.uri().toURL().toString());

        assertThat(got).isEqualTo(null);
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

        SupportedService stackoverflowService = entityManager.createQuery(
            "select service from SupportedService service where service.name = 'stackoverflow'",
            SupportedService.class
        ).getSingleResult();

        when(externalServicesObserver.getActualSnapshot(any(String.class))).thenReturn("example");

        linkRepository.add(testLink1.uri().toURL().toString(), stackoverflowService);
        linkRepository.add(testLink2.uri().toURL().toString(), stackoverflowService);

        var got = linkRepository.findAll()
            .stream()
            .map(e -> new Link(URI.create(e.getUrl())))
            .collect(Collectors.toList());

        final List<Link> expected = List.of(testLink1, testLink2);

        assertThat(got).containsAll(expected);
    }
}
