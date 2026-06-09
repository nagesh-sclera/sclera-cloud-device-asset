package io.sclera.Repository;

import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import io.sclera.models.ManagedSoftware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Manages persistence and querying of {@link ManagedSoftware} entities.
 */
@Repository
public interface ManagedSoftwareRepository extends JpaRepository<ManagedSoftware, String> {

    /**
     * Inserts a managed software record, or updates it when the identifier already exists.
     *
     * @param id the managed software identifier
     * @param name the software name (retained when the supplied value is blank)
     * @param applicationName the application name
     * @param applicationType the application type
     * @param url the software URL
     * @param vendor the vendor name
     * @param subscriptionId the subscription identifier
     * @param subscriptionType the subscription type
     * @param unitPrice the unit price
     * @param currency the price currency
     * @param subscriptionStartDate the epoch subscription start date
     * @param subscriptionEndDate the epoch subscription end date
     * @param status the subscription status
     * @param applicationId the associated application identifier
     */
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value =
        "INSERT INTO managed_software (" +
        "id, name, application_name, application_type, url, vendor, " +
        "subscription_id, subscription_type, unit_price, currency, subscription_start_date, subscription_end_date, " +
        "status, application_id " +
        ") VALUES (" +
        "?1, ?2, ?3, ?4, ?5, ?6, ?7, " +
        "?8, ?9, ?10, ?11, ?12, ?13, ?14 " +
        ") ON CONFLICT (id) DO UPDATE SET " +
        "name = COALESCE(NULLIF(?2, ''), managed_software.name), " +
        "application_name = EXCLUDED.application_name, " +
        "application_type = EXCLUDED.application_type, " +
        "url = EXCLUDED.url, " +
        "vendor = EXCLUDED.vendor, " +
        "subscription_id = EXCLUDED.subscription_id, " +
        "subscription_type = EXCLUDED.subscription_type, " +
        "unit_price = EXCLUDED.unit_price, " +
        "currency = EXCLUDED.currency, " +
        "subscription_start_date = EXCLUDED.subscription_start_date, " +
        "subscription_end_date = EXCLUDED.subscription_end_date, " +
        "status = EXCLUDED.status, " +
        "application_id = EXCLUDED.application_id",
        nativeQuery = true)
    void upsertManagedSoftware(
            String id, String name, String applicationName, String applicationType, String url, String vendor,
            String subscriptionId, String subscriptionType, Double unitPrice, String currency,
            BigInteger subscriptionStartDate, BigInteger subscriptionEndDate,
            String status, String applicationId
    );

    /**
     * Returns a paginated, filtered list of managed software records.
     *
     * @param condition the filter condition to apply
     * @param searchKey the search key to match against
     * @param offset the starting row offset
     * @param pageSize the maximum number of rows to return
     * @return the matching managed software records
     */
    @Query(nativeQuery = true)
    List<ManagedSoftwareDTO> getAllManagedSoftwares(String condition, String searchKey, Integer offset, Integer pageSize);

    /**
     * Returns the managed software records matching the supplied identifiers.
     *
     * @param managedSoftwareIds the managed software identifiers
     * @return the set of matching managed software records
     */
    @Query(nativeQuery = true)
    Set<ManagedSoftwareDTO> getManagedSoftwareByIdList(Set<String> managedSoftwareIds);

    /**
     * Returns the subset of the supplied names that already exist.
     *
     * @param names the candidate names to check
     * @return the names found in the table
     */
    @Query(value = "SELECT m.name FROM managed_software m WHERE m.name IN ?1 ", nativeQuery = true)
    List<String> findExistingNames(List<String> names);

    /**
     * Returns the identifier of the managed software with the given name, if present.
     *
     * @param applicationName the software name
     * @return the matching identifier, or empty if none exists
     */
    @Query(value = "SELECT m.id FROM managed_software m WHERE m.name = ?1", nativeQuery = true)
    Optional<String> getManagedSoftwareIdByName(String applicationName);

    /**
     * Returns the managed software record for the given identifier.
     *
     * @param id the managed software identifier
     * @return the matching managed software record
     */
    @Query(nativeQuery = true)
    ManagedSoftwareDTO getManagedSoftwareById(String id);

    /**
     * Returns the users associated with the given managed software.
     *
     * @param managedsoftwareId the managed software identifier
     * @return the associated user records
     */
    @Query(name = "ManagedSoftware.getManagedSoftwareUsers", nativeQuery = true)
    List<ManagedSoftwareUsersDTO> getManagedSoftwareUsers(String managedsoftwareId);

    /**
     * Returns the total number of managed software records.
     *
     * @return the total count
     */
    @Query(value = "SELECT COUNT(*) FROM managed_software", nativeQuery = true)
    Integer getAllStatusCounts();

    /**
     * Returns the number of managed software records with an active status.
     *
     * @return the active count
     */
    @Query(value = "SELECT COUNT(*) FROM managed_software WHERE LOWER(status) = 'active'", nativeQuery = true)
    Integer getActiveStatusCounts();

    /**
     * Returns the number of managed software records with an expired status.
     *
     * @return the expired count
     */
    @Query(value = "SELECT COUNT(*) FROM managed_software WHERE LOWER(status) = 'expired'", nativeQuery = true)
    Integer getExpiredStatusCounts();

    /**
     * Returns the number of managed software records whose status is neither active nor expired.
     *
     * @return the count of other statuses
     */
    @Query(value = "SELECT COUNT(*) FROM managed_software WHERE LOWER(status) NOT IN ('active', 'expired')", nativeQuery = true)
    Integer getOthersStatusCounts();

    /**
     * Returns the number of managed software records on a monthly subscription.
     *
     * @return the monthly subscription count
     */
    @Query(value = "SELECT COUNT(*) FROM managed_software WHERE LOWER(subscription_type) = 'monthly_fees'", nativeQuery = true)
    Integer getMonthlySubscribedCount();

    /**
     * Returns the number of managed software records on an annual subscription.
     *
     * @return the annual subscription count
     */
    @Query(value = "SELECT COUNT(*) FROM managed_software WHERE LOWER(subscription_type) = 'annually'", nativeQuery = true)
    Integer getYearlySubscribedCount();

    /**
     * Returns the distinct non-null application identifiers across all managed software records.
     *
     * @return the set of distinct application identifiers
     */
    @Query(value = "SELECT DISTINCT application_id FROM managed_software WHERE application_id IS NOT NULL", nativeQuery = true)
    Set<String> findDistinctApplicationIds();

    /**
     * Returns the application identifier for the given managed software.
     *
     * @param managedsoftwareId the managed software identifier
     * @return the associated application identifier
     */
    @Query(value = "SELECT application_id FROM managed_software WHERE id = ?1", nativeQuery = true)
    String getApplicationIdByManagedSoftwareId(String managedsoftwareId);

    /**
     * Returns the managed software record for the given application identifier.
     *
     * @param applicationId the application identifier
     * @return the matching managed software entity
     */
    ManagedSoftware findByApplicationId(String applicationId);

    /**
     * Returns the managed software identifier for the given application identifier.
     *
     * @param applicationId the application identifier
     * @return the matching managed software identifier
     */
    @Query(value = "SELECT id FROM managed_software WHERE application_id = ?1", nativeQuery = true)
    String findIdByApplicationId(String applicationId);

    /**
     * Returns the managed software records matching the supplied application identifiers.
     *
     * @param applicationIds the application identifiers
     * @return the matching managed software entities
     */
    List<ManagedSoftware> findByApplicationIdIn(List<String> applicationIds);

    /**
     * Updates the status of the managed software with the given identifier.
     *
     * @param id the managed software identifier
     * @param status the new status value
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE managed_software SET status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateStatusById(String id, String status);

}
