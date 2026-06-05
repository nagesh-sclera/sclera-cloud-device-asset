package io.sclera.Repository;

import io.sclera.dto.TechnicianCertificateDTO;
import io.sclera.models.TechnicianCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link TechnicianCertificate} records.
 */
@Repository
public interface TechnicianCertificateRepository extends JpaRepository<TechnicianCertificate,String> {

    /**
     * Inserts a new technician certificate row with the given values.
     *
     * @param id           certificate identifier
     * @param name         certificate name
     * @param type         certificate type
     * @param url          certificate document URL
     * @param technicianId owning technician identifier
     * @return the number of rows inserted
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician_certificate (id, name, type, url, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5)", nativeQuery = true)
    Integer createTechnicianCertificate(String id, String name, String type, String url, String technicianId);



    /**
     * Updates the certificate row identified by the given id with new values.
     *
     * @param id           certificate identifier
     * @param name         certificate name
     * @param type         certificate type
     * @param url          certificate document URL
     * @param technicianId owning technician identifier
     * @return the number of rows updated
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE technician_certificate SET name = ?2, type = ?3, url = ?4, technician_id = ?5 WHERE id = ?1", nativeQuery = true)
    Integer updateTechnicianCertificate(String id, String name, String type, String url, String technicianId);

    /**
     * Returns all technician certificate records.
     *
     * @return the list of certificate projections
     */
    @Query(name = "TechnicianCertificate.getAll", nativeQuery = true)
    List<TechnicianCertificateDTO> getAllTechnicianCertificates();

    /**
     * Returns the certificate record with the given id.
     *
     * @param id certificate identifier
     * @return the matching certificate projection
     */
    @Query(name = "TechnicianCertificate.getById", nativeQuery = true)
    TechnicianCertificateDTO getTechnicianCertificateById(String id);

    /**
     * Deletes the certificate row with the given id.
     *
     * @param id certificate identifier
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_certificate WHERE id = ?1", nativeQuery = true)
    void deleteTechnicianCertificateById(String id);

    /**
     * Returns all certificate records belonging to the given technician.
     *
     * @param technicianId owning technician identifier
     * @return the matching certificate projections
     */
    @Query(name = "TechnicianCertificate.getByTechnicianId", nativeQuery = true)
    List<TechnicianCertificateDTO> getCertificatesByTechnicianId(String technicianId);

    /**
     * Inserts the certificate row or updates it on id conflict.
     *
     * @param id           certificate identifier
     * @param name         certificate name
     * @param type         certificate type
     * @param url          certificate document URL
     * @param technicianId owning technician identifier
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value = "INSERT INTO technician_certificate (id, name, type, url, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "name = EXCLUDED.name, type = EXCLUDED.type, url = EXCLUDED.url"
            , nativeQuery = true)
    Integer upsertTechnicianCertificate(String id, String name, String type, String url, String technicianId);

    /**
     * Returns the subset of the supplied ids that already exist.
     *
     * @param ids candidate certificate identifiers
     * @return the identifiers found in the table
     */
    @Query(value = "SELECT id FROM technician_certificate WHERE id IN ?1", nativeQuery = true)
    Set<String> findExistingTechnicianCertificatesByIds(List<String> ids);

    /**
     * Deletes the certificate rows matching the given ids.
     *
     * @param ids certificate identifiers to delete
     * @return the number of rows deleted
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_certificate WHERE id IN ?1", nativeQuery = true)
    int deleteTechnicianCertificatesByIds(Set<String> ids);

    /**
     * Deletes all certificate rows belonging to the given technicians.
     *
     * @param technicianIds technician identifiers whose certificates are removed
     * @return the number of rows deleted
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_certificate WHERE technician_id IN ?1", nativeQuery = true)
    int deleteTechnicianCertificatesByTechnicianIds(Set<String> technicianIds);
}
