package io.sclera.Repository;

import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import io.sclera.models.ManagedSoftware;
import org.springframework.data.domain.Pageable;
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
    // NOT CONVERTED — stays native: ON CONFLICT upsert already valid PostgreSQL; COALESCE(NULLIF(?2,''),managed_software.name) semantics not expressible in JPQL
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
     * <p>The {@code condition} parameter accepts {@code "all"}, {@code "active"},
     * {@code "expired"}, or {@code "others"}. The {@code searchKey} parameter
     * is matched case-insensitively against name, applicationName, vendor and
     * subscriptionType; pass {@code "null"} (literal string) to skip filtering.
     *
     * <p>Pagination is handled via the trailing {@link Pageable} argument.
     * Callers that previously computed {@code offset} and {@code pageSize} manually
     * should switch to {@code PageRequest.of(pageNo - 1, pageSize)}.
     *
     * @param condition the filter condition to apply
     * @param searchKey the search key to match against, or {@code "null"} for none
     * @param pageable pagination (page + size)
     * @return the matching managed software records
     */
    @Query("SELECT new io.sclera.dto.ManagedSoftwareDTO(" +
           "ms.id, ms.name, ms.applicationName, ms.applicationType, ms.url, " +
           "ms.vendor, ms.subscriptionId, ms.subscriptionType, ms.unitPrice, ms.currency, " +
           "ms.subscriptionStartDate, ms.subscriptionEndDate, ms.status, ms.applicationId) " +
           "FROM ManagedSoftware ms " +
           "WHERE (" +
           "  ?1 = 'all' " +
           "  OR (?1 = 'active' AND ms.status = 'active') " +
           "  OR (?1 = 'expired' AND ms.status = 'expired') " +
           "  OR (?1 = 'others' AND ms.status NOT IN ('active', 'expired')) " +
           ") " +
           "AND (?2 = 'null' OR " +
           "  CONCAT(COALESCE(ms.name,''), COALESCE(ms.applicationName,''), COALESCE(ms.vendor,''), COALESCE(ms.subscriptionType,'')) " +
           "  LIKE CONCAT('%', ?2, '%'))")
    List<ManagedSoftwareDTO> getAllManagedSoftwares(String condition, String searchKey, Pageable pageable);

    /**
     * Returns the managed software records matching the supplied identifiers,
     * ordered by their position in the input list.
     *
     * @param managedSoftwareIds the managed software identifiers
     * @return the set of matching managed software records
     */
    // NOT CONVERTED — stays native: ORDER BY FIELD(ms.id, ?1) is MySQL-specific; no portable JPQL equivalent for list-order preservation
    @Query(nativeQuery = true)
    Set<ManagedSoftwareDTO> getManagedSoftwareByIdList(Set<String> managedSoftwareIds);

    /**
     * Returns the subset of the supplied names that already exist.
     *
     * @param names the candidate names to check
     * @return the names found in the table
     */
    @Query("SELECT ms.name FROM ManagedSoftware ms WHERE ms.name IN ?1")
    List<String> findExistingNames(List<String> names);

    /**
     * Returns the identifier of the managed software with the given name, if present.
     *
     * @param applicationName the software name
     * @return the matching identifier, or empty if none exists
     */
    @Query("SELECT ms.id FROM ManagedSoftware ms WHERE ms.name = ?1")
    Optional<String> getManagedSoftwareIdByName(String applicationName);

    /**
     * Returns the managed software record for the given identifier.
     *
     * @param id the managed software identifier
     * @return the matching managed software record
     */
    @Query("SELECT new io.sclera.dto.ManagedSoftwareDTO(" +
           "ms.id, ms.name, ms.applicationName, ms.applicationType, ms.url, " +
           "ms.vendor, ms.subscriptionId, ms.subscriptionType, ms.unitPrice, ms.currency, " +
           "ms.subscriptionStartDate, ms.subscriptionEndDate, ms.status, ms.applicationId) " +
           "FROM ManagedSoftware ms WHERE ms.id = ?1")
    ManagedSoftwareDTO getManagedSoftwareById(String id);

    /**
     * Returns the users associated with the given managed software.
     *
     * @param managedsoftwareId the managed software identifier
     * @return the associated user records
     */
    @Query("SELECT new io.sclera.dto.ManagedSoftwareUsersDTO(" +
           "ds.username, ds.userUUID, ds.accountType, ds.email, dia.riskStatus, " +
           "ds.deviceName, ds.model, ds.osType) " +
           "FROM ManagedSoftware ms " +
           "JOIN DeviceInstalledApps dia ON dia.managedSoftwareId = ms.id " +
           "JOIN DeviceSpecification ds ON dia.deviceSpecificationId = ds.id " +
           "WHERE ms.id = ?1")
    List<ManagedSoftwareUsersDTO> getManagedSoftwareUsers(String managedsoftwareId);

    /**
     * Returns the total number of managed software records.
     *
     * @return the total count
     */
    @Query("SELECT COUNT(ms) FROM ManagedSoftware ms")
    Integer getAllStatusCounts();

    /**
     * Returns the number of managed software records with an active status.
     *
     * @return the active count
     */
    @Query("SELECT COUNT(ms) FROM ManagedSoftware ms WHERE LOWER(ms.status) = 'active'")
    Integer getActiveStatusCounts();

    /**
     * Returns the number of managed software records with an expired status.
     *
     * @return the expired count
     */
    @Query("SELECT COUNT(ms) FROM ManagedSoftware ms WHERE LOWER(ms.status) = 'expired'")
    Integer getExpiredStatusCounts();

    /**
     * Returns the number of managed software records whose status is neither active nor expired.
     *
     * @return the count of other statuses
     */
    @Query("SELECT COUNT(ms) FROM ManagedSoftware ms WHERE LOWER(ms.status) NOT IN ('active', 'expired')")
    Integer getOthersStatusCounts();

    /**
     * Returns the number of managed software records on a monthly subscription.
     *
     * @return the monthly subscription count
     */
    @Query("SELECT COUNT(ms) FROM ManagedSoftware ms WHERE LOWER(ms.subscriptionType) = 'monthly_fees'")
    Integer getMonthlySubscribedCount();

    /**
     * Returns the number of managed software records on an annual subscription.
     *
     * @return the annual subscription count
     */
    @Query("SELECT COUNT(ms) FROM ManagedSoftware ms WHERE LOWER(ms.subscriptionType) = 'annually'")
    Integer getYearlySubscribedCount();

    /**
     * Returns the distinct non-null application identifiers across all managed software records.
     *
     * @return the set of distinct application identifiers
     */
    @Query("SELECT DISTINCT ms.applicationId FROM ManagedSoftware ms WHERE ms.applicationId IS NOT NULL")
    Set<String> findDistinctApplicationIds();

    /**
     * Returns the application identifier for the given managed software.
     *
     * @param managedsoftwareId the managed software identifier
     * @return the associated application identifier
     */
    @Query("SELECT ms.applicationId FROM ManagedSoftware ms WHERE ms.id = ?1")
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
    @Query("SELECT ms.id FROM ManagedSoftware ms WHERE ms.applicationId = ?1")
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
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ManagedSoftware ms SET ms.status = ?2 WHERE ms.id = ?1")
    void updateStatusById(String id, String status);

}
