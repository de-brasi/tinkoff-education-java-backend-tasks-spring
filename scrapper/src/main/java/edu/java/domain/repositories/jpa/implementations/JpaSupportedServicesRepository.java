package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.SupportedService;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JpaSupportedServicesRepository extends JpaRepository<SupportedService, Long> {
    @Transactional
    Optional<SupportedService> getSupportedServiceByName(String name);
}
