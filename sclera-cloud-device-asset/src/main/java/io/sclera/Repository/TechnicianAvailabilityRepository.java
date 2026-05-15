package io.sclera.Repository;

import io.sclera.dto.TechnicianAvailabilityDTO;
import io.sclera.models.TechnicianAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

public interface TechnicianAvailabilityRepository extends JpaRepository<TechnicianAvailability, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician_availability (id, start_date, end_date, start_time, end_time, is_all_day, frequency, `condition`, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9)", nativeQuery = true)
    Integer createTechnicianAvailability(String id, Long startDate, Long endDate, String startTime, String endTime, Boolean isAllDay, String frequency, String condition, String technicianId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE technician_availability SET start_date = ?2, end_date = ?3, start_time = ?4, end_time = ?5, is_all_day = ?6, frequency = ?7, `condition` = ?8, technician_id = ?9 " +
            "WHERE id = ?1", nativeQuery = true)
    Integer updateTechnicianAvailability(String id, Long startDate, Long endDate, String startTime, String endTime, Boolean isAllDay, String frequency, String condition, String technicianId);


    @Query(value = "SELECT id FROM technician_availability WHERE id IN ?1", nativeQuery = true)
    Set<String> findExistingTechnicianAvailabilityByIds(List<String> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_availability WHERE id IN ?1", nativeQuery = true)
    int deleteTechnicianAvailabilityByIds(Set<String> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_availability WHERE technician_id IN ?1", nativeQuery = true)
    int deleteTechnicianAvailabilityByTechnicianIds(Set<String> technicianIds);

    @Query(nativeQuery = true)
    List<TechnicianAvailabilityDTO> getAllTechnicianAvailability();

    @Query(nativeQuery = true)
    TechnicianAvailabilityDTO getTechnicianAvailabilityById(String id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_availability WHERE id = ?1", nativeQuery = true)
    void deleteTechnicianAvailabilityById(String id);

    @Query(name = "TechnicianAvailability.getTechnicianAvailabilityInRange", nativeQuery = true)
    List<TechnicianAvailabilityDTO> getTechnicianAvailabilityInRange(String technicianId, String startTime, String endTime);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician_availability (id, start_date, end_date, start_time, end_time, is_all_day, frequency, `condition`, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9) " +
            "ON DUPLICATE KEY UPDATE " +
            "start_date = ?2, end_date = ?3, start_time = ?4, end_time = ?5, is_all_day = ?6, frequency = ?7, `condition` = ?8", nativeQuery = true)
    Integer upsertTechnicianAvailability(String id, Long startDate, Long endDate, String startTime, String endTime, Boolean isAllDay, String frequency, String condition, String technicianId);
}