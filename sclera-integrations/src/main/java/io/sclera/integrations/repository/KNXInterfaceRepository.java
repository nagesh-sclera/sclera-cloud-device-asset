package io.sclera.integrations.repository;

import io.sclera.integrations.model.KNXInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KNXInterfaceRepository extends JpaRepository<KNXInterface, String> {
}