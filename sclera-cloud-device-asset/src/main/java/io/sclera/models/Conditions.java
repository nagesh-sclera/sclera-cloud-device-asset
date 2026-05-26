package io.sclera.models;

// PG-restore: scalar columns + scalar FK fields referenced by alert/sensor native queries
// (loose coupling; cross-module @ManyToOne replaced by scalar *_id).
// PK changed from Long to String to match monolith + ConditionsRepository<Conditions, String>.
// @OneToMany(mappedBy="bacnet_object") was removed from Bacnet_Object; FK stored as plain columns here.

import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.Table;

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

    // --- Primary Key ---
    @Id
    private String id;

    // --- Scalar columns ---

    @Column(length = 128)
    private String name;

    @Column(length = 128)
    private String value;

    @Column(length = 128)
    private String second_value;

    private String alert_message;

    @Column(length = 64)
    private String start_time;

    @Column(length = 64)
    private String end_time;

    @Column(length = 64)
    private String alert_condition;

    @Column(columnDefinition = "boolean default false")
    private Boolean alert;

    @Column(columnDefinition = "boolean default false")
    private Boolean show_alert;

    @Column(columnDefinition = "boolean default false")
    private Boolean show_alert_message_as_value;

    @Column(length = 8, columnDefinition = "integer default 0")
    private Integer schedule;

    private String schedule_conditions;

    @Column(columnDefinition = "integer default 0")
    private Integer max_alert_count;

    @Column(columnDefinition = "integer default 0")
    private Integer alert_count;

    @Column(columnDefinition = "integer default 0")
    private Integer alert_count_enabled;

    // BigInteger maps to NUMERIC in PG (no length constraint needed)
    private BigInteger last_alerted_timestamp;

    private Integer alert_time;

    @Column(length = 128)
    private String priority;

    @Column(columnDefinition = "boolean default false")
    private Boolean last_alerted;

    @Column
    private Integer alert_count_time;

    @Column
    private Integer enable_threshold_line_onchart;

    @Column
    private String color_of_threshold_line_onchart;

    // Plain FK string (not an @ManyToOne): daintree_device_id stored directly as column
    private String daintree_device_id;

    // --- Scalar FK fields (loose coupling: all cross-module @ManyToOne replaced by plain *_id columns) ---

    // Replaces @ManyToOne Bacnet_Object (composite FK)
    private String bacnet_object_bacnet_device_id;
    private String bacnet_object_id;

    // Replaces @ManyToOne Lorawan_Sensor_Attributes (composite FK)
    private String lorawan_sensor_attributes_lorawan_sensor_id;
    private String lorawan_sensor_attributes_name;

    // Replaces @ManyToOne Snmp_Device
    private String snmp_device_id;

    // Replaces @ManyToOne DisruptiveSensor
    private String disruptive_sensor_id;

    // Replaces @ManyToOne MyDevicesSensorAttributes (composite FK)
    private String my_devices_sensor_attributes_my_devices_sensor_id;
    private String my_devices_sensor_attributes_name;

    // Replaces @ManyToOne Monnit_Sensor
    private String monnit_sensor_id;

    // Replaces @ManyToOne PelicanSensorAttributes (composite FK)
    private String pelican_sensor_attributes_pelican_sensor_id;
    private String pelican_sensor_attributes_name;

    // Replaces @ManyToOne KNXGroup (composite FK)
    private String knx_group_address;
    private String knx_group_knx_device_address;

    // Replaces @ManyToOne SnmpObject (composite FK)
    private String snmp_object_snmp_device_configuration_id;
    private String snmp_object_oid;

    // Replaces @ManyToOne MeasuringInstrument
    private String measuring_instrument_id;

    // Replaces @ManyToOne DaintreePoint
    private String daintree_point_id;

    // Replaces @ManyToOne AlertProfile (AlertProfile exists in this service but prefer scalar per loose-coupling rule)
    private String alert_profile_id;

    // Replaces @ManyToOne EcobeeSensorAttributes (composite FK)
    private String ecobee_sensor_attributes_ecobee_sensor_id;
    private String ecobee_sensor_attributes_name;

    // Replaces @ManyToOne ModbusRegister
    private String modbus_register_id;

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getSecond_value() { return second_value; }
    public void setSecond_value(String second_value) { this.second_value = second_value; }

    public String getAlert_message() { return alert_message; }
    public void setAlert_message(String alert_message) { this.alert_message = alert_message; }

    public String getStart_time() { return start_time; }
    public void setStart_time(String start_time) { this.start_time = start_time; }

    public String getEnd_time() { return end_time; }
    public void setEnd_time(String end_time) { this.end_time = end_time; }

    public String getAlert_condition() { return alert_condition; }
    public void setAlert_condition(String alert_condition) { this.alert_condition = alert_condition; }

    public Boolean getAlert() { return alert; }
    public void setAlert(Boolean alert) { this.alert = alert; }

    public Boolean getShow_alert() { return show_alert; }
    public void setShow_alert(Boolean show_alert) { this.show_alert = show_alert; }

    public Boolean getShow_alert_message_as_value() { return show_alert_message_as_value; }
    public void setShow_alert_message_as_value(Boolean show_alert_message_as_value) { this.show_alert_message_as_value = show_alert_message_as_value; }

    public Integer getSchedule() { return schedule; }
    public void setSchedule(Integer schedule) { this.schedule = schedule; }

    public String getSchedule_conditions() { return schedule_conditions; }
    public void setSchedule_conditions(String schedule_conditions) { this.schedule_conditions = schedule_conditions; }

    public Integer getMax_alert_count() { return max_alert_count; }
    public void setMax_alert_count(Integer max_alert_count) { this.max_alert_count = max_alert_count; }

    public Integer getAlert_count() { return alert_count; }
    public void setAlert_count(Integer alert_count) { this.alert_count = alert_count; }

    public Integer getAlert_count_enabled() { return alert_count_enabled; }
    public void setAlert_count_enabled(Integer alert_count_enabled) { this.alert_count_enabled = alert_count_enabled; }

    public BigInteger getLast_alerted_timestamp() { return last_alerted_timestamp; }
    public void setLast_alerted_timestamp(BigInteger last_alerted_timestamp) { this.last_alerted_timestamp = last_alerted_timestamp; }

    public Integer getAlert_time() { return alert_time; }
    public void setAlert_time(Integer alert_time) { this.alert_time = alert_time; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Boolean getLast_alerted() { return last_alerted; }
    public void setLast_alerted(Boolean last_alerted) { this.last_alerted = last_alerted; }

    public Integer getAlert_count_time() { return alert_count_time; }
    public void setAlert_count_time(Integer alert_count_time) { this.alert_count_time = alert_count_time; }

    public Integer getEnable_threshold_line_onchart() { return enable_threshold_line_onchart; }
    public void setEnable_threshold_line_onchart(Integer enable_threshold_line_onchart) { this.enable_threshold_line_onchart = enable_threshold_line_onchart; }

    public String getColor_of_threshold_line_onchart() { return color_of_threshold_line_onchart; }
    public void setColor_of_threshold_line_onchart(String color_of_threshold_line_onchart) { this.color_of_threshold_line_onchart = color_of_threshold_line_onchart; }

    public String getDaintree_device_id() { return daintree_device_id; }
    public void setDaintree_device_id(String daintree_device_id) { this.daintree_device_id = daintree_device_id; }

    public String getBacnet_object_bacnet_device_id() { return bacnet_object_bacnet_device_id; }
    public void setBacnet_object_bacnet_device_id(String bacnet_object_bacnet_device_id) { this.bacnet_object_bacnet_device_id = bacnet_object_bacnet_device_id; }

    public String getBacnet_object_id() { return bacnet_object_id; }
    public void setBacnet_object_id(String bacnet_object_id) { this.bacnet_object_id = bacnet_object_id; }

    public String getLorawan_sensor_attributes_lorawan_sensor_id() { return lorawan_sensor_attributes_lorawan_sensor_id; }
    public void setLorawan_sensor_attributes_lorawan_sensor_id(String lorawan_sensor_attributes_lorawan_sensor_id) { this.lorawan_sensor_attributes_lorawan_sensor_id = lorawan_sensor_attributes_lorawan_sensor_id; }

    public String getLorawan_sensor_attributes_name() { return lorawan_sensor_attributes_name; }
    public void setLorawan_sensor_attributes_name(String lorawan_sensor_attributes_name) { this.lorawan_sensor_attributes_name = lorawan_sensor_attributes_name; }

    public String getSnmp_device_id() { return snmp_device_id; }
    public void setSnmp_device_id(String snmp_device_id) { this.snmp_device_id = snmp_device_id; }

    public String getDisruptive_sensor_id() { return disruptive_sensor_id; }
    public void setDisruptive_sensor_id(String disruptive_sensor_id) { this.disruptive_sensor_id = disruptive_sensor_id; }

    public String getMy_devices_sensor_attributes_my_devices_sensor_id() { return my_devices_sensor_attributes_my_devices_sensor_id; }
    public void setMy_devices_sensor_attributes_my_devices_sensor_id(String my_devices_sensor_attributes_my_devices_sensor_id) { this.my_devices_sensor_attributes_my_devices_sensor_id = my_devices_sensor_attributes_my_devices_sensor_id; }

    public String getMy_devices_sensor_attributes_name() { return my_devices_sensor_attributes_name; }
    public void setMy_devices_sensor_attributes_name(String my_devices_sensor_attributes_name) { this.my_devices_sensor_attributes_name = my_devices_sensor_attributes_name; }

    public String getMonnit_sensor_id() { return monnit_sensor_id; }
    public void setMonnit_sensor_id(String monnit_sensor_id) { this.monnit_sensor_id = monnit_sensor_id; }

    public String getPelican_sensor_attributes_pelican_sensor_id() { return pelican_sensor_attributes_pelican_sensor_id; }
    public void setPelican_sensor_attributes_pelican_sensor_id(String pelican_sensor_attributes_pelican_sensor_id) { this.pelican_sensor_attributes_pelican_sensor_id = pelican_sensor_attributes_pelican_sensor_id; }

    public String getPelican_sensor_attributes_name() { return pelican_sensor_attributes_name; }
    public void setPelican_sensor_attributes_name(String pelican_sensor_attributes_name) { this.pelican_sensor_attributes_name = pelican_sensor_attributes_name; }

    public String getKnx_group_address() { return knx_group_address; }
    public void setKnx_group_address(String knx_group_address) { this.knx_group_address = knx_group_address; }

    public String getKnx_group_knx_device_address() { return knx_group_knx_device_address; }
    public void setKnx_group_knx_device_address(String knx_group_knx_device_address) { this.knx_group_knx_device_address = knx_group_knx_device_address; }

    public String getSnmp_object_snmp_device_configuration_id() { return snmp_object_snmp_device_configuration_id; }
    public void setSnmp_object_snmp_device_configuration_id(String snmp_object_snmp_device_configuration_id) { this.snmp_object_snmp_device_configuration_id = snmp_object_snmp_device_configuration_id; }

    public String getSnmp_object_oid() { return snmp_object_oid; }
    public void setSnmp_object_oid(String snmp_object_oid) { this.snmp_object_oid = snmp_object_oid; }

    public String getMeasuring_instrument_id() { return measuring_instrument_id; }
    public void setMeasuring_instrument_id(String measuring_instrument_id) { this.measuring_instrument_id = measuring_instrument_id; }

    public String getDaintree_point_id() { return daintree_point_id; }
    public void setDaintree_point_id(String daintree_point_id) { this.daintree_point_id = daintree_point_id; }

    public String getAlert_profile_id() { return alert_profile_id; }
    public void setAlert_profile_id(String alert_profile_id) { this.alert_profile_id = alert_profile_id; }

    public String getEcobee_sensor_attributes_ecobee_sensor_id() { return ecobee_sensor_attributes_ecobee_sensor_id; }
    public void setEcobee_sensor_attributes_ecobee_sensor_id(String ecobee_sensor_attributes_ecobee_sensor_id) { this.ecobee_sensor_attributes_ecobee_sensor_id = ecobee_sensor_attributes_ecobee_sensor_id; }

    public String getEcobee_sensor_attributes_name() { return ecobee_sensor_attributes_name; }
    public void setEcobee_sensor_attributes_name(String ecobee_sensor_attributes_name) { this.ecobee_sensor_attributes_name = ecobee_sensor_attributes_name; }

    public String getModbus_register_id() { return modbus_register_id; }
    public void setModbus_register_id(String modbus_register_id) { this.modbus_register_id = modbus_register_id; }
}
