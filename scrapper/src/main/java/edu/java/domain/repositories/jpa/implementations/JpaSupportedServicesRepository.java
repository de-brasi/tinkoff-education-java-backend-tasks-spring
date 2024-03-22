package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.SupportedService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSupportedServicesRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public SupportedService getService(String name) {
        try {
            return entityManager.createQuery(
                "SELECT service FROM SupportedService service WHERE service.name = :name",
                SupportedService.class
            ).setParameter("name", name).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
