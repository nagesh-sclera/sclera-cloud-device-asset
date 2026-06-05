package io.sclera.Repository;

import io.sclera.models.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link ApplicationUser} entities.
 */
@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, String> {

//    List<ApplicationUser> findByIdIn(Set<String> inventoryUserIds);

    /**
     * Inserts an application user or updates the existing row on identifier conflict.
     *
     * @param id the application user identifier
     * @param technicianId the associated technician identifier
     * @param email the user's email address
     * @param type the user type
     * @return the number of affected rows
     */
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value = "INSERT INTO application_user (id, technician_id, email, type) " +
            "VALUES (?1, ?2, ?3, ?4) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "technician_id = EXCLUDED.technician_id, email = EXCLUDED.email, type = EXCLUDED.type", nativeQuery = true)
    Integer upsertApplicationUsers(String id, String technicianId, String email, String type);

    /**
     * Clears the managed software reference for the given application users.
     *
     * @param inventoryUserIds the application user identifiers
     * @return the number of updated rows
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE application_user " +
            "SET managed_software = null " +
            "WHERE id IN ?1", nativeQuery = true)
    Integer clearManagedSoftwareId(Set<String> inventoryUserIds);

    /**
     * Assigns the given managed software to the specified application users.
     *
     * @param inventoryUserIds the application user identifiers
     * @param managedSoftwareId the managed software identifier to assign
     * @return the number of updated rows
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE application_user " +
            "SET managed_software = ?2 " +
            "WHERE id IN ?1", nativeQuery = true)
    Integer updateManagedSoftwareIdByUserIds(Set<String> inventoryUserIds, String managedSoftwareId);

    /**
     * Assigns the given managed software to a single application user.
     *
     * @param inventoryUserId the application user identifier
     * @param managedSoftwareId the managed software identifier to assign
     * @return the number of updated rows
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE application_user " +
            "SET managed_software = ?2 " +
            "WHERE id = ?1", nativeQuery = true)
    Integer updateManagedSoftwareIdByUserId(String inventoryUserId, String managedSoftwareId);

    /**
     * Returns the email addresses of the given application users.
     *
     * @param inventoryUserIds the application user identifiers
     * @return the matching email addresses
     */
    @Query(value = "SELECT email FROM application_user WHERE id IN ?1", nativeQuery = true)
    Set<String> findEmailsByUserIds(List<String> inventoryUserIds);

    /**
     * Returns the subset of the given identifiers that exist as application users.
     *
     * @param userIds the candidate application user identifiers
     * @return the existing application user identifiers
     */
    @Query(value = "SELECT id FROM application_user WHERE id IN ?1", nativeQuery = true)
    Set<String> findExistingUserIds(Set<String> userIds);

    /**
     * Deletes the application users with the given identifiers.
     *
     * @param ids the application user identifiers
     * @return the number of deleted rows
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM application_user WHERE id IN ?1", nativeQuery = true)
    Integer deleteApplicationUsersByIds(Set<String> ids);

    /**
     * Deletes the application user with the given identifier.
     *
     * @param ids the application user identifier
     * @return the number of deleted rows
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM application_user WHERE id = ?1", nativeQuery = true)
    Integer deleteApplicationUsersById(String ids);

    /**
     * Deletes the application users referencing the given managed software.
     *
     * @param managedSoftwareIds the managed software identifier
     * @return the number of deleted rows
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM application_user WHERE managed_software = ?1", nativeQuery = true)
    Integer deleteByManagedSoftwareId(String managedSoftwareIds);

    /**
     * Clears the managed software reference for users referencing the given managed software.
     *
     * @param managedSoftwareIds the managed software identifiers
     * @return the number of updated rows
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE application_user " +
            "SET managed_software = null " +
            "WHERE managed_software IN ?1", nativeQuery = true)
    Integer clearManagedSoftwareByManagedSoftwareIds(List<String> managedSoftwareIds);

    /**
     * Returns the subset of the given identifiers that exist as application users.
     *
     * @param outUserIds the candidate application user identifiers
     * @return the existing application user identifiers
     */
    @Query(value = "SELECT id FROM application_user WHERE id IN ?1", nativeQuery = true)
    Set<String> findUserIdsByIds(Set<String> outUserIds);
}
