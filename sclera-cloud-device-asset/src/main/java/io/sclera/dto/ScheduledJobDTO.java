package io.sclera.dto;

public class ScheduledJobDTO {
    private String id;
    private String job_type;
    private Long time_in_seconds;
    private String condition_id;
    private String condition_type;
    private String condition_group;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getJob_type() { return job_type; }
    public void setJob_type(String job_type) { this.job_type = job_type; }
    public Long getTime_in_seconds() { return time_in_seconds; }
    public void setTime_in_seconds(Long time_in_seconds) { this.time_in_seconds = time_in_seconds; }
    public String getCondition_id() { return condition_id; }
    public void setCondition_id(String condition_id) { this.condition_id = condition_id; }
    public String getCondition_type() { return condition_type; }
    public void setCondition_type(String condition_type) { this.condition_type = condition_type; }
    public String getCondition_group() { return condition_group; }
    public void setCondition_group(String condition_group) { this.condition_group = condition_group; }
}
