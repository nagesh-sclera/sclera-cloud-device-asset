package io.sclera.Repository;

import io.sclera.dto.TechnicianDTO;
import io.sclera.models.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician,String> {

    @Query(nativeQuery = true)
    TechnicianDTO getTechnicianById(String id);

    @Query(nativeQuery = true)
    List<TechnicianDTO> getAllTechnician();

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician (id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, vdms_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11)", nativeQuery = true)
    Integer createTechnician(String id, String email, String phone, String countryCode, String name, String department, String designation, String timeZone, String createdBy, Long createdAt, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE technician SET " +
            "email = ?2, phone = ?3, country_code = ?4, name = ?5, department = ?6, designation = ?7, " +
            "time_zone = ?8, created_by = ?9, created_at = ?10, vdms_id = ?11 WHERE id = ?1", nativeQuery = true)
    Integer updateTechnician(String id, String email, String phone, String countryCode, String name, String department, String designation, String timeZone, String createdBy, Long createdAt, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE technician " +
            "SET country_code = ?1, name = ?2, department = ?3, designation = ?4, time_zone = ?5, created_by = ?6, created_at = ?7, vdms_id = ?8 " +
            "WHERE email = ?9 AND phone = ?10", nativeQuery = true)
    Integer updateTechnicianByEmailAndPhone(String countryCode, String name, String department, String designation, String timeZone, String createdBy, Long aLong, String vdmsId, String email, String phone);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician WHERE id = ?1", nativeQuery = true)
    void deleteTechnicianById(String id);

    @Query(value = "SELECT email FROM technician WHERE ", nativeQuery = true)
    List<Set> getAllTechniciansEmail();

    // Upsert method to insert or update technician details
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician (id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, cost, unit, type, vdms_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14) " +
            "ON DUPLICATE KEY UPDATE " +
            "email = ?2, phone = ?3, country_code = ?4, name = ?5, department = ?6, designation = ?7, time_zone = ?8, cost = ?11, unit = ?12, type = ?13", nativeQuery = true)
    Integer upsertTechnician(String id, String email, String phone, String countryCode, String name, String department, String designation, String timeZone, String createdBy, Long createdAt, Integer cost, String unit, String type, String vdmsId);


    @Query(name = "Technician.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById", nativeQuery = true)
    TechnicianDTO getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(String id, String formattedDateTime);

    @Query(name = "Technician.getAllTechnicianSkillProfilesWithPrimarySkillAndAvailability", nativeQuery = true)
    List<TechnicianDTO> getAllTechnicianSkillProfilesWithPrimarySkillAndAvailability(String formattedDateTime, int size, int offset);

    @Query(name = "Technician.getTechnicianWithCountryCodePhoneAndAvailabilityById", nativeQuery = true)
    TechnicianDTO getTechnicianWithCountryCodePhoneAndAvailabilityById(String id, String formattedDateTime);

    @Query(name = "Technician.getAllTechniciansByFilterByPagination", nativeQuery = true)
    List<TechnicianDTO> getAllTechniciansByFilterByPagination(String formattedDateTime, int size, int offset, String technicianIdFilter, String departmentFilter, String availabilityFilter);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_technician (technician_id, device_id) VALUES (?1, ?2)", nativeQuery = true)
    void tagTechniciansToDevice(String technicianId, String deviceId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM device_technician WHERE technician_id = ?1 AND device_id = ?2", nativeQuery = true)
    void unTagTechniciansFromDevice(String technicianId, String deviceId);

    @Query(value = "SELECT technician_id FROM device_technician WHERE device_id = ?1", nativeQuery = true)
    List<String> getAllTaggedTechnicianIds(String deviceId);

    @Query(value = "SELECT name FROM technician WHERE id = ?1", nativeQuery = true)
    String getTechnicianNameById(String technicianId);

    @Query(name = "Technician.getAllTechnicianNamesAndIds", nativeQuery = true)
    List<TechnicianDTO> getAllTechnicianNamesAndIds(int size, int offset, String searchKey);

    @Query(value = "SELECT DISTINCT department FROM technician WHERE department IS NOT NULL", nativeQuery = true)
    List<String> getUniqueTechnicianDepartments();

    @Query(value = "SELECT id FROM technician WHERE id IN ?1", nativeQuery = true)
    Set<String> findExistingTechniciansByIds(List<String> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician WHERE id IN ?1", nativeQuery = true)
    int deleteTechniciansByIds(Set<String> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM device_technician WHERE technician_id IN ?1", nativeQuery = true)
    int deleteDeviceTechniciansByTechnicianIds(Set<String> technicianIds);
}
