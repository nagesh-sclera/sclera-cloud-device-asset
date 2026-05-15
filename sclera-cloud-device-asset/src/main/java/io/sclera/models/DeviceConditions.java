
package io.sclera.models;


import io.sclera.dto.DeviceConditionsDTO;

import javax.persistence.*;
import java.math.BigInteger;


@Entity
@SqlResultSetMapping(
        name = "deviceconditionsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceConditionsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "alert_condition", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "alert_profile_id", type = String.class),
                                @ColumnResult(name = "trigger_time", type = Integer.class),
                                @ColumnResult(name = "last_alerted_time", type = BigInteger.class),
                                @ColumnResult(name = "priority", type = String.class),
                                @ColumnResult(name = "start_time", type = String.class),
                                @ColumnResult(name = "end_time", type = String.class),
                                @ColumnResult(name = "schedule", type = Integer.class),
                                @ColumnResult(name = "schedule_conditions", type = String.class),
                                @ColumnResult(name = "max_alert_count", type = Integer.class),
                                @ColumnResult(name = "alert_count", type = Integer.class),
                                @ColumnResult(name = "alert_count_enabled", type = Integer.class),
                                @ColumnResult(name = "alert_count_time", type = Integer.class),
                                @ColumnResult(name = "last_alerted", type = Boolean.class),
                                @ColumnResult(name = "alert_message", type = String.class)


                        })
        })

@NamedNativeQuery(
        name = "DeviceConditions.getDeviceConditions",
        query = "SELECT dc.id , dc.alert_condition, dc.device_id, dc.alert_profile_id, dc.trigger_time, dc.last_alerted_time, dc.priority, dc.start_time, dc.end_time, dc.schedule, dc.schedule_conditions, dc.max_alert_count, dc.alert_count, dc.alert_count_enabled, dc.alert_count_time, dc.last_alerted, dc.alert_message FROM device_conditions dc"
                + " WHERE dc.device_id = ?1 AND dc.alert_condition != 'device_offline_ai_call_alert'  ",
        resultSetMapping = "deviceconditionsmapping")

@NamedNativeQuery(
        name = "DeviceConditions.getDeviceConditionsById",
        query = "SELECT dc.id , dc.alert_condition, dc.device_id, dc.alert_profile_id, dc.trigger_time, dc.last_alerted_time, dc.priority, dc.start_time, dc.end_time, dc.schedule, dc.schedule_conditions, dc.max_alert_count, dc.alert_count, dc.alert_count_enabled, dc.alert_count_time, dc.last_alerted, dc.alert_message FROM device_conditions dc"
                + " WHERE dc.id = ?1  AND dc.alert_condition != 'device_offline_ai_call_alert'",
        resultSetMapping = "deviceconditionsmapping")

@NamedNativeQuery(
        name = "DeviceConditions.getDeviceConditionsForAiCall",
        query = "SELECT dc.id , dc.alert_condition, dc.device_id, dc.alert_profile_id, dc.trigger_time, dc.last_alerted_time, dc.priority, dc.start_time, dc.end_time, dc.schedule, dc.schedule_conditions, dc.max_alert_count, dc.alert_count, dc.alert_count_enabled, dc.alert_count_time, dc.last_alerted, dc.alert_message FROM device_conditions dc"
                + " WHERE dc.device_id = ?1 AND dc.alert_condition = 'device_offline_ai_call_alert'  ",
        resultSetMapping = "deviceconditionsmapping")

@NamedNativeQuery(
        name = "DeviceConditions.getDeviceConditionsByIdForAiCall",
        query = "SELECT dc.id , dc.alert_condition, dc.device_id, dc.alert_profile_id, dc.trigger_time, dc.last_alerted_time, dc.priority, dc.start_time, dc.end_time, dc.schedule, dc.schedule_conditions, dc.max_alert_count, dc.alert_count, dc.alert_count_enabled, dc.alert_count_time, dc.last_alerted, dc.alert_message FROM device_conditions dc"
                + " WHERE dc.id = ?1  AND dc.alert_condition = 'device_offline_ai_call_alert'",
        resultSetMapping = "deviceconditionsmapping")

public class DeviceConditions {

    @Id
    private String id;

    @Column(length = 64)
    private String alert_condition;

    @ManyToOne
    private Device device;

    // removed: relation to Bucket-C entity AlertProfile (AP-C3)
    @Column(length = 128)
    private String alert_profile_id;

    private BigInteger last_alerted_time;


    private Integer trigger_time;

    @Column(length = 128)
    private String priority;

    @Column(length = 64)
    private String start_time;

    @Column(length = 64)
    private String end_time;

    @Column(length = 8, columnDefinition = "integer default 0")
    private Integer schedule;

    private String schedule_conditions;

    @Column(columnDefinition = "integer default 0")
    private Integer max_alert_count;

    @Column(columnDefinition = "integer default 0")
    private Integer alert_count;

    @Column(columnDefinition = "integer default 0")
    private Integer alert_count_enabled;

    @Column
    private Integer alert_count_time;

    @Column(columnDefinition = "boolean default false")
    private Boolean last_alerted;

    @Column
    private String alert_message;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlert_condition() {
        return alert_condition;
    }

    public void setAlert_condition(String alert_condition) {
        this.alert_condition = alert_condition;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getAlert_profile_id() {
        return alert_profile_id;
    }

    public void setAlert_profile_id(String alert_profile_id) {
        this.alert_profile_id = alert_profile_id;
    }

    public BigInteger getLast_alerted_time() {
        return last_alerted_time;
    }

    public void setLast_alerted_time(BigInteger last_alerted_time) {
        this.last_alerted_time = last_alerted_time;
    }

    public Integer getTrigger_time() {
        return trigger_time;
    }

    public void setTrigger_time(Integer trigger_time) {
        this.trigger_time = trigger_time;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public Integer getSchedule() {
        return schedule;
    }

    public void setSchedule(Integer schedule) {
        this.schedule = schedule;
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

    public Integer getAlert_count_enabled() {
        return alert_count_enabled;
    }

    public void setAlert_count_enabled(Integer alert_count_enabled) {
        this.alert_count_enabled = alert_count_enabled;
    }

    public Integer getAlert_count_time() {
        return alert_count_time;
    }

    public void setAlert_count_time(Integer alert_count_time) {
        this.alert_count_time = alert_count_time;
    }

    public Boolean getLast_alerted() {
        return last_alerted;
    }

    public void setLast_alerted(Boolean last_alerted) {
        this.last_alerted = last_alerted;
    }

    public String getAlert_message() {
        return alert_message;
    }

    public void setAlert_message(String alert_message) {
        this.alert_message = alert_message;
    }


    public String getSchedule_conditions() {
        return schedule_conditions;
    }

    public void setSchedule_conditions(String schedule_conditions) {
        this.schedule_conditions = schedule_conditions;
    }
}
