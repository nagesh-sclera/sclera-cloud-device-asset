package io.sclera.vdms.repository;

import io.sclera.vdms.model.Vdms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface VdmsJpaRepository extends JpaRepository<Vdms, String> {

    @Query(value = "SELECT * FROM vdms LIMIT 1", nativeQuery = true)
    Optional<Vdms> findFirst();

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET customer_org_id = :customerOrgId WHERE id = :vdmsId", nativeQuery = true)
    void updateCustomerOrgId(@Param("vdmsId") String vdmsId, @Param("customerOrgId") String customerOrgId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET address = :address WHERE id = :vdmsId", nativeQuery = true)
    void updateAddress(@Param("vdmsId") String vdmsId, @Param("address") String address);
}
