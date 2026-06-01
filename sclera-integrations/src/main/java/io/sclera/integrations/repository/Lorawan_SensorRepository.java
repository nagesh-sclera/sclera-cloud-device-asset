package io.sclera.integrations.repository;

import io.sclera.integrations.model.Lorawan_Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Lorawan_SensorRepository extends JpaRepository<Lorawan_Sensor, String> {
}