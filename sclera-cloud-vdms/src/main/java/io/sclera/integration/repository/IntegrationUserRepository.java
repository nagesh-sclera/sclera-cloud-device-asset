package io.sclera.integration.repository;

import io.sclera.integration.model.IntegrationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationUserRepository extends JpaRepository<IntegrationUser ,String> {

    @Query(value = "SELECT customer_org_id FROM integration_user WHERE username = ?1" ,nativeQuery = true)
    String getOrgIdByUsername(String username);
}
