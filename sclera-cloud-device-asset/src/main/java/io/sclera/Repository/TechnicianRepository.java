package io.sclera.Repository;

import io.sclera.dto.TechnicianDTO;
import io.sclera.models.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link Technician} records and their device tags.
 */
@Repository
public interface TechnicianRepository extends JpaRepository<Technician,String>, TechnicianRepositoryCustom {

    /**
     * Fetches the managed {@link Technician} entity for the given id (single-table read), or
     * {@code null} if none. Backs {@link #getTechnicianById(String)} below.
     *
     * @param id technician identifier
     * @return the matching technician entity, or {@code null}
     */
    @Query("SELECT t FROM Technician t WHERE t.id = :id")
    Technician findTechnicianEntityById(@Param("id") String id);

    /**
     * Returns the technician projection with the given id, or {@code null} if none — same contract
     * as the previous native {@code Technician.getTechnicianById} query. Fetches the entity via
     * {@link #findTechnicianEntityById(String)} and maps it to {@link TechnicianDTO} via
     * {@link io.sclera.mapper.TechnicianDtoMapper}.
     *
     * @param id technician identifier
     * @return the matching technician projection, or {@code null}
     */
    default TechnicianDTO getTechnicianById(String id) {
        Technician t = findTechnicianEntityById(id);
        if (t == null) {
            return null;
        }
        return io.sclera.mapper.TechnicianMapperHolder.MAPPER.toDto(t);
    }

    /**
     * Inserts a new technician row with the given values.
     *
     * @param id          technician identifier
     * @param email       technician email address
     * @param phone       technician phone number
     * @param countryCode phone country code
     * @param name        technician name
     * @param department  technician department
     * @param designation technician designation
     * @param timeZone    technician time zone
     * @param createdBy   identifier of the creating user
     * @param createdAt   creation timestamp as an epoch value
     * @param vdmsId      associated VDMS identifier
     * @return the number of rows inserted
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): JPA JPQL has no INSERT statement; plain INSERT already PG-compatible (no MySQL constructs)
    @Query(value = "INSERT INTO technician (id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, vdms_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11)", nativeQuery = true)
    Integer createTechnician(String id, String email, String phone, String countryCode, String name, String department, String designation, String timeZone, String createdBy, Long createdAt, String vdmsId);

    /**
     * Updates the technician row identified by the given id with new values.
     *
     * @param id          technician identifier
     * @param email       technician email address
     * @param phone       technician phone number
     * @param countryCode phone country code
     * @param name        technician name
     * @param department  technician department
     * @param designation technician designation
     * @param timeZone    technician time zone
     * @param createdBy   identifier of the creating user
     * @param createdAt   creation timestamp as an epoch value
     * @param vdmsId      associated VDMS identifier
     * @return the number of rows updated
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    // NOT CONVERTED (partial) — vdms_id is managed by @ManyToOne Vdms vdms; JPQL cannot SET an association FK via path navigation
    // in a bulk UPDATE. All other columns are portable JPQL; vdms update left native via the upsertTechnician method.
    @Query(value = "UPDATE technician SET " +
            "email = ?2, phone = ?3, country_code = ?4, name = ?5, department = ?6, designation = ?7, " +
            "time_zone = ?8, created_by = ?9, created_at = ?10, vdms_id = ?11 WHERE id = ?1", nativeQuery = true)
    Integer updateTechnician(String id, String email, String phone, String countryCode, String name, String department, String designation, String timeZone, String createdBy, Long createdAt, String vdmsId);

    /**
     * Updates the technician matched by email and phone with new values.
     *
     * @param countryCode phone country code
     * @param name        technician name
     * @param department  technician department
     * @param designation technician designation
     * @param timeZone    technician time zone
     * @param createdBy   identifier of the creating user
     * @param aLong       creation timestamp as an epoch value
     * @param vdmsId      associated VDMS identifier
     * @param email       email used to match the technician
     * @param phone       phone used to match the technician
     * @return the number of rows updated
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    // NOT CONVERTED (partial) — vdms_id is managed by @ManyToOne Vdms vdms; JPQL cannot SET an association FK via path navigation
    // in a bulk UPDATE. All other columns are portable JPQL; vdms update left native via the upsertTechnician method.
    @Query(value = "UPDATE technician SET " +
            "country_code = ?1, name = ?2, department = ?3, designation = ?4, time_zone = ?5, created_by = ?6, created_at = ?7, vdms_id = ?8 " +
            "WHERE email = ?9 AND phone = ?10", nativeQuery = true)
    Integer updateTechnicianByEmailAndPhone(String countryCode, String name, String department, String designation, String timeZone, String createdBy, Long aLong, String vdmsId, String email, String phone);

    /**
     * Deletes the technician row with the given id.
     *
     * @param id technician identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Technician t WHERE t.id = ?1")
    void deleteTechnicianById(String id);

    /**
     * Returns the email addresses of all technicians.
     *
     * @return the list of technician emails
     */
    // NOT CONVERTED — stays native (PG-translation track): query is syntactically broken (WHERE clause has no predicate — latent bug); keeping native preserves current runtime behaviour
    @Query(value = "SELECT email FROM technician WHERE ", nativeQuery = true)
    List<Set> getAllTechniciansEmail();

