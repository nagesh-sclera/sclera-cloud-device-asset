package io.sclera.repository;

import io.sclera.dto.QrCodeTemplateDTO;
import io.sclera.model.QrCodeTemplate;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface QrCodeTemplateRepository extends JpaRepository<QrCodeTemplate,String> {

    @Query(nativeQuery = true)
    List<QrCodeTemplateDTO> getAllQrCodeTemplateByOrgId(String key, String orgId, int pageSize, int offset);

    @Query(nativeQuery = true)
    List<QrCodeTemplateDTO> getAllQrCodeTemplate(String key, int pageSize, int offset);

    @Query(value = "SELECT qr_code_template_url FROM qr_code_template WHERE id IN ?1",nativeQuery = true)
    List<String> getAllQrCodeTemplateUrlByIds(List<String> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM qr_code_template WHERE id IN ?1",nativeQuery = true)
    void removeQrCodeTemplateByIds(List<String> ids);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO qr_code_template (id, name, qr_code_template_url,qr_code_logo_url, customer_org_id, qr_template_json, creation_timestamp, added_by) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)",nativeQuery = true)
    void addQrCodeTemplate(String id, String name, String qrCodeTemplateUrl,String logoUrl, String customerOrgId, String body, BigInteger creationTimestamp, String loggedInUser);

    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code_template SET name = ?1, qr_code_template_url = ?2, qr_code_logo_url = ?3, customer_org_id = ?4, qr_template_json = ?5, updated_timestamp = ?6, updated_by = ?7 " +
            "WHERE id = ?8",nativeQuery = true)
    void updateQrCodeTemplateById(String name, String qrCodeTemplateUrl,String logoUrl, String orgId, String body, BigInteger updatedTimeStamp, String loggedInUser, String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code_template SET in_use = ?1 WHERE qr_code_template_url = ?2",nativeQuery = true)
    void updateInUseByUrl(Integer inUse,String templateUrl);

    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code_template SET in_use = ?1 WHERE customer_org_id = ?2",nativeQuery = true)
    void updateInUseByOrgId(Integer inUse, String orgId);

    @Query(nativeQuery = true)
    QrCodeTemplateDTO getInUseUrlByOrgId(int inuUse,String orgId);

    @Query(value = "SELECT qr_code_logo_url FROM qr_code_template WHERE id = ?1",nativeQuery = true)
    String getQrCodeLogoUrlById(String id);

    @Query(nativeQuery = true)
    QrCodeTemplateDTO getDefaultTemplate();

    @Query(value = "SELECT COUNT(in_use) FROM qr_code_template WHERE customer_org_id = ?1 AND in_use = 1",nativeQuery = true)
    int getTemplateInUseCount(String orgId);

    @Query(nativeQuery = true)
    List<QrCodeTemplateDTO> getDataByIds(List<String> ids);
}
