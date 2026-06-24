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
public class SensorReportDetailsDTO {

    private String report_template_name;
    private String file;
    private String preSignUrl;
    private String description;
    private BigInteger from;
    private String number_of_sensors;
    private BigInteger to;
    private String category;
    private String template_type;
    private String report_template_id;
    private String report_condition_id;

    @Override
    public String toString() {
        return "SensorReportDetailsDTO{" +
                "report_template_name='" + report_template_name + '\'' +
                ", description='" + description + '\'' +
                ", from=" + from +
                ", number_of_sensors='" + number_of_sensors + '\'' +
                ", to=" + to +
                ", category='" + category + '\'' +
                ", template_type='" + template_type + '\'' +
                ", report_template_id='" + report_template_id + '\'' +
                ", report_condition_id='" + report_condition_id + '\'' +
                '}';
    }
}
