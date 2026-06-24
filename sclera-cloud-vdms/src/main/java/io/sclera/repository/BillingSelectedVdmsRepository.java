package io.sclera.repository;

import io.sclera.model.BillingSelectedVdms;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;

public interface BillingSelectedVdmsRepository extends JpaRepository<BillingSelectedVdms,String> {

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO billing_selected_vdms(id,billing_id,vdms_id) VALUES(?1,?2,?3)",nativeQuery = true)
    void addBillingSelectedVdms(String billingSelectedVdmsId, String billingId, String vdmsId);

    @Query(value="SELECT vdms_id FROM billing_selected_vdms WHERE billing_id = ?1",nativeQuery = true)
    List<String> findVdmsIdsByBillingId(String billingId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM billing_selected_vdms WHERE billing_id = :billingId and vdms_id IN (:vdmsIds)",nativeQuery = true)
    void deleteByBillingIdAndVdmsIds(@Param("billingId") String billingId,@Param("vdmsIds")ArrayList<String> vdmsIds);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM billing_selected_vdms WHERE billing_id IN (:billingIds)",nativeQuery = true)
    void deleteSelectedVdms(@Param("billingIds") List<String> billingIds);
}
