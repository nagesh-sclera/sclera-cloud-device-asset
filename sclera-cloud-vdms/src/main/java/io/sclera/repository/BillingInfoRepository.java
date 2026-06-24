package io.sclera.repository;

import io.sclera.dto.BillingInfoDTO;
import io.sclera.model.BillingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

public interface BillingInfoRepository extends JpaRepository<BillingInfo,String> {

    @Query(nativeQuery = true)
    List<BillingInfoDTO> findAllByOrgId(String key,BigInteger currentDate,String status,int pageSize, int offset);

    @Transactional
    @Modifying
    @Query(value = """
                UPDATE billing_info 
                SET total_licensed_assets = ?2, 
                    invoice_number = ?3, 
                    billing_contact = ?4, 
                    billing_start_date = ?5, 
                    billing_end_date = ?6, 
                    saas_term = ?7, 
                    updated_by = ?8, po_tracking = ?9, currency_id = ?10, trial = ?11
                WHERE billing_id = ?1
            """, nativeQuery = true)
    void updateBillingInfoByBillingId(String billingId, Integer totalLicensedAssets, String invoiceNumber, String billingContact,
                                      BigInteger startDate, BigInteger endDate, Integer saasTerm, String loggedInUser, Integer poTracking, String currencyId, Integer trialStatus);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM billing_info WHERE billing_id IN (:billingIds)",nativeQuery = true)
    void deleteBillingInfoByBillingIds(@Param("billingIds")List<String> billingIds);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO billing_info(billing_id,org_id,total_licensed_assets,invoice_number,billing_contact,billing_start_date," +
            "billing_end_date,saas_term,created_by,updated_by,creation_time,po_tracking,currency_id,trial) " +
            "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14)", nativeQuery = true)
    void addBillingInfo(String billingId, String orgId, Integer totalLicensedAssets, String invoiceNumber, String billingContact, BigInteger startDate,
                        BigInteger endDate, Integer saasTerm, String createdBy, String updatedBy, BigInteger creationTime,
                        Integer poTracking, String currencyId, Integer trialStatus);


    @Query(value = "SELECT total_licensed_assets FROM billing_info WHERE billing_id = ?1",nativeQuery = true)
    Integer getTotalLicensedAssets(String billingId);


    @Query(nativeQuery = true)
    BillingInfoDTO findBillingDataByOrgId(String orgId);

}
