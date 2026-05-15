package io.sclera.Repository;

import io.sclera.dto.TechnicianCertificateDTO;
import io.sclera.models.TechnicianCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Repository
public interface TechnicianCertificateRepository extends JpaRepository<TechnicianCertificate,String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician_certificate (id, name, type, url, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5)", nativeQuery = true)
    Integer createTechnicianCertificate(String id, String name, String type, String url, String technicianId);



    @Modifying
    @Transactional
    @Query(value = "UPDATE technician_certificate SET name = ?2, type = ?3, url = ?4, technician_id = ?5 WHERE id = ?1", nativeQuery = true)
    Integer updateTechnicianCertificate(String id, String name, String type, String url, String technicianId);

    @Query(name = "TechnicianCertificate.getAll", nativeQuery = true)
    List<TechnicianCertificateDTO> getAllTechnicianCertificates();

    @Query(name = "TechnicianCertificate.getById", nativeQuery = true)
    TechnicianCertificateDTO getTechnicianCertificateById(String id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_certificate WHERE id = ?1", nativeQuery = true)
    void deleteTechnicianCertificateById(String id);

    @Query(name = "TechnicianCertificate.getByTechnicianId", nativeQuery = true)
    List<TechnicianCertificateDTO> getCertificatesByTechnicianId(String technicianId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician_certificate (id, name, type, url, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5) " +
            "ON DUPLICATE KEY UPDATE " +
            "name = ?2, type = ?3, url = ?4"
            , nativeQuery = true)
    Integer upsertTechnicianCertificate(String id, String name, String type, String url, String technicianId);

    @Query(value = "SELECT id FROM technician_certificate WHERE id IN ?1", nativeQuery = true)
    Set<String> findExistingTechnicianCertificatesByIds(List<String> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_certificate WHERE id IN ?1", nativeQuery = true)
    int deleteTechnicianCertificatesByIds(Set<String> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_certificate WHERE technician_id IN ?1", nativeQuery = true)
    int deleteTechnicianCertificatesByTechnicianIds(Set<String> technicianIds);
}
