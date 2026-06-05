package io.sclera.Repository;

import io.sclera.models.Interface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Manages persistence and querying of {@link Interface} entities.
 */
@Repository
public interface InterfaceRepository extends JpaRepository<Interface, String> {
}
