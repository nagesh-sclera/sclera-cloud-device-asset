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
    // NOT CONVERTED — stays native: plain INSERT (already PG-valid)
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
    // NOT CONVERTED — stays native: sets technician_id (relation FK column) from a scalar id param;
    // JPQL cannot SET a @ManyToOne FK column by scalar value
    @Modifying
    @Transactional
    @Query(value = "UPDATE technician_certificate SET name = ?2, type = ?3, url = ?4, technician_id = ?5 WHERE id = ?1", nativeQuery = true)
    Integer updateTechnicianCertificate(String id, String name, String type, String url, String technicianId);

    /**
     * Returns all technician certificate records.
     *
     * @return the list of certificate projections
     */
    @Query("SELECT new io.sclera.dto.TechnicianCertificateDTO(" +
           "tc.id, tc.name, tc.type, tc.url, tc.technician.id, CAST(NULL AS integer)) " +
           "FROM TechnicianCertificate tc")
    List<TechnicianCertificateDTO> getAllTechnicianCertificates();

    /**
     * Returns the certificate record with the given id.
     *
     * @param id certificate identifier
     * @return the matching certificate projection
     */
    @Query("SELECT new io.sclera.dto.TechnicianCertificateDTO(" +
           "tc.id, tc.name, tc.type, tc.url, tc.technician.id, CAST(NULL AS integer)) " +
           "FROM TechnicianCertificate tc WHERE tc.id = ?1")
    TechnicianCertificateDTO getTechnicianCertificateById(String id);

    /**
     * Deletes the certificate row with the given id.
     *
     * @param id certificate identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM TechnicianCertificate tc WHERE tc.id = ?1")
    void deleteTechnicianCertificateById(String id);

    /**
     * Returns all certificate records belonging to the given technician.
     *
     * @param technicianId owning technician identifier
     * @return the matching certificate projections
     */
    @Query("SELECT new io.sclera.dto.TechnicianCertificateDTO(" +
           "tc.id, tc.name, tc.type, tc.url, tc.technician.id, CAST(NULL AS integer)) " +
           "FROM TechnicianCertificate tc WHERE tc.technician.id = ?1")
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
    // NOT CONVERTED — stays native: already PG-valid INSERT … ON CONFLICT (id) DO UPDATE
    @Modifying
    @Transactional
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
    @Query("SELECT tc.id FROM TechnicianCertificate tc WHERE tc.id IN ?1")
    Set<String> findExistingTechnicianCertificatesByIds(List<String> ids);

    /**
     * Deletes the certificate rows matching the given ids.
     *
     * @param ids certificate identifiers to delete
     * @return the number of rows deleted
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM TechnicianCertificate tc WHERE tc.id IN ?1")
    int deleteTechnicianCertificatesByIds(Set<String> ids);

    /**
     * Deletes all certificate rows belonging to the given technicians.
     *
     * @param technicianIds technician identifiers whose certificates are removed
     * @return the number of rows deleted
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM TechnicianCertificate tc WHERE tc.technician.id IN ?1")
    int deleteTechnicianCertificatesByTechnicianIds(Set<String> technicianIds);
}
