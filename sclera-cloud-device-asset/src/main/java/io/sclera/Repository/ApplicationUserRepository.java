package io.sclera.Repository;

import io.sclera.models.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, String> {

//    List<ApplicationUser> findByIdIn(Set<String> inventoryUserIds);

    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value = "INSERT INTO application_user (id, technician_id, email, type) " +
            "VALUES (?1, ?2, ?3, ?4) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "technician_id = EXCLUDED.technician_id, email = EXCLUDED.email, type = EXCLUDED.type", nativeQuery = true)
    Integer upsertApplicationUsers(String id, String technicianId, String email, String type);

    @Modifying
    @Transactional
    @Query(value = "UPDATE application_user " +
            "SET managed_software = null " +
            "WHERE id IN ?1", nativeQuery = true)
    Integer clearManagedSoftwareId(Set<String> inventoryUserIds);

    @Modifying
    @Transactional
    @Query(value = "UPDATE application_user " +
            "SET managed_software = ?2 " +
            "WHERE id IN ?1", nativeQuery = true)
    Integer updateManagedSoftwareIdByUserIds(Set<String> inventoryUserIds, String managedSoftwareId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE application_user " +
            "SET managed_software = ?2 " +
            "WHERE id = ?1", nativeQuery = true)
    Integer updateManagedSoftwareIdByUserId(String inventoryUserId, String managedSoftwareId);

    @Query(value = "SELECT email FROM application_user WHERE id IN ?1", nativeQuery = true)
    Set<String> findEmailsByUserIds(List<String> inventoryUserIds);

    @Query(value = "SELECT id FROM application_user WHERE id IN ?1", nativeQuery = true)
    Set<String> findExistingUserIds(Set<String> userIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM application_user WHERE id IN ?1", nativeQuery = true)
    Integer deleteApplicationUsersByIds(Set<String> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM application_user WHERE id = ?1", nativeQuery = true)
    Integer deleteApplicationUsersById(String ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM application_user WHERE managed_software = ?1", nativeQuery = true)
    Integer deleteByManagedSoftwareId(String managedSoftwareIds);

    @Modifying
    @Transactional
    @Query(value = "UPDATE application_user " +
            "SET managed_software = null " +
            "WHERE managed_software IN ?1", nativeQuery = true)
    Integer clearManagedSoftwareByManagedSoftwareIds(List<String> managedSoftwareIds);

    @Query(value = "SELECT id FROM application_user WHERE id IN ?1", nativeQuery = true)
    Set<String> findUserIdsByIds(Set<String> outUserIds);
}
