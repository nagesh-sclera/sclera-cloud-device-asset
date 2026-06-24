package io.sclera.repository;

import io.sclera.model.TrustedOrigin;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrustedOriginRepository extends JpaRepository<TrustedOrigin, String> {

    @Query(value = "SELECT origin FROM trusted_origin", nativeQuery = true)
    List<String> getAllTrustedOrigin();

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO trusted_origin(origin) VALUES(?1)", nativeQuery = true)
    void addOrigin(String origin);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM trusted_origin WHERE origin IN ?1", nativeQuery = true)
    void deleteOrigin(List<String> origins);
}
