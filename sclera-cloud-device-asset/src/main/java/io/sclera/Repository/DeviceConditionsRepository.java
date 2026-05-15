
package io.sclera.Repository;

import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.models.DeviceConditions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;
@Repository
public interface DeviceConditionsRepository extends JpaRepository<DeviceConditions, String> {

    // new changes
    @Modifying
    @Transactional
    @Query(value = "UPDATE device_conditions SET alert_condition = ?2, device_id = ?3 , alert_profile_id = ?4 , trigger_time = ?5, priority = ?6, start_time = ?7, end_time = ?8, alert_count = ?9, max_alert_count = ?10, alert_count_enabled = ?11, schedule = ?12, schedule_conditions = ?13, alert_count_time = ?14, last_alerted = ?15, alert_message = ?16 WHERE id = ?1", nativeQuery = true)
    void updateDeviceConditions(String id, String alert_condition, String device_id, String alert_profile_id, Integer trigger_time, String priority, String start_time, String end_time, Integer alert_count, Integer max_alert_count, Integer alert_count_enabled, Integer schedule, String schedule_conditions, Integer alert_count_time, Boolean last_alerted, String alert_message);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_conditions(id, alert_condition, device_id, alert_profile_id, trigger_time, priority,start_time,end_time,max_alert_count, alert_count_enabled, schedule, schedule_conditions, alert_count_time, last_alerted, alert_message) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15)", nativeQuery = true)
    void addDeviceConditions(String id, String alert_condition, String device_id, String alert_profile_id, Integer trigger_time, String priority, String start_time, String end_time, Integer max_alert_count, Integer alert_count_enabled, Integer schedule, String schedule_conditions, Integer alert_count_time, Boolean last_alerted, String alert_message);

    @Query(nativeQuery = true)
    Set<DeviceConditionsDTO> getDeviceConditions(String device_id);

    @Query(nativeQuery = true)
    DeviceConditionsDTO getDeviceConditionsById(String condition_id);

    @Query(value = "SELECT alert_condition FROM device_conditions WHERE device_id = ?1 ", nativeQuery = true)
    void getDeviceConditionsByDeviceId(String device_id);

    @Query(value = "SELECT last_alerted_time FROM device_conditions WHERE device_id = ?1 ", nativeQuery = true)
    BigInteger getLastAlertedTimeByDeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_conditions SET last_alerted_time = ?2, last_alerted = ?3, alert_count = ?4 WHERE id =?1 ", nativeQuery = true)
    void updateLastAlertedDetails(String id, BigInteger last_alerted_time, Boolean last_alerted, Integer alert_count);


    // query to just update last_alerted_time
    @Modifying
    @Transactional
    @Query(value = "UPDATE device_conditions SET last_alerted_time = NULL WHERE id =?1 ", nativeQuery = true)
    void updateLastAlertedTimestamp(String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_conditions SET alert_profile_id = NULL  WHERE alert_profile_id = ?1", nativeQuery = true)
    void updateAlertProfileId(String alert_profile_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_conditions SET alert_count = ?2, last_alerted = ?3, last_alerted_time = NULL  WHERE id = ?1", nativeQuery = true)
    void resetDeviceConditions(String id, Integer alert_count, Boolean last_alerted);

    @Query(value = "SELECT COUNT(*) FROM device_conditions WHERE id = ?1", nativeQuery = true)
    int deviceConditionById(String id);

    @Query(value = "SELECT alert_count FROM device_conditions WHERE device_id = ?1 AND alert_condition = 'device_offline_ai_call_alert'", nativeQuery = true)
    Integer getAlertCount(String deviceId);

    @Query(value = "SELECT id FROM device_conditions WHERE device_id = ?1 AND alert_condition = 'device_offline_ai_call_alert'", nativeQuery = true)
    String getDeviceConditionIdByDeviceId(String deviceId);

    @Query(nativeQuery = true)
    Set<DeviceConditionsDTO> getDeviceConditionsForAiCall(String deviceId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_conditions SET alert_count = ?2 WHERE id = ?1 AND alert_condition = 'device_offline_ai_call_alert'", nativeQuery = true)
    void updateAlertCountByConditionId(String id, int alertCount);

    @Query(nativeQuery = true)
    DeviceConditionsDTO getDeviceConditionsByIdForAiCall(String deviceConditionId);
}

