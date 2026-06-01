package io.sclera.integrations.repository;

import io.sclera.integrations.model.Lorawan_Sensor_Attributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Lorawan_Sensor_AttributesRepository extends JpaRepository<Lorawan_Sensor_Attributes, String> {
}