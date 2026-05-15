package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConditionsDTO {

    private String id;

    private String name;

    private String value;

    private String second_value;

    private String alert_message;

    private String start_time;

    private String end_time;

    private String alert_condition;

    private Boolean alert;

    private Boolean show_alert;

    private Boolean show_alert_message_as_value;

    private String bacnet_device_id;

    private String bacnet_object_id;

    private String lorawan_sensor_id;

    private String lorawan_sensor_attributes_name;

    private String current_value;

    private String snmp_device_id;

    private String disruptive_sensor_id;

    private String my_devices_sensor_id;

    private String my_devices_sensor_attributes_name;

    private String monnit_sensor_id;

    private String pelican_sensor_id;

    private String pelican_sensor_attributes_name;

    private Integer schedule;

    private String schedule_conditions;

    private String knx_group_address;

    private String knx_device_address;

    private String snmp_device_configuration_id;

    private String snmp_object_oid;

    private Integer alert_count_enabled;

    private Integer max_alert_count;

    private Integer alert_count;

    private String measuring_instrument_id;

    private BigInteger last_alerted_timestamp;
    private Integer alert_time;

    private String device_id;

    private Set<String> device_ids;

    private String daintree_device_id;
    private String daintree_point_id;

    private String alert_profile_id;

    private AlertProfileDTO alert_profile;

    private String ecobee_sensor_id;
    private String ecobee_sensor_attributes_name;

    private String modbus_register_id;

    private String priority;

    private Boolean last_alerted;

    private Integer alert_count_time;

    private Integer enable_threshold_line_onchart;

    public Integer getEnable_threshold_line_onchart() {
        return enable_threshold_line_onchart;
    }

    public void setEnable_threshold_line_onchart(Integer enable_threshold_line_onchart) {
        this.enable_threshold_line_onchart = enable_threshold_line_onchart;
    }

    public String getColor_of_threshold_line_onchart() {
        return color_of_threshold_line_onchart;
    }

    public void setColor_of_threshold_line_onchart(String color_of_threshold_line_onchart) {
        this.color_of_threshold_line_onchart = color_of_threshold_line_onchart;
    }

    private String color_of_threshold_line_onchart;


    public Boolean getLast_alerted() {
        return last_alerted;
    }

    public void setLast_alerted(Boolean last_alerted) {
        this.last_alerted = last_alerted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAlert_message() {
        return alert_message;
    }

    public void setAlert_message(String alert_message) {
        this.alert_message = alert_message;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getAlert_condition() {
        return alert_condition;
    }

    public void setAlert_condition(String alert_condition) {
        this.alert_condition = alert_condition;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public Boolean getShow_alert() {
        return show_alert;
    }

    public void setShow_alert(Boolean show_alert) {
        this.show_alert = show_alert;
    }

    public Boolean getShow_alert_message_as_value() {
        return show_alert_message_as_value;
    }

    public void setShow_alert_message_as_value(Boolean show_alert_message_as_value) {
        this.show_alert_message_as_value = show_alert_message_as_value;
    }

    public String getBacnet_device_id() {
        return bacnet_device_id;
    }

    public void setBacnet_device_id(String bacnet_device_id) {
        this.bacnet_device_id = bacnet_device_id;
    }

    public String getBacnet_object_id() {
        return bacnet_object_id;
    }

    public void setBacnet_object_id(String bacnet_object_id) {
        this.bacnet_object_id = bacnet_object_id;
    }

    public String getLorawan_sensor_id() {
        return lorawan_sensor_id;
    }

    public void setLorawan_sensor_id(String lorawan_sensor_id) {
        this.lorawan_sensor_id = lorawan_sensor_id;
    }

    public String getLorawan_sensor_attributes_name() {
        return lorawan_sensor_attributes_name;
    }

    public void setLorawan_sensor_attributes_name(String lorawan_sensor_attributes_name) {
        this.lorawan_sensor_attributes_name = lorawan_sensor_attributes_name;
    }

    public String getCurrent_value() {
        return current_value;
    }

    public void setCurrent_value(String current_value) {
        this.current_value = current_value;
    }

    public String getSnmp_device_id() {
        return snmp_device_id;
    }

    public void setSnmp_device_id(String snmp_device_id) {
        this.snmp_device_id = snmp_device_id;
    }

    public String getDisruptive_sensor_id() {
        return disruptive_sensor_id;
    }

    public void setDisruptive_sensor_id(String disruptive_sensor_id) {
        this.disruptive_sensor_id = disruptive_sensor_id;
    }

    public String getMy_devices_sensor_id() {
        return my_devices_sensor_id;
    }

    public void setMy_devices_sensor_id(String my_devices_sensor_id) {
        this.my_devices_sensor_id = my_devices_sensor_id;
    }

    public String getMy_devices_sensor_attributes_name() {
        return my_devices_sensor_attributes_name;
    }

    public void setMy_devices_sensor_attributes_name(String my_devices_sensor_attributes_name) {
        this.my_devices_sensor_attributes_name = my_devices_sensor_attributes_name;
    }

    public String getMonnit_sensor_id() {
        return monnit_sensor_id;
    }

    public void setMonnit_sensor_id(String monnit_sensor_id) {
        this.monnit_sensor_id = monnit_sensor_id;
    }

    public String getPelican_sensor_id() {
        return pelican_sensor_id;
    }

    public void setPelican_sensor_id(String pelican_sensor_id) {
        this.pelican_sensor_id = pelican_sensor_id;
    }

    public String getPelican_sensor_attributes_name() {
        return pelican_sensor_attributes_name;
    }

    public void setPelican_sensor_attributes_name(String pelican_sensor_attributes_name) {
        this.pelican_sensor_attributes_name = pelican_sensor_attributes_name;
    }

    public String getKnx_group_address() {
        return knx_group_address;
    }

    public void setKnx_group_address(String knx_group_address) {
        this.knx_group_address = knx_group_address;
    }

    public String getKnx_device_address() {
        return knx_device_address;
    }

    public void setKnx_device_address(String knx_device_address) {
        this.knx_device_address = knx_device_address;
    }

    public Integer getSchedule() {
        return schedule;
    }

    public void setSchedule(Integer schedule) {
        this.schedule = schedule;
    }

    public String getSnmp_device_configuration_id() {
        return snmp_device_configuration_id;
    }

    public void setSnmp_device_configuration_id(String snmp_device_configuration_id) {
        this.snmp_device_configuration_id = snmp_device_configuration_id;
    }

    public String getSnmp_object_oid() {
        return snmp_object_oid;
    }

    public void setSnmp_object_oid(String snmp_object_oid) {
        this.snmp_object_oid = snmp_object_oid;
    }

    public Integer getAlert_count_enabled() {
        return alert_count_enabled;
    }

    public void setAlert_count_enabled(Integer alert_count_enabled) {
        this.alert_count_enabled = alert_count_enabled;
    }

    public Integer getMax_alert_count() {
        return max_alert_count;
    }

    public void setMax_alert_count(Integer max_alert_count) {
        this.max_alert_count = max_alert_count;
    }

    public Integer getAlert_count() {
        return alert_count;
    }

    public void setAlert_count(Integer alert_count) {
        this.alert_count = alert_count;
    }

    public String getMeasuring_instrument_id() {
        return measuring_instrument_id;
    }

    public void setMeasuring_instrument_id(String measuring_instrument_id) {
        this.measuring_instrument_id = measuring_instrument_id;
    }

    public BigInteger getLast_alerted_timestamp() {
        return last_alerted_timestamp;
    }

    public void setLast_alerted_timestamp(BigInteger last_alerted_timestamp) {
        this.last_alerted_timestamp = last_alerted_timestamp;
    }

    public Integer getAlert_time() {
        return alert_time;
    }

    public void setAlert_time(Integer alert_time) {
        this.alert_time = alert_time;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public Set<String> getDevice_ids() {
        return device_ids;
    }

    public void setDevice_ids(Set<String> device_ids) {
        this.device_ids = device_ids;
    }

    public String getDaintree_device_id() {
        return daintree_device_id;
    }

    public void setDaintree_device_id(String daintree_device_id) {
        this.daintree_device_id = daintree_device_id;
    }

    public String getDaintree_point_id() {
        return daintree_point_id;
    }

    public void setDaintree_point_id(String daintree_point_id) {
        this.daintree_point_id = daintree_point_id;
    }

    public String getAlert_profile_id() {
        return alert_profile_id;
    }

    public void setAlert_profile_id(String alert_profile_id) {
        this.alert_profile_id = alert_profile_id;
    }

    public AlertProfileDTO getAlert_profile() {
        return alert_profile;
    }

    public void setAlert_profile(AlertProfileDTO alert_profile) {
        this.alert_profile = alert_profile;
    }

    public String getEcobee_sensor_id() {
        return ecobee_sensor_id;
    }

    public void setEcobee_sensor_id(String ecobee_sensor_id) {
        this.ecobee_sensor_id = ecobee_sensor_id;
    }

    public String getEcobee_sensor_attributes_name() {
        return ecobee_sensor_attributes_name;
    }

    public void setEcobee_sensor_attributes_name(String ecobee_sensor_attributes_name) {
        this.ecobee_sensor_attributes_name = ecobee_sensor_attributes_name;
    }

    public String getModbus_register_id() {
        return modbus_register_id;
    }

    public void setModbus_register_id(String modbus_register_id) {
        this.modbus_register_id = modbus_register_id;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getAlert_count_time() {
        return alert_count_time;
    }

    public void setAlert_count_time(Integer alert_count_time) {
        this.alert_count_time = alert_count_time;
    }

    public String getSecond_value() {
        return second_value;
    }

    public void setSecond_value(String second_value) {
        this.second_value = second_value;
    }

    public String getSchedule_conditions() {
        return schedule_conditions;
    }

    public void setSchedule_conditions(String schedule_conditions) {
        this.schedule_conditions = schedule_conditions;
    }

    public ConditionsDTO() {
        super();
    }

    public ConditionsDTO(String id, String name, String value, String second_value, String alert_message, String start_time, String end_time,
                         Integer schedule, String schedule_conditions, Integer alert_count_enabled, Integer max_alert_count, Integer alert_count,
                         String alert_condition, Boolean alert, Boolean show_alert, Boolean show_alert_message_as_value,
                         String bacnet_device_id, String bacnet_object_id, String lorawan_sensor_id,
                         String lorawan_sensor_attributes_name, String snmp_device_id, String disruptive_sensor_id,
                         String my_devices_sensor_id, String my_devices_sensor_attributes_name, String monnit_sensor_id,
                         String pelican_sensor_id, String pelican_sensor_attributes_name, String knx_group_address, String knx_device_address,
                         String snmp_device_configuration_id, String snmp_object_oid, String measuring_instrument_id, BigInteger last_alerted_timestamp,
                         Integer alert_time, String daintree_device_id, String daintree_point_id, String alert_profile_id,
                         String ecobee_sensor_id, String ecobee_sensor_attributes_name, String modbus_register_id, String priority, Boolean last_alerted, Integer alert_count_time, Integer enable_threshold_line_onchart, String color_of_threshold_line_onchart) {
        super();
        this.id = id;
        this.name = name;
        this.value = value;
        this.second_value = second_value;
        this.alert_message = alert_message;
        this.start_time = start_time;
        this.end_time = end_time;
        this.schedule = schedule;
        this.schedule_conditions = schedule_conditions;
        this.alert_count_enabled = alert_count_enabled;
        this.max_alert_count = max_alert_count;
        this.alert_count = alert_count;
        this.alert_condition = alert_condition;
        this.alert = alert;
        this.show_alert = show_alert;
        this.show_alert_message_as_value = show_alert_message_as_value;
        this.bacnet_device_id = bacnet_device_id;
        this.bacnet_object_id = bacnet_object_id;
        this.lorawan_sensor_id = lorawan_sensor_id;
        this.lorawan_sensor_attributes_name = lorawan_sensor_attributes_name;
        this.snmp_device_id = snmp_device_id;
        this.disruptive_sensor_id = disruptive_sensor_id;
        this.my_devices_sensor_id = my_devices_sensor_id;
        this.my_devices_sensor_attributes_name = my_devices_sensor_attributes_name;
        this.monnit_sensor_id = monnit_sensor_id;
        this.pelican_sensor_id = pelican_sensor_id;
        this.pelican_sensor_attributes_name = pelican_sensor_attributes_name;
        this.knx_group_address = knx_group_address;
        this.knx_device_address = knx_device_address;
        this.snmp_device_configuration_id = snmp_device_configuration_id;
        this.snmp_object_oid = snmp_object_oid;
        this.measuring_instrument_id = measuring_instrument_id;
        this.last_alerted_timestamp = last_alerted_timestamp;
        this.alert_time = alert_time;
        this.daintree_device_id = daintree_device_id;
        this.daintree_point_id = daintree_point_id;
        this.alert_profile_id = alert_profile_id;
        this.ecobee_sensor_id = ecobee_sensor_id;
        this.ecobee_sensor_attributes_name = ecobee_sensor_attributes_name;
        this.modbus_register_id = modbus_register_id;
        this.priority = priority;
        this.last_alerted = last_alerted;
        this.alert_count_time = alert_count_time;
        this.enable_threshold_line_onchart = enable_threshold_line_onchart;
        this.color_of_threshold_line_onchart = color_of_threshold_line_onchart;
    }

    //Added for Share Conditions to Update the Sensor Id's and Current Value
    public ConditionsDTO(String id, String name, String value, String second_value, String alert_message, String start_time, String end_time,
                         Integer schedule, String schedule_conditions, Integer alert_count_enabled, Integer max_alert_count,
                         String alert_condition, Boolean alert, Boolean show_alert, Boolean show_alert_message_as_value, String current_value,
                         String bacnet_device_id, String bacnet_object_id, String lorawan_sensor_id,
                         String lorawan_sensor_attributes_name, String snmp_device_id, String disruptive_sensor_id,
                         String my_devices_sensor_id, String my_devices_sensor_attributes_name, String monnit_sensor_id,
                         String pelican_sensor_id, String pelican_sensor_attributes_name, String knx_group_address, String knx_device_address,
                         String snmp_device_configuration_id, String snmp_object_oid, String measuring_instrument_id, Integer alert_time,
                         String daintree_device_id, String daintree_point_id, String alert_profile_id,
                         String ecobee_sensor_id, String ecobee_sensor_attributes_name, String modbus_register_id, String priority, Integer alert_count_time,Integer enable_threshold_line_onchart, String color_of_threshold_line_onchart) {
        super();
        this.id = id;
        this.name = name;
        this.value = value;
        this.second_value = second_value;
        this.alert_message = alert_message;
        this.start_time = start_time;
        this.end_time = end_time;
        this.schedule = schedule;
        this.schedule_conditions = schedule_conditions;
        this.alert_count_enabled = alert_count_enabled;
        this.max_alert_count = max_alert_count;
        this.alert_condition = alert_condition;
        this.alert = alert;
        this.show_alert = show_alert;
        this.show_alert_message_as_value = show_alert_message_as_value;
        this.current_value = current_value;
        this.bacnet_device_id = bacnet_device_id;
        this.bacnet_object_id = bacnet_object_id;
        this.lorawan_sensor_id = lorawan_sensor_id;
        this.lorawan_sensor_attributes_name = lorawan_sensor_attributes_name;
        this.snmp_device_id = snmp_device_id;
        this.disruptive_sensor_id = disruptive_sensor_id;
        this.my_devices_sensor_id = my_devices_sensor_id;
        this.my_devices_sensor_attributes_name = my_devices_sensor_attributes_name;
        this.monnit_sensor_id = monnit_sensor_id;
        this.pelican_sensor_id = pelican_sensor_id;
        this.pelican_sensor_attributes_name = pelican_sensor_attributes_name;
        this.knx_group_address = knx_group_address;
        this.knx_device_address = knx_device_address;
        this.snmp_device_configuration_id = snmp_device_configuration_id;
        this.snmp_object_oid = snmp_object_oid;
        this.measuring_instrument_id = measuring_instrument_id;
        this.alert_time = alert_time;
        this.daintree_device_id = daintree_device_id;
        this.daintree_point_id = daintree_point_id;
        this.alert_profile_id = alert_profile_id;
        this.ecobee_sensor_id = ecobee_sensor_id;
        this.ecobee_sensor_attributes_name = ecobee_sensor_attributes_name;
        this.modbus_register_id = modbus_register_id;
        this.priority = priority;
        this.alert_count_time = alert_count_time;
        this.enable_threshold_line_onchart = enable_threshold_line_onchart;
        this.color_of_threshold_line_onchart = color_of_threshold_line_onchart;
    }


    //To get conditions count based details
    public ConditionsDTO(String id, Integer alert_count_enabled, Integer max_alert_count, Integer alert_count, Integer alert_time) {
        this.id = id;
        this.alert_count_enabled = alert_count_enabled;
        this.max_alert_count = max_alert_count;
        this.alert_count = alert_count;
        this.alert_time = alert_time;
    }

    //To get device alert messages
    public ConditionsDTO(String alert_message, String device_id) {
        super();
        this.alert_message = alert_message;
        this.device_id = device_id;
    }

    public ConditionsDTO(String id, Integer alert_count_enabled, Integer max_alert_count, Integer alert_count, Integer alert_time, Boolean last_alerted) {
        this.id = id;
        this.alert_count_enabled = alert_count_enabled;
        this.max_alert_count = max_alert_count;
        this.alert_count = alert_count;
        this.alert_time = alert_time;
        this.last_alerted = last_alerted;
    }

    public ConditionsDTO(String id, Integer alert_count_enabled, Integer max_alert_count, Integer alert_count, Integer alert_time,
                         Boolean last_alerted, String alert_condition, String value, String second_value, Boolean show_alert, Integer schedule, String schedule_conditions, String start_time, String end_time, Integer alert_count_time) {
        this.id = id;
        this.alert_count_enabled = alert_count_enabled;
        this.max_alert_count = max_alert_count;
        this.alert_count = alert_count;
        this.alert_time = alert_time;
        this.last_alerted = last_alerted;
        this.alert_condition = alert_condition;
        this.value = value;
        this.second_value = second_value;
        this.show_alert = show_alert;
        this.schedule = schedule;
        this.schedule_conditions = schedule_conditions;
        this.start_time = start_time;
        this.end_time = end_time;
        this.alert_count_time = alert_count_time;

    }

    @Override
    public String toString() {
        return "ConditionsDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", second_value='" + second_value + '\'' +
                ", alert_message='" + alert_message + '\'' +
                ", start_time='" + start_time + '\'' +
                ", end_time='" + end_time + '\'' +
                ", alert_condition='" + alert_condition + '\'' +
                ", alert=" + alert +
                ", show_alert=" + show_alert +
                ", show_alert_message_as_value=" + show_alert_message_as_value +
                ", bacnet_device_id='" + bacnet_device_id + '\'' +
                ", bacnet_object_id='" + bacnet_object_id + '\'' +
                ", lorawan_sensor_id='" + lorawan_sensor_id + '\'' +
                ", lorawan_sensor_attributes_name='" + lorawan_sensor_attributes_name + '\'' +
                ", current_value='" + current_value + '\'' +
                ", snmp_device_id='" + snmp_device_id + '\'' +
                ", disruptive_sensor_id='" + disruptive_sensor_id + '\'' +
                ", my_devices_sensor_id='" + my_devices_sensor_id + '\'' +
                ", my_devices_sensor_attributes_name='" + my_devices_sensor_attributes_name + '\'' +
                ", monnit_sensor_id='" + monnit_sensor_id + '\'' +
                ", pelican_sensor_id='" + pelican_sensor_id + '\'' +
                ", pelican_sensor_attributes_name='" + pelican_sensor_attributes_name + '\'' +
                ", schedule=" + schedule +
                ", schedule_conditions=" + schedule_conditions +
                ", knx_group_address='" + knx_group_address + '\'' +
                ", knx_device_address='" + knx_device_address + '\'' +
                ", snmp_device_configuration_id='" + snmp_device_configuration_id + '\'' +
                ", snmp_object_oid='" + snmp_object_oid + '\'' +
                ", alert_count_enabled=" + alert_count_enabled +
                ", max_alert_count=" + max_alert_count +
                ", alert_count=" + alert_count +
                ", measuring_instrument_id='" + measuring_instrument_id + '\'' +
                ", last_alerted_timestamp=" + last_alerted_timestamp +
                ", alert_time=" + alert_time +
                ", device_id='" + device_id + '\'' +
                ", device_ids=" + device_ids +
                ", daintree_device_id='" + daintree_device_id + '\'' +
                ", daintree_point_id='" + daintree_point_id + '\'' +
                ", alert_profile_id='" + alert_profile_id + '\'' +
                ", alert_profile=" + alert_profile +
                ", ecobee_sensor_id='" + ecobee_sensor_id + '\'' +
                ", ecobee_sensor_attributes_name='" + ecobee_sensor_attributes_name + '\'' +
                '}';
    }


}
