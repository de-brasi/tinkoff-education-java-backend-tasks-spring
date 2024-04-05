package edu.java.scrapper.domaintest.jpa;

import edu.java.LinkUpdaterScheduler;
import edu.java.api.LinksController;
import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import edu.java.configuration.KafkaConfig;
import edu.java.domain.entities.Link;
import edu.java.domain.repositories.jdbc.JdbcChatLinkBoundRepository;
import edu.java.domain.repositories.jdbc.JdbcLinkRepository;
import edu.java.domain.repositories.jdbc.JdbcTelegramChatRepository;
import edu.java.domain.repositories.jpa.entities.SupportedService;
import edu.java.domain.repositories.jpa.implementations.JpaLinkRepository;
import edu.java.domain.repositories.jpa.implementations.JpaSupportedServicesRepository;
import edu.java.scrapper.IntegrationTest;
import edu.java.services.ExternalServicesObserver;
import edu.java.services.interfaces.LinkUpdater;
import edu.java.updateproducing.ScrapperHttpProducer;
import edu.java.updateproducing.ScrapperQueueProducer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import java.net.MalformedURLException;
import java.net.URI;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "app.database-access-type=jpa",
    "app.github-client-settings.timeout-in-milliseconds=1000",
    "app.stackoverflow-client-settings.timeout-in-milliseconds=1000",
    "app.use-queue=false",
    "app.topic.name='some'",
    "app.topic.partitions-count=1",
    "app.topic.replicas-count=2",
    "app.scheduler.enable=false",
    "app.scheduler.force-check-delay=1000",
    "app.scheduler.interval=1000"
})
public class JpaSupportedServicesRepositoryTest extends IntegrationTest {
    @Autowired JpaSupportedServicesRepository supportedServicesRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @MockBean
    LinkUpdaterScheduler linkUpdaterScheduler;

    @MockBean
    LinkUpdater linkUpdater;

    @MockBean
    LinksController linksController;

    @MockBean
    JdbcChatLinkBoundRepository jdbcChatLinkBoundRepository;

    @MockBean
    JdbcTelegramChatRepository jdbcTelegramChatRepository;

    @MockBean
    JdbcLinkRepository jdbcLinkRepository;

    @MockBean
    ExternalServicesObserver externalServicesObserver;

    @MockBean
    GitHubClient gitHubClient;

    @MockBean
    StackOverflowClient stackOverflowClient;

    @MockBean
    ScrapperQueueProducer scrapperQueueProducer;

    @MockBean
    ScrapperHttpProducer scrapperHttpProducer;

    @MockBean KafkaConfig kafkaConfig;

    @Test
    @Transactional
    @Rollback
    void checkAllServices() {
        long suchRowCount = entityManager
            .createQuery("SELECT count(*) from SupportedService service", Long.class)
            .getSingleResult();
        assertThat(suchRowCount).isEqualTo(2);
    }

    @Test
    @Transactional
    @Rollback
    void checkStackoverflow() {
        var stackoverflow = supportedServicesRepository.getService("stackoverflow");
        assertThat(stackoverflow.getName()).isEqualTo("stackoverflow");
    }

    @Test
    @Transactional
    @Rollback
    void checkGithub() {
        var gh = supportedServicesRepository.getService("github");
        assertThat(gh.getName()).isEqualTo("github");
    }
}
