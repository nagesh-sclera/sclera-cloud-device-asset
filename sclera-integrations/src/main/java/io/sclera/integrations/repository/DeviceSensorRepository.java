package io.sclera.integrations.repository;

import io.sclera.integrations.model.DeviceSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceSensorRepository extends JpaRepository<DeviceSensor, String> {
    @Query("SELECT s FROM DeviceSensor s WHERE s.device_id = ?1 ORDER BY s.created_timestamp DESC")
    List<DeviceSensor> findForDevice(String deviceId);
}
