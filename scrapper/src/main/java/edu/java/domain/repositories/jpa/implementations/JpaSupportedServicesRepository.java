package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.SupportedService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface JpaSupportedServicesRepository extends JpaRepository<SupportedService, Long> {
    @Transactional
    public Optional<SupportedService> getSupportedServiceByName(String name);
}
