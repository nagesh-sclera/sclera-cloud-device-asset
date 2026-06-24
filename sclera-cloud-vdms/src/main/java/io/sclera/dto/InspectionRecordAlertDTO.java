package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigInteger;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InspectionRecordAlertDTO {

    private String name;
    private String assignee_email;
    private String description;
    private BigInteger start_date;
    private BigInteger due_date;
    private String code;
    private String template_type;
    private String type;
    private String status;
    private BigInteger completed_date;
    private String file;
    private String completed_by;
    private List<RecordChecklistAlertDTO> recordchecklist;
    private Integer recordchecklist_count;
    private  String completion_note;

    @Override
    public String toString() {
        return "InspectionRecordAlertDTO{" +
                "name='" + name + '\'' +
                ", assignee_email='" + assignee_email + '\'' +
                ", description='" + description + '\'' +
                ", start_date=" + start_date +
                ", due_date=" + due_date +
                ", code='" + code + '\'' +
                ", template_type='" + template_type + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", completed_date=" + completed_date +
                ", recordchecklist=" + recordchecklist +
                ", recordchecklist_count=" + recordchecklist_count +
                ", completion_note='" + completion_note + '\'' +
                '}';
    }
}
