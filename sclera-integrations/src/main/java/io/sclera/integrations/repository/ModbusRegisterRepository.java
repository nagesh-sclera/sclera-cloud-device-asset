package io.sclera.integrations.repository;

import io.sclera.integrations.model.ModbusRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModbusRegisterRepository extends JpaRepository<ModbusRegister, String> {
}