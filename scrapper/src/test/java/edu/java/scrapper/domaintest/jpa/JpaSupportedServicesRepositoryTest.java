package edu.java.scrapper.domaintest.jpa;

import edu.java.domain.entities.Link;
import edu.java.domain.repositories.jpa.entities.SupportedService;
import edu.java.domain.repositories.jpa.implementations.JpaLinkRepository;
import edu.java.domain.repositories.jpa.implementations.JpaSupportedServicesRepository;
import edu.java.scrapper.IntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.net.MalformedURLException;
import java.net.URI;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JpaSupportedServicesRepositoryTest extends IntegrationTest {
    @Autowired JpaSupportedServicesRepository supportedServicesRepository;

    @PersistenceContext
    private EntityManager entityManager;

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
