package io.sclera.integrations.repository;

import io.sclera.integrations.model.DaintreeDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DaintreeDeviceRepository extends JpaRepository<DaintreeDevice, String> {
}