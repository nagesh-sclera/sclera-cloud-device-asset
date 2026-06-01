package io.sclera.integrations.repository;

import io.sclera.integrations.model.LorawanConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LorawanConfigurationRepository extends JpaRepository<LorawanConfiguration, String> {
}