
package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceConditionsDTO {


    private String id;
    private String alert_condition;
    private String device_id;
    private String alert_profile_id;
    private AlertProfileDTO alert_profile;
    private BigInteger last_alerted_time;
    private Integer trigger_time;

    private String priority;

    private String start_time;

    private String end_time;

    private Integer schedule;

    private String schedule_conditions;

    private Integer max_alert_count;

    private Integer alert_count;

    private Integer alert_count_enabled;

    private Integer alert_count_time;

    private Boolean last_alerted;

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

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
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

    public DeviceConditionsDTO() {
    }

    public DeviceConditionsDTO(String id, String alert_condition, String device_id, String alert_profile_id, Integer trigger_time, BigInteger last_alerted_time, String priority, String start_time, String end_time, Integer schedule, String schedule_conditions, Integer max_alert_count, Integer alert_count, Integer alert_count_enabled, Integer alert_count_time, Boolean last_alerted, String alert_message) {
        this.id = id;
        this.alert_condition = alert_condition;
        this.device_id = device_id;
        this.alert_profile_id = alert_profile_id;
        this.trigger_time = trigger_time;
        this.last_alerted_time = last_alerted_time;
        this.priority = priority;
        this.start_time = start_time;
        this.end_time = end_time;
        this.schedule = schedule;
        this.schedule_conditions = schedule_conditions;
        this.max_alert_count = max_alert_count;
        this.alert_count = alert_count;
        this.alert_count_enabled = alert_count_enabled;
        this.alert_count_time = alert_count_time;
        this.last_alerted = last_alerted;
        this.alert_message = alert_message;
    }

    public DeviceConditionsDTO(String alert_condition, String device_id, String alert_profile_id, Integer trigger_time, BigInteger last_alerted_time, String priority, String start_time, String end_time, Integer schedule, String schedule_conditions, Integer max_alert_count, Integer alert_count_time, Integer alert_count_enabled, String alert_message) {
        this.alert_condition = alert_condition;
        this.device_id = device_id;
        this.alert_profile_id = alert_profile_id;
        this.trigger_time = trigger_time;
        this.last_alerted_time = last_alerted_time;
        this.priority = priority;
        this.start_time = start_time;
        this.end_time = end_time;
        this.schedule = schedule;
        this.schedule_conditions = schedule_conditions;
        this.max_alert_count = max_alert_count;
        this.alert_count_time = alert_count_time;
        this.alert_count_enabled = alert_count_enabled;
        this.alert_message = alert_message;
    }


}
