package io.sclera.repository;

import io.sclera.dto.*;
import io.sclera.model.Vdms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Repository
public interface VdmsRepository extends JpaRepository<Vdms, String> {

    @Query(nativeQuery = true)
    VdmsDTO getVdmsInfoByVdmsId(String vdms_id);

    @Query(nativeQuery = true)
    List<VdmsDTO> getAllVdmsInfoByOrganisationId(String organisationId);

    @Query(nativeQuery = true)
    Set<VdmsDTO> getAllVdmsInfoByMasterUserOrganisationId(String organisation_id, String key, String sort, int pageSize, int offset);

    @Query(nativeQuery = true)
    Set<VdmsDTO> getAllVdmsInfoByUserOrganisationIdAndUserEmail(String organisation_id, String email, String key, String sort, int pageSize, int offset);

    @Query(nativeQuery = true)
    Set<VdmsDTO> getAllVdmsInfoByMasterVendorOrganisationId(String orgId, String key, String sort, int pageSize, int offset);

    @Query(nativeQuery = true)
    Set<VdmsDTO> getAllVdmsInfoByVendorOrganisationIdAndVendorEmail(String organisation_id, String email, String key, String sort, int pageSize, int offset);

    @Query(nativeQuery = true)
    Set<VdmsDTO> getAllVdmsInfo(String key, String sort, int pageSize, int offset);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO vdms(id,property_name ,image_url ,longitude,latitude, creation_timestamp ,address_id " +
            ",customer_org_id,deployment_type,trial_status,trial_start_date,trial_end_date,region,aws_region) " +
            "VALUES(?1,?2,?3,?4,?5,?6,?7,?8,IFNULL(?9, 'on_premises'),?10,?11,?12,?13,?14)", nativeQuery = true)
    void addVdmsByUserOrganisationId(String id, String property_name, String image_url, String longitude, String latitude, BigInteger creation_time,
                                     String address_id, String organisation_id, String deploymentType,
                                     Boolean trialStatus,BigInteger trialStartDate,BigInteger trialEndDate,String region,String awsRegion);
    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET property_name = ?1 ,image_url = ?2 ,longitude = IFNULL(?3 ,longitude) ,latitude = IFNULL(?4 ,latitude) ," +
            " deployment_type = IFNULL(?5, 'on_premises'),trial_status = ?6,trial_start_date = ?7,trial_end_date = ?8, region = ?9, aws_region = ?10, " +
            " is_multi_tenant = ?11 WHERE id = ?12", nativeQuery = true)
    void editVdmsByVdmsId(String property_name, String image_url, String longitude, String latitude, String deployment_type,
                          Boolean trialStatus,BigInteger trialStartDate,BigInteger trialEndDate,String region,String awsRegion,Integer isMultiTenant,String vdms_id);

    @Query(value = "SELECT v.id FROM vdms v LEFT JOIN customer_organisation co ON co.id = v.customer_org_id LEFT JOIN user u ON u.customer_org_id = co.id WHERE u.email = ?1", nativeQuery = true)
    Set<String> getVdmsIdsByEmail(String username);

    @Query(value = "SELECT v.id FROM vdms v WHERE v.customer_org_id = ?1", nativeQuery = true)
    Set<String> getVdmsIdsByCustomerOrganisationId(String username);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM vdms WHERE id = ?1", nativeQuery = true)
    void deleteVdmsByVdmsId(String vdms_id, HttpServletRequest httpServletRequest);

    @Query(nativeQuery = true)
    Set<QuickSearchDTO> getQuickSearchListByAdmin();

    @Query(nativeQuery = true)
    Set<QuickSearchDTO> getQuickSearchListByMasterUserOrganisationId(String organisation_id);

    @Query(nativeQuery = true)
    Set<QuickSearchDTO> getQuickSearchListByUserOrganisationIdAndEmail(String organisation_id, String email);

    @Query(nativeQuery = true)
    Set<QuickSearchDTO> getQuickSearchListByMasterVendorOrganisationId(String organisation_id);

    @Query(nativeQuery = true)
    Set<QuickSearchDTO> getQuickSearchListByVendorOrganisationIdAndEmail(String organisation_id, String email);

    @Query(nativeQuery = true)
    Set<QuickSearchDTO> getQuickSearchListByPropertyAdminOrganisationIdAndEmail(String organisation_id, String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET is_block = true , block_timestamp = ?2 WHERE id = ?1", nativeQuery = true)
    void blockVdmsByVdmsId(String vdms_id, BigInteger block_timestamp);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET is_block = false  WHERE id = ?1", nativeQuery = true)
    void unBlockVdmsByVdmsId(String vdms_id);

    @Query(value = "SELECT id FROM vdms WHERE id  = ?1", nativeQuery = true)
    String checkVdmsId(String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET activation_status = ?1, activation_timestamp = ?2  WHERE id = ?3", nativeQuery = true)
    void activateVdms(String activation_status, BigInteger activationTime, String vdms_id);

    @Query(value = "SELECT id FROM vdms", nativeQuery = true)
    List<String> getVdmsIds();

    @Query(value = "SELECT activation_status FROM vdms WHERE id = ?1 ", nativeQuery = true)
    String getVdmsActivationStatusByVdmsId(String vdms_id);

    @Query(value = "SELECT property_name FROM vdms WHERE id = ?1", nativeQuery = true)
    String getPropertyNameByVdmsId(String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET end_date = ?1 ,status = ?2 ,plan = ?3 WHERE id = ?4", nativeQuery = true)
    void updateSubscriptionByVdmsId(String end_date, String status, String plan, String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET last_seen = ?1 WHERE id = ?2", nativeQuery = true)
    void updateVdmsLastSeenTimeStampByVdmsId(BigInteger last_seen, String vdms_id);

    @Query(nativeQuery = true)
    VdmsSyncDTO getVdmsSyncByVdmsId(String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET image_sync = IFNULL(?1 ,image_sync) ,user_sync = IFNULL(?2 ,user_sync) "
            + "WHERE id = ?3", nativeQuery = true)
    void updateVdmsSyncByVdmsId(Integer image_sync, Integer user_sync, String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET last_updated = ?1 WHERE id = ?2", nativeQuery = true)
    void changeLastUpdatedTimeByVdmsId(BigInteger time, String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET primary_proxy_profile_id = ?1 WHERE id = ?2", nativeQuery = true)
    void tagPrimaryProxyProfileToVdmsByProxyProfileId(String proxy_profile_id, String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET primary_proxy_profile_id = NULL WHERE id = ?1", nativeQuery = true)
    void untagPrimaryProxyProfileToVdmsByVdmsId(String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET primary_proxy_profile_id = NULL WHERE primary_proxy_profile_id = ?1", nativeQuery = true)
    void untagAllProxyProfilesFromVdmsByProxyProfileId(String id);

    @Query(nativeQuery = true)
    Set<VdmsDTO> getProxyProfilesTaggedToVdmsIdByOrganisationId(String customer_org_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET proxy_server_host_sync = ?1 WHERE id = ?2", nativeQuery = true)
    void updateProxyServerHostSyncByVdmsId(Integer value, String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET proxy_client_sync = ?1 WHERE id = ?2", nativeQuery = true)
    void updateProxyClientSyncByVdmsId(Integer value, String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET proxy_client_sync = 1 WHERE primary_proxy_profile_id = ?1 ", nativeQuery = true)
    void updateProxyClientSyncByProxyProfileId(String id);

    @Query(value = "SELECT primary_proxy_profile_id FROM vdms WHERE id = ?1", nativeQuery = true)
    String getPrimaryProxyProfileByVdmsId(String vdms_id);

    @Query(value = "SELECT DISTINCT v.id AS vdms_id FROM vdms v " +
            "LEFT JOIN address a ON v.address_id = a.id " +
            "LEFT JOIN docker d ON d.vdms_id = v.id " +
            "WHERE (( d.vendor_org_id = ?1) " +
            "OR ((d.invitee_org_id = ?1) " +
            "AND (d.invite_status IS NULL OR d.invite_status != 'rejected')))", nativeQuery = true)
    Set<String> getVisibleVdmsIdsByVendorOrganisationId(String organisation_id);

    @Query(value = "SELECT id FROM vdms WHERE primary_proxy_profile_id = ?1", nativeQuery = true)
    Set<String> getVdmsIdsByProxyProfileId(String id);

    @Query(value = "SELECT image_url FROM vdms WHERE id = ?1", nativeQuery = true)
    String getImageUrlByVdmsId(String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET property_name = IFNULL(?1 ,property_name) ,devuid = IFNULL(?2 ,devuid) WHERE id = ?3", nativeQuery = true)
    void updateVdmsDetailsByVdmsId(String property_name, String devuid, String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET latitude = ?1 , longitude = ?2 , vdms_coordinates_id=?3 WHERE id = ?4", nativeQuery = true)
    void updateVdmsLocationByVdmsId(String latitude, String longitude,String coordinatesId,String vdms_id);

    @Query(value = "SELECT u.email FROM user u LEFT JOIN vdms v ON u.customer_org_id = v.customer_org_id WHERE u.email = ?1 AND v.id = ?2", nativeQuery = true)
    String checkVdmsIdByEmail(String email, String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET customer_org_id = ?1, vdms_transfer = ?2  WHERE id = ?3", nativeQuery = true)
    void transferVdmsToRegisteredUserByVdmsId(String customer_org_id, Integer vdms_transfer, String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET vdms_transfer = 0  WHERE id = ?1", nativeQuery = true)
    void updateVdmsTranfer(String vdmsId);

    @Query(value = "SELECT customer_org_id FROM vdms WHERE id = ?1", nativeQuery = true)
    String getCustomerOrgIdByVdmsId(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET is_master = ?1 ,secondary_device_id = ?2 WHERE id = ?3", nativeQuery = true)
    void updateVdmsMasterStatusByVdmsId(Integer isMaster, String secondaryDeviceId, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET is_master = ?1 ,secondary_device_id = ?2 WHERE id = ?3", nativeQuery = true)
    void updateVdmsSlaveStatusByVdmsId(int isMaster, String secondaryDeviceId, String vdmsId);

    @Query(value = "SELECT last_seen FROM vdms WHERE id = ?1", nativeQuery = true)
    BigInteger getVdmsLastSeenByVdmsId(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET first_seen = ?1 WHERE id = ?2", nativeQuery = true)
    void updateVdmsFirstSeenByVdmsId(BigInteger firstSeen, String vdmsId);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS result FROM vdms WHERE activation_status = 'activated' And customer_org_id = ?1", nativeQuery = true)
    Integer getVdmsActivationStatusByOrgId(String orgId);

    @Query(nativeQuery = true)
    List<VdmsDTO> getStatus();


    @Query(nativeQuery = true)
    List<VdmsDTO> getVdmsByKey(String key);

    @Query(nativeQuery = true)
    List<QuickSearchDTO> getVdmsPropertyInfoByVdmsId(List<String> vdmsId);

    @Query(value = "SELECT id FROM vdms WHERE customer_org_id = ?1 AND id IN (?2)", nativeQuery = true)
    List<String> getVdmsIdsByOrgIdAndVdmsIds(String orgId, List<String> vdmsIds);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO vdms(id, property_name, address_id, devuid, customer_org_id, activation_status,image_url) " +
            "VALUES(?1,?2,?3,?4,?5,?6,?7)", nativeQuery = true)
    void addVdms(String vdmsId, String propertyName, String addressId, String devUid, String orgId, String activated,String image_url);

    @Query(nativeQuery = true)
    List<ExternalClientUserDTO> getExternalVdmsPropertyInfoByOrgId(String orgId);

    @Query(value = "SELECT customer_org_id FROM `vdms` where id = ?1", nativeQuery = true)
    String getOrgIdByVdmsId(String orgId);

    @Query(value="SELECT vdms_coordinates_id from vdms where id = ?1",nativeQuery = true)
    String getCoordinatesVdmsId(String vdmsId);


    @Query(nativeQuery = true)
    List<VdmsDTO> getVdmsAlertData();

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET email_alert = ?2 WHERE id = ?1", nativeQuery = true)
    void updaateVdmsEmailAlertByVdmsId(String vdmsId,int alertStatus);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET qr_sync = ?1 WHERE id = ?2", nativeQuery = true)
    void updateQrCodeSyncByVdmId(Integer qrSync, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET nfc_sync = ?1 WHERE id = ?2", nativeQuery = true)
    void updateNfcSyncByVdmId(Integer nfcSync, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET nfc_sync = ?1 WHERE id IN ?2", nativeQuery = true)
    void updateNfcSyncByVdmIds(Integer nfcSync, List<String> vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET qr_sync = ?1 WHERE id IN ?2", nativeQuery = true)
    void updateQrCodeSyncByVdmIds(Integer qrSync, List<String> vdmsId);

    @Query(nativeQuery = true)
    List<ExternalClientUserDTO> getVdmsIdsByEmailList(List<String> masterUserAndOrgAdminEmailsList);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET bar_code_sync = ?1 WHERE id = ?2", nativeQuery = true)
    void updateBarCodeSyncByVdmsId(Integer barCodeSync, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET bar_code_sync = ?1 WHERE id IN ?2", nativeQuery = true)
    void updateBarCodeSyncByVdmsIds(Integer barCodeSync, List<String> vdmsId);


    @Transactional
    @Modifying
    @Query(value = "UPDATE vdms SET asset_count = ?2 WHERE id = ?1",nativeQuery = true)
    void updateVdmsAssetCountByVdmsId(String vdmsId, Integer assetCount);

    @Query(value = "SELECT id FROM vdms WHERE customer_org_id = ?1",nativeQuery = true)
    List<String> getVdmsIdListByOrgId(String orgId);

    @Query(nativeQuery = true)
    List<PropertySummaryDTO> getBillingInfoPropertySummaryByOrgId(
            @Param("orgId") String orgId,
            @Param("selectedVdmsList") List<String> selectedVdmsList,
            @Param("pageSize") int pageSize,
            @Param("offset") int offset
    );

    @Query(nativeQuery = true)
    TierCountSummaryDTO getPropertyTierSummaryByOrgId(@Param("orgId") String orgId, @Param("selectedVdmsList") List<String> selectedVdmsList, @Param("billingId") String billingId);

    @Query(value = "SELECT COALESCE(SUM(v.asset_count), 0) FROM vdms v WHERE v.customer_org_id = ?1 AND v.id IN ?2", nativeQuery = true)
    Integer getTotalOnboardedAssets(String orgId,List<String> selectedVdmsList);

    @Query(value = "SELECT bar_code_sync FROM vdms WHERE id = ?1", nativeQuery = true)
    int getBarCodeSyncByVdmsId(String vdmsId);

    @Query(value = "SELECT nfc_sync FROM vdms WHERE id = ?1", nativeQuery = true)
    int getNfcSyncStateByVdmsId(String vdmsId);

    @Query(value = "SELECT qr_sync FROM vdms WHERE id = ?1", nativeQuery = true)
    Integer getQrSyncByVdmsId(String vdmsId);

    @Query(nativeQuery = true)
    List<ExternalClientUserDTO> getExternalClientVdmsPropertyInfoByOrganisationId(String orgId, int pageSize, int offset);

    @Query(value = "SELECT v.id FROM vdms v RIGHT JOIN vdms_visibility vv ON v.id = vv.vdms_id WHERE v.customer_org_id = ?1 AND vv.email = ?2",nativeQuery = true)
    List<String> getAllVdmsIdByUserOrganisationIdAndUserEmail(String organisationId, String email);

    @Query(nativeQuery = true)
    List<ExternalClientUserDTO> getScleraAgentVdmsInfo(List<String> vdmsIds,String key, int pageSize, int offset);

    @Query(nativeQuery = true)
    List<ExternalClientUserDTO> getScleraAgentVdmsInfoByMasterUser(String orgId, String key, int pageSize, int offset);

    @Query(nativeQuery = true)
    Set<VdmsDTO> getVdmsListByOrganisationId(String orgId);

    @Query(value = "SELECT id FROM vdms WHERE corrigo_config_id IN ?1", nativeQuery = true)
    List<String> getCorrigoTaggedVdmsIds(List<String> configIds);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET corrigo_sync = ?1 WHERE id IN ?2", nativeQuery = true)
    void updateCorrigoSyncByVdmsIds(Integer corrigoSync, List<String> vdmsIds);

    @Query(value = "SELECT id from vdms WHERE corrigo_config_id = ?1", nativeQuery = true)
    List<String> getCorrigoTaggedVdmsIdByConfigId(String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET corrigo_config_id = ?1 WHERE id = ?2", nativeQuery = true)
    void updateCorrigoConfigIdByVdmsId(String configId, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET corrigo_sync = ?1 WHERE id = ?2", nativeQuery = true)
    void updateCorrigoSyncByVdmsId(Integer corrigoSync, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET corrigo_config_id = ?1, corrigo_sync = ?2 WHERE id = ?3", nativeQuery = true)
    void updateCorrigoConfigIdAndSync (String configId, int sync,String vdmsId);

    @Query(value = """
            SELECT
                co.id AS organisation_id
            FROM qr_code qc
            JOIN vdms v
                ON qc.vdms_id = v.id
            JOIN customer_organisation co
                ON v.customer_org_id = co.id
            WHERE qc.id = ?1
            """, nativeQuery = true)
    String getOrgIdByQrCodeId(String qrCodeId);

    @Query(value = """
            SELECT
                co.id AS organisation_id
            FROM client_bar_code bc
            JOIN vdms v
                ON bc.vdms_id = v.id
            JOIN customer_organisation co
                ON v.customer_org_id = co.id
            WHERE bc.client_bar_code_id = ?1
            """, nativeQuery = true)
    String getOrgIdByBarCodeId(String barCodeId);

    @Query(value = """
            SELECT
                co.id AS organisation_id
            FROM client_qr_code cqc
            JOIN vdms v
                ON cqc.vdms_id = v.id
            JOIN customer_organisation co
                ON v.customer_org_id = co.id
            WHERE cqc.client_qr_code_id = ?1
            """, nativeQuery = true)
    String getOrgIdByClientQrCodeId(String qrCodeId);

    @Query(value = """
            SELECT
                co.id AS organisation_id
            FROM client_nfc cn
            JOIN vdms v
                ON cn.vdms_id = v.id
            JOIN customer_organisation co
                ON v.customer_org_id = co.id
            WHERE cn.nfc_id = ?1
            """, nativeQuery = true)
    String getOrgIdByClientNfcId(String nfcId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET activation_status = ?1, activation_timestamp = ?2, is_multi_tenant = 1 WHERE id = ?3", nativeQuery = true)
    void activateMultiTenantVdms(String activation_status, BigInteger activationTime, String vdms_id);

    @Query(value = "SELECT is_multi_tenant FROM vdms WHERE id = ?1",nativeQuery = true)
    int getMultiTenantCheck(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET is_multi_tenant = 1 AND deployment_type = cloud WHERE id = ?1", nativeQuery = true)
    void updateVdmsDeploymentType(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms SET aws_region = ?1 WHERE id = ?2", nativeQuery = true)
    void updateAwsRegionById(String region, String vdms_id);

    @Query(value = "SELECT aws_region FROM vdms WHERE id = ?1",nativeQuery = true)
    String getAwsRegionByVdmsId(String vdmsId);
}
