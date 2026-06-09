package io.sclera.scheduler.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VdmsRegistryRepository extends JpaRepository<VdmsRegistryEntity, String> {
    List<VdmsRegistryEntity> findByActiveTrue();
}
