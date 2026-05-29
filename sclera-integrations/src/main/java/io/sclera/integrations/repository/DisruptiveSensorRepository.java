package io.sclera.integrations.repository;

import io.sclera.integrations.model.DisruptiveSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisruptiveSensorRepository extends JpaRepository<DisruptiveSensor, String> {
}