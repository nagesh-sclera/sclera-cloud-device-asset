package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.sclera.dto.ConditionsDTO;
import io.sclera.models.Conditions;

@Repository
public interface ConditionsRepository extends JpaRepository<Conditions, String> {

    @Query(value = "SELECT COUNT(*) FROM conditions WHERE id = ?1", nativeQuery = true)
    int conditionById(String id);

//	@Modifying
//	@Transactional
//	@Query(value = "INSERT INTO conditions (id, name, value, alert_message, start_time, end_time, schedule, alert_condition, alert, show_alert, show_alert_message_as_value, "
//			+ "bacnet_object_bacnet_device_id, bacnet_object_id, lorawan_sensor_attributes_lorawan_sensor_id, lorawan_sensor_attributes_name, snmp_device_id, "
//			+ "disruptive_sensor_id, my_devices_sensor_attributes_my_devices_sensor_id, my_devices_sensor_attributes_name, monnit_sensor_id, "
//			+ "pelican_sensor_attributes_pelican_sensor_id, pelican_sensor_attributes_name,knx_group_address, knx_group_knx_device_address, snmp_object_snmp_device_configuration_id, snmp_object_oid) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, "
//			+ "?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18, ?19, ?20, ?21, ?22,?23,?24,?25,?26)", nativeQuery = true)
//	void addCondition(String id, String name, String value, String alert_message, String start_time, String end_time, Integer schedule,
//			String alert_condition, Boolean alert, Boolean show_alert, Boolean show_alert_message_as_value,
//			String bacnet_device_id, String bacnet_object_id, String lorawan_sensor_id,
//			String lorawan_sensor_attributes_name, String snmp_device_id, String disruptive_sensor_id, String my_devices_sensor_id,
//			String my_devices_sensor_attributes_name, String monnit_sensor_id, String pelican_sensor_id, String pelican_sensor_attributes_name,
//			String knx_group_address, String knx_device_address, String snmp_device_configuration_id, String snmp_object_oid);
//
//	@Modifying
//	@Transactional
//	@Query(value = "UPDATE conditions SET name = ?2, value = ?3, alert_message = ?4, start_time = ?5, end_time = ?6, schedule = ?7, alert_condition = ?8, show_alert = ?9, "
//			+ "show_alert_message_as_value = ?10, bacnet_object_bacnet_device_id = ?11, bacnet_object_id = ?12, lorawan_sensor_attributes_lorawan_sensor_id = ?13, "
//			+ "lorawan_sensor_attributes_name = ?14, snmp_device_id = ?15, disruptive_sensor_id = ?16, my_devices_sensor_attributes_my_devices_sensor_id = ?17, "
//			+ "my_devices_sensor_attributes_name = ?18, monnit_sensor_id = ?19, pelican_sensor_attributes_pelican_sensor_id = ?20, "
//			+ "pelican_sensor_attributes_name = ?21,knx_group_address = ?22, knx_group_knx_device_address = ?23,snmp_object_snmp_device_configuration_id = ?24, snmp_object_oid = ?25 "
//			+ "WHERE id = ?1", nativeQuery = true)
//	void updateCondition(String id, String name, String value, String alert_message, String start_time, String end_time, Integer schedule,
//			String alert_condition, Boolean show_alert, Boolean show_alert_message_as_value,
//			String bacnet_device_id, String bacnet_object_id, String lorawan_sensor_id,
//			String lorawan_sensor_attributes_name, String snmp_device_id, String disruptive_sensor_id, String my_devices_sensor_id,
//			String my_devices_sensor_attributes_name, String monnit_sensor_id, String pelican_sensor_id, String pelican_sensor_attributes_name,
//			String knx_group_address, String knx_device_address, String snmp_device_configuration_id, String snmp_object_oid);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO conditions (id, name, value,second_value, alert_message, start_time, end_time, schedule, schedule_conditions, max_alert_count, alert_count_enabled, alert_condition, alert, show_alert, show_alert_message_as_value, "
            + "bacnet_object_bacnet_device_id, bacnet_object_id, lorawan_sensor_attributes_lorawan_sensor_id, lorawan_sensor_attributes_name, snmp_device_id, "
            + "disruptive_sensor_id, my_devices_sensor_attributes_my_devices_sensor_id, my_devices_sensor_attributes_name, monnit_sensor_id, "
            + "pelican_sensor_attributes_pelican_sensor_id, pelican_sensor_attributes_name,knx_group_address, knx_group_knx_device_address, snmp_object_snmp_device_configuration_id, snmp_object_oid,measuring_instrument_id, "
            + "alert_time, daintree_device_id, daintree_point_id, alert_profile_id,ecobee_sensor_attributes_ecobee_sensor_id, ecobee_sensor_attributes_name, modbus_register_id,priority, last_alerted, alert_count_time, enable_threshold_line_onchart, color_of_threshold_line_onchart) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18, ?19, ?20, ?21, ?22,?23,?24,?25,?26,?27,?28,?29, ?30, ?31, ?32,?33,?34,?35,?36,?37, ?38, ?39, ?40, ?41, ?42, ?43)", nativeQuery = true)
    void addCondition(String id, String name, String value, String second_value, String alert_message, String start_time, String end_time, Integer schedule, String schedule_conditions, Integer max_alert_count, Integer alert_count_enabled,
                      String alert_condition, Boolean alert, Boolean show_alert, Boolean show_alert_message_as_value,
                      String bacnet_device_id, String bacnet_object_id, String lorawan_sensor_id,
                      String lorawan_sensor_attributes_name, String snmp_device_id, String disruptive_sensor_id, String my_devices_sensor_id,
                      String my_devices_sensor_attributes_name, String monnit_sensor_id, String pelican_sensor_id, String pelican_sensor_attributes_name,
                      String knx_group_address, String knx_device_address, String snmp_device_configuration_id, String snmp_object_oid, String measuring_instrument_id,
                      Integer alert_time, String daintree_device_id, String daintree_point_id, String alert_profile_id, String ecobee_sensor_id, String ecobee_sensor_attributes_name, String modbus_register_id,
                      String priority, Boolean last_alerted, Integer alert_count_time, Integer enable_threshold_line_onchart, String color_of_threshold_line_onchart);

    @Modifying
    @Transactional
    @Query(value = "UPDATE conditions SET name = ?2, value = ?3, second_value=?4, alert_message = ?5, start_time = ?6, end_time = ?7, schedule = ?8, schedule_conditions =?9, max_alert_count = ?10, alert_count_enabled = ?11, alert_count = ?12, alert_condition = ?13, show_alert = ?14, "
            + "show_alert_message_as_value = ?15, bacnet_object_bacnet_device_id = ?16, bacnet_object_id = ?17, lorawan_sensor_attributes_lorawan_sensor_id = ?18, "
            + "lorawan_sensor_attributes_name = ?19, snmp_device_id = ?20, disruptive_sensor_id = ?21, my_devices_sensor_attributes_my_devices_sensor_id = ?22, "
            + "my_devices_sensor_attributes_name = ?23, monnit_sensor_id = ?24, pelican_sensor_attributes_pelican_sensor_id = ?25, "
            + "pelican_sensor_attributes_name = ?26, knx_group_address = ?27, knx_group_knx_device_address = ?28, snmp_object_snmp_device_configuration_id = ?29, snmp_object_oid = ?30,measuring_instrument_id = ?31, alert_time = ?32,daintree_device_id = ?33, daintree_point_id = ?34 , alert_profile_id = ?35 , ecobee_sensor_attributes_ecobee_sensor_id = ?36, ecobee_sensor_attributes_name= ?37, modbus_register_id= ?38, priority= ?39, last_alerted = ?40, alert_count_time = ?41, enable_threshold_line_onchart = ?42 , color_of_threshold_line_onchart = ?43"
            + " WHERE id = ?1", nativeQuery = true)
    void updateCondition(String id, String name, String value, String second_value, String alert_message, String start_time, String end_time, Integer schedule, String schedule_conditions, Integer max_alert_count, Integer alert_count_enabled, Integer alert_count,
                         String alert_condition, Boolean show_alert, Boolean show_alert_message_as_value,
                         String bacnet_device_id, String bacnet_object_id, String lorawan_sensor_id,
                         String lorawan_sensor_attributes_name, String snmp_device_id, String disruptive_sensor_id, String my_devices_sensor_id,
                         String my_devices_sensor_attributes_name, String monnit_sensor_id, String pelican_sensor_id, String pelican_sensor_attributes_name,
                         String knx_group_address, String knx_device_address, String snmp_device_configuration_id, String snmp_object_oid, String measuring_instrument_id,
                         Integer alert_time, String daintree_device_id, String daintree_point_id, String alert_profile_id, String ecobee_sensor_id, String ecobee_sensor_attributes_name, String modbus_register_id,
                         String priority, Boolean last_alerted, Integer alert_count_time, Integer enable_threshold_line_onchart, String color_of_threshold_line_onchart);

    //delete is done by cascade delete, if this query not required can be deleted
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM conditions WHERE id = ?1", nativeQuery = true)
    void deleteConditionById(String conditionId);

    @Query(nativeQuery = true)
    Set<ConditionsDTO> getConditions(String bacnet_object_id, String bacnet_device_id, String lorawan_sensor_id, String snmp_device_id,
                                     String disruptive_sensor_id, String my_devices_sensor_id, String monnit_sensor_id, String pelican_sensor_id, String knx_group_address,
                                     String knx_device_address, String snmp_device_configuration_id, String snmp_object_oid, String measuring_instrument_id, String daintree_device_id, String ecobee_sensor_id, String modbus_register_id);

    @Query(nativeQuery = true)
    Set<ConditionsDTO> getConditionsById(String bacnet_device_id, String bacnet_object_id, String lorawan_sensor_id,
                                         String lorawan_sensor_attributes_name, String snmp_device_id, String disruptive_sensor_id, String my_devices_sensor_id,
                                         String my_devices_sensor_attributes_name, String monnit_sensor_id, String pelican_sensor_id, String pelican_sensor_attributes_name,
                                         String knx_group_address, String knx_device_address, String snmp_device_configuration_id, String snmp_object_oid, String measuring_instrument_id, String daintree_device_id,
                                         String daintree_point_id, String ecobee_sensor_id, String ecobee_sensor_attributes_name, String modbus_register_id);

