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
public class TicketAlertDTO {
    private String id;
    private String name;
    private String assignee_user_email;
    private String assignee_name;
    private String number;
    private String status;
    private String description;
    private String template_type;
}
