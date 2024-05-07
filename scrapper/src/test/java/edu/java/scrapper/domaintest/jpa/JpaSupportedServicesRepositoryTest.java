package edu.java.scrapper.domaintest.jpa;

import edu.java.configuration.KafkaConfig;
import edu.java.domain.repositories.jpa.implementations.JpaSupportedServicesRepository;
import edu.java.scrapper.IntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "app.database-access-type=jpa",
    "third-party-web-clients.github-properties.timeout-in-milliseconds=1000",
    "third-party-web-clients.stackoverflow-properties.timeout-in-milliseconds=1000",
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
    KafkaConfig kafkaConfig;

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
        var stackoverflow = supportedServicesRepository.getSupportedServiceByName("stackoverflow");
        assertThat(stackoverflow.orElseThrow().getName()).isEqualTo("stackoverflow");
    }

    @Test
    @Transactional
    @Rollback
    void checkGithub() {
        var gh = supportedServicesRepository.getSupportedServiceByName("github");
        assertThat(gh.orElseThrow().getName()).isEqualTo("github");
    }
}
