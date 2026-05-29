package io.sclera.integrations.repository;

import io.sclera.integrations.model.PelicanSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PelicanSensorRepository extends JpaRepository<PelicanSensor, String> {
}