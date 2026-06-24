package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordChecklistAlertDTO {

    private String name;
    private String description;
    private BigInteger created_timestamp;
    private String category;
    private String template_type;
    private String record_type;

    private String device_id;
    private String location_id;

    private String status;
    private String assignee_email;
    private LocationAlertDTO location;
    private DeviceAlertDTO device;
    private Integer count;
    private BigInteger due_date;
    private String task_type;
    private String completed_by;
    private BigInteger completed_timestamp;
    private String preSignUrl;
    private String file;
    private String remarks_by;
    private BigInteger remarks_timestamp;
    private String remarks;
    private String reference_number;
    private String contact_email;
    private BigInteger updated_timestamp;
}
