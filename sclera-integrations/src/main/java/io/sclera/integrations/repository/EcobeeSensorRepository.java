package io.sclera.integrations.repository;

import io.sclera.integrations.model.EcobeeSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EcobeeSensorRepository extends JpaRepository<EcobeeSensor, String> {
}