//	@Modifying
//	@Transactional
//	@Query(value = "UPDATE conditions SET alert = ?2 WHERE id = ?1", nativeQuery = true)
//	void updateConditionAlert(String id, Boolean alert);

    @Modifying
    @Transactional
    @Query(value = "UPDATE conditions SET alert = ?2, alert_count = ?3, last_alerted_timestamp = ?4, last_alerted = ?5  WHERE id = ?1", nativeQuery = true)
    void updateConditionAlert(String id, Boolean alert, Integer alert_count, BigInteger last_alerted_timestamp, Boolean last_alerted);

    //update condition alert count
    @Modifying
    @Transactional
    @Query(value = "UPDATE conditions SET alert_count = ?2  WHERE id = ?1", nativeQuery = true)
    void updateConditionAlertCount(String id, Integer alert_count);

    //get alert count details
    @Query(nativeQuery = true)
    ConditionsDTO getConditionAlertCountDetails(String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE conditions SET alert_profile_id = NULL  WHERE alert_profile_id = ?1", nativeQuery = true)
    void updateAlertProfileId(String alert_profile_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE conditions SET last_alerted = ?2  WHERE id = ?1", nativeQuery = true)
    void resetLastAlertById(String id, Boolean last_alerted);

    @Query(nativeQuery = true)
    ConditionsDTO getConditionByConditionId(String conditionId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE conditions SET last_alerted_timestamp = NULL WHERE id = ?1", nativeQuery = true)
    Object updateLastAlertedTimestamp(String id);

    @Query( nativeQuery = true,
            value = "SELECT c.id AS condition_id,c.name AS condition_name,c.alert_condition AS alert_condition,c.value AS value_name,c.alert_message AS alert_message,c.priority AS priority,ap.id AS alert_profile_id,ap.name AS alert_profile_name,ap.ioc AS ioc,mi.id AS measuring_instrument_id,c.show_alert AS show_alert,c.show_alert_message_as_value AS show_alert_message_as_value,c.enable_threshold_line_onchart AS enable_threshold_line_onchart,c.color_of_threshold_line_onchart AS color_of_threshold_line_onchart,c.alert AS alert_after,c.alert_time AS alert_time, c.schedule AS schedule_alert, c.start_time AS schedule_start_time, c.end_time AS schedule_end_time,c.schedule_conditions AS schedule_conditions , c.alert_count_enabled AS alert_count_enable  " +
                    "FROM device d " +
                    "JOIN measuring_instrument mi " +
                    "ON mi.device_id = d.id " +
                    "JOIN conditions c " +
                    "ON c.measuring_instrument_id = mi.id " +
                    "LEFT JOIN alert_profile ap " +
                    "ON ap.id = c.alert_profile_id " +
                    "WHERE d.id = ?1 "
    )
    List<Map<String,Object>> getConditionsForAdvanceExcelExport(String deviceId);
}
