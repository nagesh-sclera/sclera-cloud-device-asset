package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorAlertDTO {

    private String primary_id;
    private String secondary_id;
    private String primary_name;
    private String secondary_name;
    private String category;
    private String value;
    private String type;
    private String unit;
    private String alert_message;
    private String device_id;
    private String protocol;
    private String priority;

}
