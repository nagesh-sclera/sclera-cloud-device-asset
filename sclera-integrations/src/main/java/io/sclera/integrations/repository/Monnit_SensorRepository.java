package io.sclera.integrations.repository;

import io.sclera.integrations.model.Monnit_Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Monnit_SensorRepository extends JpaRepository<Monnit_Sensor, String> {
}