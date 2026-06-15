
package io.sclera.Repository;

import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.models.DeviceConditions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;

// JPQL conversion applied 2026-06-11.
// updateDeviceConditions — NOT CONVERTED — stays native: sets device_id column which maps to @ManyToOne Device device;
//     JPQL UPDATE SET cannot target a FK column directly when the field is a relation reference.
// addDeviceConditions    — NOT CONVERTED — stays native: multi-param INSERT (15 cols); no JPQL INSERT syntax.
/**
 * Manages persistence and querying of {@link DeviceConditions} entities.
 */
@Repository
public interface DeviceConditionsRepository extends JpaRepository<DeviceConditions, String> {

    /**
     * Updates the fields of an existing device condition.
     *
     * @param id                  the device-condition identifier
     * @param alert_condition      the alert condition type
     * @param device_id            the device identifier
     * @param alert_profile_id     the alert profile identifier
     * @param trigger_time         the trigger time
     * @param priority             the alert priority
     * @param start_time           the schedule start time
     * @param end_time             the schedule end time
     * @param alert_count          the current alert count
     * @param max_alert_count      the maximum alert count
     * @param alert_count_enabled  the alert-count-enabled flag
     * @param schedule             the schedule flag
     * @param schedule_conditions  the schedule conditions
     * @param alert_count_time     the alert count time window
     * @param last_alerted         the last-alerted flag
     * @param alert_message        the alert message
     */
    // new changes
    // NOT CONVERTED — stays native: device_id maps to @ManyToOne Device; JPQL UPDATE cannot set a relation FK by scalar id.
    @Modifying
    @Transactional
    @Query(value = "UPDATE device_conditions SET alert_condition = ?2, device_id = ?3 , alert_profile_id = ?4 , trigger_time = ?5, priority = ?6, start_time = ?7, end_time = ?8, alert_count = ?9, max_alert_count = ?10, alert_count_enabled = ?11, schedule = ?12, schedule_conditions = ?13, alert_count_time = ?14, last_alerted = ?15, alert_message = ?16 WHERE id = ?1", nativeQuery = true)
    void updateDeviceConditions(String id, String alert_condition, String device_id, String alert_profile_id, Integer trigger_time, String priority, String start_time, String end_time, Integer alert_count, Integer max_alert_count, Integer alert_count_enabled, Integer schedule, String schedule_conditions, Integer alert_count_time, Boolean last_alerted, String alert_message);

