package io.sclera.integrations.repository;

import io.sclera.integrations.model.ConnectedDevices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectedDevicesRepository extends JpaRepository<ConnectedDevices, String> {
}