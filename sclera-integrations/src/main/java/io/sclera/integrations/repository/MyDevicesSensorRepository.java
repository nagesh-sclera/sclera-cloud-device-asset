package io.sclera.integrations.repository;

import io.sclera.integrations.model.MyDevicesSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyDevicesSensorRepository extends JpaRepository<MyDevicesSensor, String> {
}