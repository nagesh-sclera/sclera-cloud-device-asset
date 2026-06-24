package io.sclera.repository;

import io.sclera.dto.DockerDTO;
import io.sclera.dto.DockerSyncDTO;
import io.sclera.model.Docker;
import io.sclera.model.compositeclass.DockerIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;

@Repository
public interface DockerRepository extends JpaRepository<Docker, DockerIds> {

    @Query(nativeQuery = true)
    DockerDTO getDockerInfoByVdmsIdAndDockerName(String vdms_id, String docker_name);

    @Query(nativeQuery = true)
    Set<DockerDTO> getAllDockersByVdmsId(String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO docker(name ,vdms_id ,mac_address ,public_ip_address ,system_type ,gateway) VALUES(?1,?2,?3,?4,?5,?6)", nativeQuery = true)
    void addDockerByVdmsId(String docker_name, String vdms_id, String mac_address, String public_ip_address,
                           String system_type, String gateway);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM docker WHERE vdms_id = ?1", nativeQuery = true)
    void deleteDockerByVdmsId(String vdms_id);

    @Query(value = "SELECT name FROM docker WHERE vdms_id = ?1", nativeQuery = true)
    Set<String> getDockerIdsByVdmsId(String vdms_id);

    @Query(value = "SELECT name FROM docker WHERE vdms_id = ?1", nativeQuery = true)
    Set<String> getDockerNamesByVdmsId(String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET block_timestamp = ?1 ,cidr = ?2 ,external_ip_address = ?3 ,gateway = ?4 ,"
            + "host = ?5 ,interface_in = ?6 ,interface_out = ?7 ,internal_ip_address = ?8 ,internet_required = ?9 ,"
            + "internet_status = ?10 ,internet_timestamp = ?11 ,is_static = ?12 ,is_tagged = ?13 ,is_block = ?14 ,"
            + "mac_address = ?15 ,macvlan_name = ?16 ,primary_dns = ?17 ,public_ip_address = ?18 ,secondary_dns = ?19 ,"
            + "system_type = ?20 ,vlan_id = ?21 ,approval_status = ?22 ,vendor_org_id = ?23 ,network_name = ?24 " +
            " WHERE name = ?25 AND vdms_id = ?26",
            nativeQuery = true)
    void updateDockerByVdmsIdAndDockerName(BigInteger block_timestamp, Integer cidr, String external_ip_address,
                                           String gateway, Boolean host, String interface_in, String interface_out, String internal_ip_address,
                                           Boolean internet_required, Boolean internet_status, BigInteger internet_timestamp, Boolean is_static,
                                           Boolean is_tagged, Boolean is_block, String mac_address, String macvlan_name, String primary_dns,
                                           String public_ip_address, String secondary_dns, String system_type, String vlan_id, String approval_status,
                                           String vendor_org_id, String network_name, String docker_name, String vdms_id);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO docker(name,vdms_id,network_name,block_timestamp,cidr,external_ip_address,gateway,host,interface_in,"
            + "interface_out,internal_ip_address,internet_required,internet_status,internet_timestamp,is_static,"
            + "is_tagged,is_block,mac_address,macvlan_name,primary_dns,public_ip_address ,secondary_dns,system_type,vlan_id,approval_status,"
            + "vendor_org_id,primary_proxy_profile_id) VALUE(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21,?22,?23,"
            + "?24,?25,?26,?27)", nativeQuery = true)
    void addDockerByVdmsIdAndDockerName(String docker_name, String vdms_id, String network_name, BigInteger block_timestamp, Integer cidr,
                                        String external_ip_address, String gateway, Boolean host, String interface_in, String interface_out,
                                        String internal_ip_address, Boolean internet_required, Boolean internet_status,
                                        BigInteger internet_timestamp, Boolean is_static, Boolean is_tagged, Boolean is_block, String mac_address,
                                        String macvlan_name, String primary_dns, String public_ip_address, String secondary_dns, String system_type,
                                        String vlan_id, String approval_status, String vendor_org_id, String primary_proxy_profile_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM docker WHERE vdms_id = ?1 AND name = ?2", nativeQuery = true)
    void deleteDockerByVdmsIdAndDockerName(String vdms_id, String docker_name);

    @Query(value = "SELECT COUNT(name) FROM docker WHERE vdms_id = ?1", nativeQuery = true)
    Integer getNetworkCountByVdmsId(String vdms_id);

    @Query(value = "SELECT COUNT(name) FROM docker WHERE vdms_id = ?1 AND vendor_org_id = ?2", nativeQuery = true)
    Integer getNetworkCountByVdmsIdAndVendorOrganisationId(String vdms_id, String vendor_org_id);

    @Query(nativeQuery = true)
    Set<DockerSyncDTO> getDockerSyncByVdmsId(String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET vendor_sync = 1 WHERE vendor_org_id = ?1", nativeQuery = true)
    void addVdmsSyncByVendorOrganisationId(String vendor_org_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET vendor_sync = 0 WHERE vendor_org_id = ?1 AND vdms_id = ?2 AND name = ?3", nativeQuery = true)
    void updateVdmsSyncByVendorOrgIdVdmsIdAndDockerName(String vendor_org_id, String vdms_id, String name);

    @Query(nativeQuery = true)
    Set<DockerDTO> getAllDockersByInviteeOrganisationIdAndVdmsId(String organisation_id, String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET vendor_org_id = ?1 ,invite_status = NULL, invitee_org_id = NULL ,vendor_transfer = 1 WHERE vdms_id = ?2 AND name = ?3", nativeQuery = true)
    void acceptCustomerRequestByVdmsIdAndDockerName(String vendor_org_id, String vdms_id, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET invite_status = NULL, invitee_org_id = NULL WHERE vdms_id = ?1 AND name = ?2", nativeQuery = true)
    void declineCustomerRequestByVdmsIdAndDockerName(String vdms_id, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET invitee_org_id = ?1 , invite_status = 'invited' WHERE vdms_id = ?2 AND name = ?3", nativeQuery = true)
    void transferDockerToRegisteredVendorByVdmsIdAndDockerName(String organisation_id, String vdms_id, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET invitee_org_id = NULL ,invite_status = NULL WHERE vdms_id = ?1 AND name = ?2", nativeQuery = true)
    void cancelVendorTransferByVdmsIdAndDockerName(String vdms_id, String name);

    @Query(value = "SELECT vendor_org_id FROM docker WHERE vdms_id = ?1 AND name = ?2", nativeQuery = true)
    String getVendorOrganisationIdByVdmsIdAndDockerName(String vdms_id, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET vendor_transfer = 0 WHERE vdms_id = ?1 AND name = ?2", nativeQuery = true)
    void updateVendorTransferByVdmsIdAndDockerName(String vdms_id, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET primary_proxy_profile_id = NULL WHERE vdms_id = ?1", nativeQuery = true)
    void removeProxyProfileByVdmsId(String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET primary_proxy_profile_id = ?1 WHERE vdms_id = ?2 AND name = ?3 ", nativeQuery = true)
    void modifyProxyProfileToDockerByVdmsIdAndNetworkName(String id, String vdms_id, String network);

    @Query(value = "SELECT name FROM docker WHERE vdms_id = ?1 AND primary_proxy_profile_id IS NOT NULL ", nativeQuery = true)
    Set<String> getProxyProfileTaggedDockersByVdmsId(String vdms_id);

    @Query(value = "SELECT name FROM docker WHERE vdms_id = ?1", nativeQuery = true)
    Set<String> getNetworkNamesByVdmsId(String vdms_id);

    @Query(value = "SELECT DISTINCT(primary_proxy_profile_id) FROM docker WHERE vdms_id = ?1 AND primary_proxy_profile_id IS NOT NULL", nativeQuery = true)
    String getDistinctProxyProfileByVdmsId(String vdms_id);

    @Query(value = "SELECT COUNT(name) FROM docker WHERE invitee_org_id = ?1 AND invite_status = 'invited' AND vdms_id = ?2 AND name = ?3 ", nativeQuery = true)
    Integer checkInviteStatusByVdmsIdAndDockerName(String vendorOrgId, String vdms_id, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET vendor_org_id = NULL ,invitee_org_id = NULL ,invite_status = NULL WHERE vdms_id = ?1 AND name = ?2", nativeQuery = true)
    void unTagVendorByMasterUserEmail(String vdms_id, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET vendor_org_id = ?1 WHERE vdms_id = ?2 AND name = ?3", nativeQuery = true)
    void tagMasterVendorToNetwork(String vendorOrgId, String vdmsId, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE docker SET vendor_org_id = NULL ,invitee_org_id = NULL WHERE vendor_org_id = ?1" ,nativeQuery = true)
    void untagVendorByOrganisationId(String orgId);

    @Query(value = "SELECT DISTINCT vdms_id FROM docker WHERE vdms_id = ?1 AND vendor_org_id = ?2 ", nativeQuery = true)
    String getVdmsAccessByOrganisationIdAndVdmsId(String vdmsId, String organisationId);

    @Query(value = "SELECT COUNT(name) FROM docker WHERE vdms_id = ?1 AND invitee_org_id = ?2", nativeQuery = true)
    Integer getNetworkCountByVdmsIdAndInviteeOrgId(String vdms_id, String vendor_org_id);

    @Query(value = "SELECT invitee_org_id FROM docker WHERE vdms_id = ?1 AND invitee_org_id = ?2", nativeQuery = true)
    String getInviteeOrgId(String vdmsId, String organisationId);

}
