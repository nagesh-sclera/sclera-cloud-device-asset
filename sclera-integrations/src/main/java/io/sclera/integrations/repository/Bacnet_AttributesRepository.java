package io.sclera.integrations.repository;

import io.sclera.integrations.model.Bacnet_Attributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Bacnet_AttributesRepository extends JpaRepository<Bacnet_Attributes, String> {
}