    /**
     * Inserts the technician row or updates it on id conflict.
     *
     * @param id          technician identifier
     * @param email       technician email address
     * @param phone       technician phone number
     * @param countryCode phone country code
     * @param name        technician name
     * @param department  technician department
     * @param designation technician designation
     * @param timeZone    technician time zone
     * @param createdBy   identifier of the creating user
     * @param createdAt   creation timestamp as an epoch value
     * @param cost        technician cost
     * @param unit        cost unit
     * @param type        technician type
     * @param vdmsId      associated VDMS identifier
     * @return the number of rows affected
     */
    // NOT CONVERTED — stays native (PG-translation track): ON CONFLICT upsert already PG-portable; no portable JPQL equivalent for upsert semantics
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician (id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, cost, unit, type, vdms_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "email = EXCLUDED.email, phone = EXCLUDED.phone, country_code = EXCLUDED.country_code, name = EXCLUDED.name, department = EXCLUDED.department, designation = EXCLUDED.designation, time_zone = EXCLUDED.time_zone, cost = EXCLUDED.cost, unit = EXCLUDED.unit, type = EXCLUDED.type", nativeQuery = true)
    Integer upsertTechnician(String id, String email, String phone, String countryCode, String name, String department, String designation, String timeZone, String createdBy, Long createdAt, Integer cost, String unit, String type, String vdmsId);


    /**
     * Returns a technician's skill profile with primary skill and availability resolved for the given time.
     *
     * @param id                technician identifier
     * @param formattedDateTime reference date-time used to evaluate availability
     * @return the matching technician projection
     */
    // NOT CONVERTED — stays native (PG-translation track): CONVERT_TZ/AT TIME ZONE/FROM_UNIXTIME/DATE_FORMAT/JSON_CONTAINS/jsonb operators; already PG-ported in @NamedNativeQuery
    @Query(name = "Technician.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById", nativeQuery = true)
    TechnicianDTO getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(String id, String formattedDateTime);

    /**
     * Returns a page of technician skill profiles with primary skill and availability resolved for the given time.
     *
     * @param formattedDateTime reference date-time used to evaluate availability
     * @param size              page size
     * @param offset            row offset for pagination
     * @return the matching technician projections
     */
    // NOT CONVERTED — stays native (PG-translation track): CONVERT_TZ/AT TIME ZONE/FROM_UNIXTIME/DATE_FORMAT/JSON_CONTAINS/jsonb operators; already PG-ported in @NamedNativeQuery
    @Query(name = "Technician.getAllTechnicianSkillProfilesWithPrimarySkillAndAvailability", nativeQuery = true)
    List<TechnicianDTO> getAllTechnicianSkillProfilesWithPrimarySkillAndAvailability(String formattedDateTime, int size, int offset);

    /**
     * Returns a technician with country code, phone, and availability resolved for the given time.
     *
     * @param id                technician identifier
     * @param formattedDateTime reference date-time used to evaluate availability
     * @return the matching technician projection
     */
    // NOT CONVERTED — stays native (PG-translation track): CONVERT_TZ/AT TIME ZONE/FROM_UNIXTIME/DATE_FORMAT/JSON_CONTAINS/jsonb operators; already PG-ported in @NamedNativeQuery
    @Query(name = "Technician.getTechnicianWithCountryCodePhoneAndAvailabilityById", nativeQuery = true)
    TechnicianDTO getTechnicianWithCountryCodePhoneAndAvailabilityById(String id, String formattedDateTime);