    /**
     * Inserts a new device condition.
     *
     * @param id                  the device-condition identifier
     * @param alert_condition      the alert condition type
     * @param device_id            the device identifier
     * @param alert_profile_id     the alert profile identifier
     * @param trigger_time         the trigger time
     * @param priority             the alert priority
     * @param start_time           the schedule start time
     * @param end_time             the schedule end time
     * @param max_alert_count      the maximum alert count
     * @param alert_count_enabled  the alert-count-enabled flag
     * @param schedule             the schedule flag
     * @param schedule_conditions  the schedule conditions
     * @param alert_count_time     the alert count time window
     * @param last_alerted         the last-alerted flag
     * @param alert_message        the alert message
     */
    // NOT CONVERTED — stays native: multi-param INSERT (15 cols); no JPQL INSERT syntax.
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_conditions(id, alert_condition, device_id, alert_profile_id, trigger_time, priority,start_time,end_time,max_alert_count, alert_count_enabled, schedule, schedule_conditions, alert_count_time, last_alerted, alert_message) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15)", nativeQuery = true)
    void addDeviceConditions(String id, String alert_condition, String device_id, String alert_profile_id, Integer trigger_time, String priority, String start_time, String end_time, Integer max_alert_count, Integer alert_count_enabled, Integer schedule, String schedule_conditions, Integer alert_count_time, Boolean last_alerted, String alert_message);

    /**
     * Returns the device conditions for the given device.
     *
     * @param device_id the device identifier
     * @return the matching device-condition projections
     */
    @Query("SELECT new io.sclera.dto.DeviceConditionsDTO(dc.id, dc.alert_condition, dc.device.id, dc.alert_profile_id, dc.trigger_time, dc.last_alerted_time, dc.priority, dc.start_time, dc.end_time, dc.schedule, dc.schedule_conditions, dc.max_alert_count, dc.alert_count, dc.alert_count_enabled, dc.alert_count_time, dc.last_alerted, dc.alert_message) "
            + "FROM DeviceConditions dc WHERE dc.device.id = ?1 AND dc.alert_condition <> 'device_offline_ai_call_alert'")
    Set<DeviceConditionsDTO> getDeviceConditions(String device_id);

    /**
     * Returns a single device condition by its identifier.
     *
     * @param condition_id the device-condition identifier
     * @return the matching device-condition projection
     */
    @Query("SELECT new io.sclera.dto.DeviceConditionsDTO(dc.id, dc.alert_condition, dc.device.id, dc.alert_profile_id, dc.trigger_time, dc.last_alerted_time, dc.priority, dc.start_time, dc.end_time, dc.schedule, dc.schedule_conditions, dc.max_alert_count, dc.alert_count, dc.alert_count_enabled, dc.alert_count_time, dc.last_alerted, dc.alert_message) "
            + "FROM DeviceConditions dc WHERE dc.id = ?1 AND dc.alert_condition <> 'device_offline_ai_call_alert'")
    DeviceConditionsDTO getDeviceConditionsById(String condition_id);

    /**
     * Selects the alert condition for the given device.
     *
     * @param device_id the device identifier
     */
    @Query("SELECT dc.alert_condition FROM DeviceConditions dc WHERE dc.device.id = ?1")
    void getDeviceConditionsByDeviceId(String device_id);

    /**
     * Returns the last-alerted timestamp for the given device.
     *
     * @param device_id the device identifier
     * @return the last-alerted time
     */
    @Query("SELECT dc.last_alerted_time FROM DeviceConditions dc WHERE dc.device.id = ?1")
    BigInteger getLastAlertedTimeByDeviceId(String device_id);

    /**
     * Updates the last-alerted timestamp, flag, and alert count of a device condition.
     *
     * @param id                the device-condition identifier
     * @param last_alerted_time  the last-alerted timestamp
     * @param last_alerted       the last-alerted flag
     * @param alert_count        the alert count
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceConditions dc SET dc.last_alerted_time = ?2, dc.last_alerted = ?3, dc.alert_count = ?4 WHERE dc.id = ?1")
    void updateLastAlertedDetails(String id, BigInteger last_alerted_time, Boolean last_alerted, Integer alert_count);


    /**
     * Clears the last-alerted timestamp of a device condition.
     *
     * @param id the device-condition identifier
     */
    // query to just update last_alerted_time
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceConditions dc SET dc.last_alerted_time = NULL WHERE dc.id = ?1")
    void updateLastAlertedTimestamp(String id);

    /**
     * Clears the alert profile association from all device conditions referencing it.
     *
     * @param alert_profile_id the alert profile identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceConditions dc SET dc.alert_profile_id = NULL WHERE dc.alert_profile_id = ?1")
    void updateAlertProfileId(String alert_profile_id);

    /**
     * Resets the alert count, last-alerted flag, and last-alerted timestamp of a device condition.
     *
     * @param id           the device-condition identifier
     * @param alert_count  the alert count to set
     * @param last_alerted the last-alerted flag to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceConditions dc SET dc.alert_count = ?2, dc.last_alerted = ?3, dc.last_alerted_time = NULL WHERE dc.id = ?1")
    void resetDeviceConditions(String id, Integer alert_count, Boolean last_alerted);


    /**
     * Returns the alert count of the offline AI-call condition for the given device.
     *
     * @param deviceId the device identifier
     * @return the alert count
     */
    @Query("SELECT dc.alert_count FROM DeviceConditions dc WHERE dc.device.id = ?1 AND dc.alert_condition = 'device_offline_ai_call_alert'")
    Integer getAlertCount(String deviceId);

    /**
     * Returns the identifier of the offline AI-call condition for the given device.
     *
     * @param deviceId the device identifier
     * @return the device-condition identifier
     */
    @Query("SELECT dc.id FROM DeviceConditions dc WHERE dc.device.id = ?1 AND dc.alert_condition = 'device_offline_ai_call_alert'")
    String getDeviceConditionIdByDeviceId(String deviceId);

    /**
     * Returns the device conditions relevant to AI-call alerting for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching device-condition projections
     */
    @Query("SELECT new io.sclera.dto.DeviceConditionsDTO(dc.id, dc.alert_condition, dc.device.id, dc.alert_profile_id, dc.trigger_time, dc.last_alerted_time, dc.priority, dc.start_time, dc.end_time, dc.schedule, dc.schedule_conditions, dc.max_alert_count, dc.alert_count, dc.alert_count_enabled, dc.alert_count_time, dc.last_alerted, dc.alert_message) "
            + "FROM DeviceConditions dc WHERE dc.device.id = ?1 AND dc.alert_condition = 'device_offline_ai_call_alert'")
    Set<DeviceConditionsDTO> getDeviceConditionsForAiCall(String deviceId);

    /**
     * Updates the alert count of the offline AI-call condition identified by its identifier.
     *
     * @param id         the device-condition identifier
     * @param alertCount the alert count to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceConditions dc SET dc.alert_count = ?2 WHERE dc.id = ?1 AND dc.alert_condition = 'device_offline_ai_call_alert'")
    void updateAlertCountByConditionId(String id, int alertCount);

    /**
     * Returns the AI-call device condition by its identifier.
     *
     * @param deviceConditionId the device-condition identifier
     * @return the matching device-condition projection
     */
    @Query("SELECT new io.sclera.dto.DeviceConditionsDTO(dc.id, dc.alert_condition, dc.device.id, dc.alert_profile_id, dc.trigger_time, dc.last_alerted_time, dc.priority, dc.start_time, dc.end_time, dc.schedule, dc.schedule_conditions, dc.max_alert_count, dc.alert_count, dc.alert_count_enabled, dc.alert_count_time, dc.last_alerted, dc.alert_message) "
            + "FROM DeviceConditions dc WHERE dc.id = ?1 AND dc.alert_condition = 'device_offline_ai_call_alert'")
    DeviceConditionsDTO getDeviceConditionsByIdForAiCall(String deviceConditionId);
}

