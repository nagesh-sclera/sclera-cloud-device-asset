package io.sclera.integrations.repository;

import io.sclera.integrations.model.Datahoist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatahoistRepository extends JpaRepository<Datahoist, String> {
}