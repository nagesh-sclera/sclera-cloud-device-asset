package io.sclera.Repository;

import io.sclera.models.Interface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterfaceRepository extends JpaRepository<Interface, String> {
}
