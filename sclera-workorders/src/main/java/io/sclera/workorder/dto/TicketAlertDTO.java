package io.sclera.workorder.dto;

/** Payload for a ticket alert/notification — assignee details plus ticket context and template type. */
public class TicketAlertDTO {

    private String id;
    private String name;
    private String assignee_user_email;
    private String assignee_name;
    private String number;
    private String status;
    private String description;
    private String template_type;


    public TicketAlertDTO() {
    }

    public TicketAlertDTO(String id, String name, String assinee_user_email, String assinee_name, String number, String status, String description, String template_type) {
        this.id = id;
        this.name = name;
        this.assignee_user_email = assinee_user_email;
        this.assignee_name = assinee_name;
        this.number = number;
        this.status = status;
        this.description = description;
        this.template_type = template_type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssignee_user_email() {
        return assignee_user_email;
    }

    public void setAssignee_user_email(String assignee_user_email) {
        this.assignee_user_email = assignee_user_email;
    }

    public String getAssignee_name() {
        return assignee_name;
    }

    public void setAssignee_name(String assignee_name) {
        this.assignee_name = assignee_name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplate_type() {
        return template_type;
    }

    public void setTemplate_type(String template_type) {
        this.template_type = template_type;
    }

    @Override
    public String toString() {
        return "TicketAlertDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", assinee_user_email='" + assignee_user_email + '\'' +
                ", assinee_name='" + assignee_name + '\'' +
                ", number='" + number + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", template_type='" + template_type + '\'' +
                '}';
    }
}
