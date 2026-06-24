package io.sclera.repository;

import io.sclera.dto.ProxyProfileDTO;
import io.sclera.model.ProxyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Set;

@Repository
public interface ProxyProfileRepository extends JpaRepository<ProxyProfile ,String> {

    @Query(nativeQuery = true)
    Set<ProxyProfileDTO> getProxyProfileByCustomerOrganisationId(String customer_org_id);

    @Query(nativeQuery = true)
    Set<ProxyProfileDTO> getGlobalProxyProfiles();

    @Query(nativeQuery = true)
    ProxyProfileDTO getProxyProfileByProxyProfileId(String id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO proxy_profile(id,name,public_ip,host_machine,tcp_port,udp_port,ssl_enabled,customer_org_id) VALUE(?1,?2,?3,?4,?5,?6,?7,?8)" ,nativeQuery = true)
    void addProxyProfileByCustomerOrganisationId(String id, String name, String public_ip, String host_machine,String tcp_port, String udp_port, Integer ssl_enabled ,String customer_org_id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO proxy_profile(id,name,public_ip,tcp_port,udp_port,is_global,ssl_enabled) VALUE(?1,?2,?3,?4,?5,?6,?7)" ,nativeQuery = true)
    void addGlobalProxyProfileByAdminEmail(String id, String name, String public_ip, String tcp_port, String udp_port, int i ,Integer ssl_enabled);

    @Modifying
    @Transactional
    @Query(value = "UPDATE proxy_profile SET name = ?1 ,public_ip = ?2 ,host_machine = ?3 ,tcp_port = ?4 ,udp_port = ?5 ,ssl_enabled = ?6 WHERE id = ?7" ,nativeQuery = true)
    void updateProxyProfileByProxyProfileId(String name, String public_ip, String host_machine, String tcp_port, String udp_port, Integer ssl_enabled ,String id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxy_profile WHERE id = ?1" ,nativeQuery = true)
    void deleteProxyProfileByProxyProfileId(String id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxy_profile WHERE customer_org_id = ?1" ,nativeQuery = true)
    void deleteProxyProfileByCustomerOrganisationId(String customer_org_id);

    @Query(value = "SELECT host_machine FROM proxy_profile WHERE id = ?1" ,nativeQuery = true)
    String getHostMachineByProfileByProxyProfileId(String id);

    @Query(value = "SELECT COUNT(id) FROM proxy_profile WHERE host_machine = ?1" ,nativeQuery = true)
    Integer checkProxyProfileByHostMachine(String host_machine);

    @Query(nativeQuery = true)
    ProxyProfileDTO getServerProxyProfileByVdmsId(String vdms_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE proxy_profile SET name = ?1 , public_ip = ?2 ,tcp_port = ?3 ,udp_port = ?4 ,ssl_enabled = ?5 WHERE id = ?6" ,nativeQuery = true)
    void updateGlobalProxyProfileByProxyProfileId(String name, String public_ip,String tcp_port, String udp_port, Integer ssl_enabled ,String proxy_profile_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxy_profile WHERE id = ?1 AND is_global = 1" ,nativeQuery = true)
    void deleteGlobalProxyProfileByProxyProfileId(String id);

    @Query(nativeQuery = true)
    ProxyProfileDTO getVdmsProxyProfileByVdmsId(String vdms_id);
}
