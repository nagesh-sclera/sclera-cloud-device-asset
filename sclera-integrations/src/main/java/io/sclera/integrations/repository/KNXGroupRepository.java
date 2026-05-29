package io.sclera.integrations.repository;

import io.sclera.integrations.model.KNXGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KNXGroupRepository extends JpaRepository<KNXGroup, String> {
}