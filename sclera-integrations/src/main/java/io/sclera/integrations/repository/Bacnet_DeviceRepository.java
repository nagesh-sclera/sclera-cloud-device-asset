package io.sclera.integrations.repository;

import io.sclera.integrations.model.Bacnet_Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Bacnet_DeviceRepository extends JpaRepository<Bacnet_Device, String> {
}