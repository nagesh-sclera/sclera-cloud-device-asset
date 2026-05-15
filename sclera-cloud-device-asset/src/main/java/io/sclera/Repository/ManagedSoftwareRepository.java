package io.sclera.Repository;

import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import io.sclera.models.ManagedSoftware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ManagedSoftwareRepository extends JpaRepository<ManagedSoftware, String> {

    @Modifying
    @Transactional
    @Query(value =
        "INSERT INTO managed_software (" +
        "id, name, application_name, application_type, url, vendor, " +
        "subscription_id, subscription_type, unit_price, currency, subscription_start_date, subscription_end_date, " +
        "status, application_id " +
        ") VALUES (" +
        "?1, ?2, ?3, ?4, ?5, ?6, ?7, " +
        "?8, ?9, ?10, ?11, ?12, ?13, ?14 " +
        ") ON DUPLICATE KEY UPDATE " +
        "name = COALESCE(NULLIF(?2, ''), name), " +
        "application_name = ?3, " +
        "application_type = ?4, " +
        "url = ?5, " +
        "vendor = ?6, " +
        "subscription_id = ?7, " +
        "subscription_type = ?8, " +
        "unit_price = ?9, " +
        "currency = ?10, " +
        "subscription_start_date = ?11, " +
        "subscription_end_date = ?12, " +
        "status = ?13, " +
        "application_id = ?14 ",
        nativeQuery = true)
    void upsertManagedSoftware(
            String id, String name, String applicationName, String applicationType, String url, String vendor,
            String subscriptionId, String subscriptionType, Double unitPrice, String currency,
            BigInteger subscriptionStartDate, BigInteger subscriptionEndDate,
            String status, String applicationId
    );

    @Query(nativeQuery = true)
    List<ManagedSoftwareDTO> getAllManagedSoftwares(String condition, String searchKey, Integer offset, Integer pageSize);

    @Query(nativeQuery = true)
    Set<ManagedSoftwareDTO> getManagedSoftwareByIdList(Set<String> managedSoftwareIds);

    @Query(value = "SELECT m.name FROM managed_software m WHERE m.name IN ?1 ", nativeQuery = true)
    List<String> findExistingNames(List<String> names);

    @Query(value = "SELECT m.id FROM managed_software m WHERE m.name = ?1", nativeQuery = true)
    Optional<String> getManagedSoftwareIdByName(String applicationName);

    @Query(nativeQuery = true)
    ManagedSoftwareDTO getManagedSoftwareById(String id);

    @Query(name = "ManagedSoftware.getManagedSoftwareUsers", nativeQuery = true)
    List<ManagedSoftwareUsersDTO> getManagedSoftwareUsers(String managedsoftwareId);

    @Query(value = "SELECT COUNT(*) FROM managed_software", nativeQuery = true)
    Integer getAllStatusCounts();

    @Query(value = "SELECT COUNT(*) FROM managed_software WHERE LOWER(status) = 'active'", nativeQuery = true)
    Integer getActiveStatusCounts();

    @Query(value = "SELECT COUNT(*) FROM managed_software WHERE LOWER(status) = 'expired'", nativeQuery = true)
    Integer getExpiredStatusCounts();

    @Query(value = "SELECT COUNT(*) FROM managed_software WHERE LOWER(status) NOT IN ('active', 'expired')", nativeQuery = true)
    Integer getOthersStatusCounts();

    @Query(value = "SELECT COUNT(*) FROM managed_software WHERE LOWER(subscription_type) = 'monthly_fees'", nativeQuery = true)
    Integer getMonthlySubscribedCount();

    @Query(value = "SELECT COUNT(*) FROM managed_software WHERE LOWER(subscription_type) = 'annually'", nativeQuery = true)
    Integer getYearlySubscribedCount();

    @Query(value = "SELECT DISTINCT application_id FROM managed_software WHERE application_id IS NOT NULL", nativeQuery = true)
    Set<String> findDistinctApplicationIds();

    @Query(value = "SELECT application_id FROM managed_software WHERE id = ?1", nativeQuery = true)
    String getApplicationIdByManagedSoftwareId(String managedsoftwareId);

    @Query(value = "SELECT * FROM managed_software WHERE application_id = ?1", nativeQuery = true)
    ManagedSoftware findByApplicationId(String applicationId);

    @Query(value = "SELECT id FROM managed_software WHERE application_id = ?1", nativeQuery = true)
    String findIdByApplicationId(String applicationId);

    @Query(value = "SELECT * FROM managed_software WHERE application_id IN ?1", nativeQuery = true)
    List<ManagedSoftware> findByApplicationIds(List<String> applicationIds);

    @Modifying
    @Transactional
    @Query(value = "UPDATE managed_software SET status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateStatusById(String id, String status);

}
