package io.sclera.workorder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Filter criteria for ticket search. All fields optional; validates format/length only. */
public class TicketFilterDTO {

    private Set<String> status;

    @Size(max = 255)
    private String category;

    @Size(max = 255)
    private String device_id;

    @Email(message = "assignee_user_email must be a valid e-mail")
    @Size(max = 320)
    private String assignee_user_email;


    public Set<String> getStatus() {
        return status;
    }

    public void setStatus(Set<String> status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getAssignee_user_email() {
        return assignee_user_email;
    }

    public void setAssignee_user_email(String assignee_user_email) {
        this.assignee_user_email = assignee_user_email;
    }


}
