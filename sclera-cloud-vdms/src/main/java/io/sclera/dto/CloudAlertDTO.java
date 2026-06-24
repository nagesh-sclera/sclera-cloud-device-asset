package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CloudAlertDTO {

    private String vdms_address;
    //sensor alert timestamp and checklist created_timestamp
    private String timestamp;
    private String unit;
    private String value;
    private String image_url;
    private String category_image_url;
    //inspection start and due date
    private String start_date;
    private String due_date;
    private String completed_date;
    private String remarks_date;

    // itam ticket alert
    private String ticket_date;
    private String ticket_time;


    // guest users reactive services alert
    private String updated_timestamp;

}
