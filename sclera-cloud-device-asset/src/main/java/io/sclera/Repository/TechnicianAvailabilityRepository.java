package io.sclera.Repository;

import io.sclera.dto.TechnicianAvailabilityDTO;
import io.sclera.models.TechnicianAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link TechnicianAvailability} records.
 */
public interface TechnicianAvailabilityRepository extends JpaRepository<TechnicianAvailability, String> {

    /**
     * Inserts a new technician availability row with the given values.
     *
     * @param id           availability identifier
     * @param startDate    availability start date as an epoch value
     * @param endDate      availability end date as an epoch value
     * @param startTime    availability start time
     * @param endTime      availability end time
     * @param isAllDay     whether the availability spans the whole day
     * @param frequency    recurrence frequency
     * @param condition    availability condition
     * @param technicianId owning technician identifier
     * @return the number of rows inserted
     */
    @Modifying
    @Transactional
    // PG-port: backtick removed (condition is not reserved in PG)
    @Query(value = "INSERT INTO technician_availability (id, start_date, end_date, start_time, end_time, is_all_day, frequency, condition, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9)", nativeQuery = true)
    Integer createTechnicianAvailability(String id, Long startDate, Long endDate, String startTime, String endTime, Boolean isAllDay, String frequency, String condition, String technicianId);

    /**
     * Updates the availability row identified by the given id with new values.
     *
     * @param id           availability identifier
     * @param startDate    availability start date as an epoch value
     * @param endDate      availability end date as an epoch value
     * @param startTime    availability start time
     * @param endTime      availability end time
     * @param isAllDay     whether the availability spans the whole day
     * @param frequency    recurrence frequency
     * @param condition    availability condition
     * @param technicianId owning technician identifier
     * @return the number of rows updated
     */
    @Modifying
    @Transactional
    // PG-port: backtick removed (condition is not reserved in PG)
    @Query(value = "UPDATE technician_availability SET start_date = ?2, end_date = ?3, start_time = ?4, end_time = ?5, is_all_day = ?6, frequency = ?7, condition = ?8, technician_id = ?9 " +
            "WHERE id = ?1", nativeQuery = true)
    Integer updateTechnicianAvailability(String id, Long startDate, Long endDate, String startTime, String endTime, Boolean isAllDay, String frequency, String condition, String technicianId);


    /**
     * Returns the subset of the supplied ids that already exist.
     *
     * @param ids candidate availability identifiers
     * @return the identifiers found in the table
     */
    @Query(value = "SELECT id FROM technician_availability WHERE id IN ?1", nativeQuery = true)
    Set<String> findExistingTechnicianAvailabilityByIds(List<String> ids);

    /**
     * Deletes the availability rows matching the given ids.
     *
     * @param ids availability identifiers to delete
     * @return the number of rows deleted
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_availability WHERE id IN ?1", nativeQuery = true)
    int deleteTechnicianAvailabilityByIds(Set<String> ids);

    /**
     * Deletes all availability rows belonging to the given technicians.
     *
     * @param technicianIds technician identifiers whose availability is removed
     * @return the number of rows deleted
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_availability WHERE technician_id IN ?1", nativeQuery = true)
    int deleteTechnicianAvailabilityByTechnicianIds(Set<String> technicianIds);

    /**
     * Returns all technician availability records.
     *
     * @return the list of availability projections
     */
    @Query(nativeQuery = true)
    List<TechnicianAvailabilityDTO> getAllTechnicianAvailability();

    /**
     * Returns the availability record with the given id.
     *
     * @param id availability identifier
     * @return the matching availability projection
     */
    @Query(nativeQuery = true)
    TechnicianAvailabilityDTO getTechnicianAvailabilityById(String id);

    /**
     * Deletes the availability row with the given id.
     *
     * @param id availability identifier
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_availability WHERE id = ?1", nativeQuery = true)
    void deleteTechnicianAvailabilityById(String id);

    /**
     * Returns a technician's availability records that fall within the given time range.
     *
     * @param technicianId owning technician identifier
     * @param startTime    range start time
     * @param endTime      range end time
     * @return the matching availability projections
     */
    @Query(name = "TechnicianAvailability.getTechnicianAvailabilityInRange", nativeQuery = true)
    List<TechnicianAvailabilityDTO> getTechnicianAvailabilityInRange(String technicianId, String startTime, String endTime);

    /**
     * Inserts the availability row or updates it on id conflict.
     *
     * @param id           availability identifier
     * @param startDate    availability start date as an epoch value
     * @param endDate      availability end date as an epoch value
     * @param startTime    availability start time
     * @param endTime      availability end time
     * @param isAllDay     whether the availability spans the whole day
     * @param frequency    recurrence frequency
     * @param condition    availability condition
     * @param technicianId owning technician identifier
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED); backticks removed (condition is not reserved in PG)
    @Query(value = "INSERT INTO technician_availability (id, start_date, end_date, start_time, end_time, is_all_day, frequency, condition, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "start_date = EXCLUDED.start_date, end_date = EXCLUDED.end_date, start_time = EXCLUDED.start_time, end_time = EXCLUDED.end_time, is_all_day = EXCLUDED.is_all_day, frequency = EXCLUDED.frequency, condition = EXCLUDED.condition", nativeQuery = true)
    Integer upsertTechnicianAvailability(String id, Long startDate, Long endDate, String startTime, String endTime, Boolean isAllDay, String frequency, String condition, String technicianId);
}