    /**
     * Returns a filtered, paginated page of technicians with availability resolved for the given time.
     *
     * @param formattedDateTime  reference date-time used to evaluate availability
     * @param size               page size
     * @param offset             row offset for pagination
     * @param technicianIdFilter technician id filter
     * @param departmentFilter   department filter
     * @param availabilityFilter availability filter
     * @return the matching technician projections
     */
    // NOT CONVERTED — stays native (PG-translation track): CONVERT_TZ/AT TIME ZONE/FROM_UNIXTIME/DATE_FORMAT/JSON_CONTAINS/jsonb operators + HAVING-on-alias; already PG-ported in @NamedNativeQuery
    @Query(name = "Technician.getAllTechniciansByFilterByPagination", nativeQuery = true)
    List<TechnicianDTO> getAllTechniciansByFilterByPagination(String formattedDateTime, int size, int offset, String technicianIdFilter, String departmentFilter, String availabilityFilter);

    /**
     * Tags a technician to a device by inserting a device-technician association.
     *
     * @param technicianId technician identifier
     * @param deviceId     device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): device_technician is not a JPA entity; JPA JPQL has no INSERT statement; plain INSERT already PG-compatible
    @Query(value = "INSERT INTO device_technician (technician_id, device_id) VALUES (?1, ?2)", nativeQuery = true)
    void tagTechniciansToDevice(String technicianId, String deviceId);

    /**
     * Removes the device-technician association for the given technician and device.
     *
     * @param technicianId technician identifier
     * @param deviceId     device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): device_technician is not a JPA entity; JPQL DELETE requires an entity; plain DELETE already PG-compatible
    @Query(value = "DELETE FROM device_technician WHERE technician_id = ?1 AND device_id = ?2", nativeQuery = true)
    void unTagTechniciansFromDevice(String technicianId, String deviceId);

    /**
     * Returns the identifiers of all technicians tagged to the given device.
     *
     * @param deviceId device identifier
     * @return the tagged technician identifiers
     */
    // NOT CONVERTED — stays native (PG-translation track): device_technician is not a JPA entity; plain SELECT already PG-compatible
    @Query(value = "SELECT technician_id FROM device_technician WHERE device_id = ?1", nativeQuery = true)
    List<String> getAllTaggedTechnicianIds(String deviceId);

    /**
     * Returns the name of the technician with the given id.
     *
     * @param technicianId technician identifier
     * @return the technician name
     */
    @Query("SELECT t.name FROM Technician t WHERE t.id = ?1")
    String getTechnicianNameById(String technicianId);

    /**
     * Returns a paginated page of technician names and ids matching the search key.
     *
     * @param size      page size
     * @param offset    row offset for pagination
     * @param searchKey search term applied to technician names
     * @return the matching technician projections
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with @SqlResultSetMapping and LIMIT/OFFSET; CONCAT_WS already PG-compatible
    @Query(name = "Technician.getAllTechnicianNamesAndIds", nativeQuery = true)
    List<TechnicianDTO> getAllTechnicianNamesAndIds(int size, int offset, String searchKey);

    /**
     * Returns the distinct non-null technician departments.
     *
     * @return the list of unique departments
     */
    @Query("SELECT DISTINCT t.department FROM Technician t WHERE t.department IS NOT NULL")
    List<String> getUniqueTechnicianDepartments();

    /**
     * Returns the subset of the supplied ids that already exist.
     *
     * @param ids candidate technician identifiers
     * @return the identifiers found in the table
     */
    @Query("SELECT t.id FROM Technician t WHERE t.id IN ?1")
    Set<String> findExistingTechniciansByIds(List<String> ids);

    /**
     * Deletes the technician rows matching the given ids.
     *
     * @param ids technician identifiers to delete
     * @return the number of rows deleted
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Technician t WHERE t.id IN ?1")
    int deleteTechniciansByIds(Set<String> ids);

    /**
     * Deletes all device-technician associations for the given technicians.
     *
     * @param technicianIds technician identifiers whose device tags are removed
     * @return the number of rows deleted
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): device_technician is not a JPA entity; JPQL DELETE requires an entity; plain DELETE already PG-compatible
    @Query(value = "DELETE FROM device_technician WHERE technician_id IN ?1", nativeQuery = true)
    int deleteDeviceTechniciansByTechnicianIds(Set<String> technicianIds);
}
