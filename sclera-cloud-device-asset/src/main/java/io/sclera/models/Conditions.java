package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

@NamedNativeQueries({
    @NamedNativeQuery(name = "Conditions.conditionById",
        query = "SELECT COUNT(*) FROM conditions WHERE id = ?1",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.addCondition",
        query = "INSERT INTO conditions (id, name, value, second_value, alert_message, start_time, end_time, schedule, schedule_conditions, max_alert_count, alert_count_enabled, alert_condition, alert, show_alert, show_alert_message_as_value, bacnet_object_bacnet_device_id, bacnet_object_id, lorawan_sensor_attributes_lorawan_sensor_id, lorawan_sensor_attributes_name, snmp_device_id, disruptive_sensor_id, my_devices_sensor_attributes_my_devices_sensor_id, my_devices_sensor_attributes_name, monnit_sensor_id, pelican_sensor_attributes_pelican_sensor_id, pelican_sensor_attributes_name, knx_group_address, knx_group_knx_device_address, snmp_object_snmp_device_configuration_id, snmp_object_oid, measuring_instrument_id, alert_time, daintree_device_id, daintree_point_id, alert_profile_id, ecobee_sensor_attributes_ecobee_sensor_id, ecobee_sensor_attributes_name, modbus_register_id, priority, last_alerted, alert_count_time, enable_threshold_line_onchart, color_of_threshold_line_onchart) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18, ?19, ?20, ?21, ?22, ?23, ?24, ?25, ?26, ?27, ?28, ?29, ?30, ?31, ?32, ?33, ?34, ?35, ?36, ?37, ?38, ?39, ?40, ?41, ?42, ?43)",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.updateCondition",
        query = "UPDATE conditions SET name = ?2, value = ?3, second_value = ?4, alert_message = ?5, start_time = ?6, end_time = ?7, schedule = ?8, schedule_conditions = ?9, max_alert_count = ?10, alert_count_enabled = ?11, alert_count = ?12, alert_condition = ?13, show_alert = ?14, show_alert_message_as_value = ?15, bacnet_object_bacnet_device_id = ?16, bacnet_object_id = ?17, lorawan_sensor_attributes_lorawan_sensor_id = ?18, lorawan_sensor_attributes_name = ?19, snmp_device_id = ?20, disruptive_sensor_id = ?21, my_devices_sensor_attributes_my_devices_sensor_id = ?22, my_devices_sensor_attributes_name = ?23, monnit_sensor_id = ?24, pelican_sensor_attributes_pelican_sensor_id = ?25, pelican_sensor_attributes_name = ?26, knx_group_address = ?27, knx_group_knx_device_address = ?28, snmp_object_snmp_device_configuration_id = ?29, snmp_object_oid = ?30, measuring_instrument_id = ?31, alert_time = ?32, daintree_device_id = ?33, daintree_point_id = ?34, alert_profile_id = ?35, ecobee_sensor_attributes_ecobee_sensor_id = ?36, ecobee_sensor_attributes_name = ?37, modbus_register_id = ?38, priority = ?39, last_alerted = ?40, alert_count_time = ?41, enable_threshold_line_onchart = ?42, color_of_threshold_line_onchart = ?43 WHERE id = ?1",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.deleteConditionById",
        query = "DELETE FROM conditions WHERE id = ?1",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.getConditions",
        query = "SELECT id, name, value, second_value, alert_message, start_time, end_time, schedule, schedule_conditions, alert_count_enabled, max_alert_count, alert_count, alert_condition, alert, show_alert, show_alert_message_as_value, bacnet_object_bacnet_device_id AS bacnet_device_id, bacnet_object_id, lorawan_sensor_attributes_lorawan_sensor_id AS lorawan_sensor_id, lorawan_sensor_attributes_name, snmp_device_id, disruptive_sensor_id, my_devices_sensor_attributes_my_devices_sensor_id AS my_devices_sensor_id, my_devices_sensor_attributes_name, monnit_sensor_id, pelican_sensor_attributes_pelican_sensor_id AS pelican_sensor_id, pelican_sensor_attributes_name, knx_group_address, knx_group_knx_device_address AS knx_device_address, snmp_object_snmp_device_configuration_id AS snmp_device_configuration_id, snmp_object_oid, measuring_instrument_id, last_alerted_timestamp, alert_time, daintree_device_id, daintree_point_id, alert_profile_id, ecobee_sensor_attributes_ecobee_sensor_id AS ecobee_sensor_id, ecobee_sensor_attributes_name, modbus_register_id, priority, last_alerted, alert_count_time, enable_threshold_line_onchart, color_of_threshold_line_onchart FROM conditions WHERE (bacnet_object_id = ?1 AND bacnet_object_bacnet_device_id = ?2) OR lorawan_sensor_attributes_lorawan_sensor_id = ?3 OR snmp_device_id = ?4 OR disruptive_sensor_id = ?5 OR my_devices_sensor_attributes_my_devices_sensor_id = ?6 OR monnit_sensor_id = ?7 OR pelican_sensor_attributes_pelican_sensor_id = ?8 OR knx_group_address = ?9 OR knx_group_knx_device_address = ?10 OR (snmp_object_snmp_device_configuration_id = ?11 AND snmp_object_oid = ?12) OR measuring_instrument_id = ?13 OR daintree_device_id = ?14 OR ecobee_sensor_attributes_ecobee_sensor_id = ?15 OR modbus_register_id = ?16",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.getConditionsById",
        query = "SELECT id, name, value, second_value, alert_message, start_time, end_time, schedule, schedule_conditions, alert_count_enabled, max_alert_count, alert_count, alert_condition, alert, show_alert, show_alert_message_as_value, bacnet_object_bacnet_device_id AS bacnet_device_id, bacnet_object_id, lorawan_sensor_attributes_lorawan_sensor_id AS lorawan_sensor_id, lorawan_sensor_attributes_name, snmp_device_id, disruptive_sensor_id, my_devices_sensor_attributes_my_devices_sensor_id AS my_devices_sensor_id, my_devices_sensor_attributes_name, monnit_sensor_id, pelican_sensor_attributes_pelican_sensor_id AS pelican_sensor_id, pelican_sensor_attributes_name, knx_group_address, knx_group_knx_device_address AS knx_device_address, snmp_object_snmp_device_configuration_id AS snmp_device_configuration_id, snmp_object_oid, measuring_instrument_id, last_alerted_timestamp, alert_time, daintree_device_id, daintree_point_id, alert_profile_id, ecobee_sensor_attributes_ecobee_sensor_id AS ecobee_sensor_id, ecobee_sensor_attributes_name, modbus_register_id, priority, last_alerted, alert_count_time, enable_threshold_line_onchart, color_of_threshold_line_onchart FROM conditions WHERE (bacnet_object_bacnet_device_id = ?1 AND bacnet_object_id = ?2) OR (lorawan_sensor_attributes_lorawan_sensor_id = ?3 AND lorawan_sensor_attributes_name = ?4) OR snmp_device_id = ?5 OR disruptive_sensor_id = ?6 OR (my_devices_sensor_attributes_my_devices_sensor_id = ?7 AND my_devices_sensor_attributes_name = ?8) OR monnit_sensor_id = ?9 OR (pelican_sensor_attributes_pelican_sensor_id = ?10 AND pelican_sensor_attributes_name = ?11) OR knx_group_address = ?12 OR knx_group_knx_device_address = ?13 OR (snmp_object_snmp_device_configuration_id = ?14 AND snmp_object_oid = ?15) OR measuring_instrument_id = ?16 OR (daintree_device_id = ?17 AND daintree_point_id = ?18) OR (ecobee_sensor_attributes_ecobee_sensor_id = ?19 AND ecobee_sensor_attributes_name = ?20) OR modbus_register_id = ?21",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.updateConditionAlert",
        query = "UPDATE conditions SET alert = ?2, alert_count = ?3, last_alerted_timestamp = ?4, last_alerted = ?5 WHERE id = ?1",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.updateConditionAlertCount",
        query = "UPDATE conditions SET alert_count = ?2 WHERE id = ?1",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.getConditionAlertCountDetails",
        query = "SELECT id, alert_count_enabled, max_alert_count, alert_count, alert_time FROM conditions WHERE id = ?1",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.updateAlertProfileId",
        query = "UPDATE conditions SET alert_profile_id = NULL WHERE alert_profile_id = ?1",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.resetLastAlertById",
        query = "UPDATE conditions SET last_alerted = ?2 WHERE id = ?1",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.getConditionByConditionId",
        query = "SELECT id, name, value, second_value, alert_message, start_time, end_time, schedule, schedule_conditions, alert_count_enabled, max_alert_count, alert_count, alert_condition, alert, show_alert, show_alert_message_as_value, bacnet_object_bacnet_device_id AS bacnet_device_id, bacnet_object_id, lorawan_sensor_attributes_lorawan_sensor_id AS lorawan_sensor_id, lorawan_sensor_attributes_name, snmp_device_id, disruptive_sensor_id, my_devices_sensor_attributes_my_devices_sensor_id AS my_devices_sensor_id, my_devices_sensor_attributes_name, monnit_sensor_id, pelican_sensor_attributes_pelican_sensor_id AS pelican_sensor_id, pelican_sensor_attributes_name, knx_group_address, knx_group_knx_device_address AS knx_device_address, snmp_object_snmp_device_configuration_id AS snmp_device_configuration_id, snmp_object_oid, measuring_instrument_id, last_alerted_timestamp, alert_time, daintree_device_id, daintree_point_id, alert_profile_id, ecobee_sensor_attributes_ecobee_sensor_id AS ecobee_sensor_id, ecobee_sensor_attributes_name, modbus_register_id, priority, last_alerted, alert_count_time, enable_threshold_line_onchart, color_of_threshold_line_onchart FROM conditions WHERE id = ?1",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.updateLastAlertedTimestamp",
        query = "UPDATE conditions SET last_alerted_timestamp = NULL WHERE id = ?1",
        resultClass = Conditions.class),
    @NamedNativeQuery(name = "Conditions.getConditionsForAdvanceExcelExport",
        query = "SELECT c.id AS condition_id, c.name AS condition_name, c.alert_condition AS alert_condition, c.value AS value_name, c.alert_message AS alert_message, c.priority AS priority, ap.id AS alert_profile_id, ap.name AS alert_profile_name, ap.ioc AS ioc, mi.id AS measuring_instrument_id, c.show_alert AS show_alert, c.show_alert_message_as_value AS show_alert_message_as_value, c.enable_threshold_line_onchart AS enable_threshold_line_onchart, c.color_of_threshold_line_onchart AS color_of_threshold_line_onchart, c.alert AS alert_after, c.alert_time AS alert_time, c.schedule AS schedule_alert, c.start_time AS schedule_start_time, c.end_time AS schedule_end_time, c.schedule_conditions AS schedule_conditions, c.alert_count_enabled AS alert_count_enable FROM device d JOIN measuring_instrument mi ON mi.device_id = d.id JOIN conditions c ON c.measuring_instrument_id = mi.id LEFT JOIN alert_profile ap ON ap.id = c.alert_profile_id WHERE d.id = ?1",
        resultClass = Conditions.class)
})
@Entity
@Table(name = "conditions")
public class Conditions {
    @Id
    private Long id;

    @ManyToOne
    private Bacnet_Object bacnet_object;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Bacnet_Object getBacnet_object() { return bacnet_object; }
    public void setBacnet_object(Bacnet_Object bacnet_object) { this.bacnet_object = bacnet_object; }
}
