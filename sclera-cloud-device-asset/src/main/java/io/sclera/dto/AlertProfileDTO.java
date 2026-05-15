package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertProfileDTO {

    private String id;
    private String description;
    private Integer email_alert;
    private Integer sms_alert;
    private String profile_id;
    private String workorder_template_id;
    private String name;

    private Integer ioc;

    private WorkorderTemplateDTO workorder_template;
    private ProfileDTO profile;

    private Integer ioc_popup_notification;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEmail_alert() {
        return email_alert;
    }

    public void setEmail_alert(Integer email_alert) {
        this.email_alert = email_alert;
    }

    public Integer getSms_alert() {
        return sms_alert;
    }

    public void setSms_alert(Integer sms_alert) {
        this.sms_alert = sms_alert;
    }


    public String getProfile_id() {
        return profile_id;
    }

    public void setProfile_id(String profile_id) {
        this.profile_id = profile_id;
    }

    public String getWorkorder_template_id() {
        return workorder_template_id;
    }

    public void setWorkorder_template_id(String workorder_template_id) {
        this.workorder_template_id = workorder_template_id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkorderTemplateDTO getWorkorder_template() {
        return workorder_template;
    }

    public void setWorkorder_template(WorkorderTemplateDTO workorder_template) {
        this.workorder_template = workorder_template;
    }

    public ProfileDTO getProfile() {
        return profile;
    }

    public void setProfile(ProfileDTO profile) {
        this.profile = profile;
    }

    public Integer getIoc() {
        return ioc;
    }

    public void setIoc(Integer ioc) {
        this.ioc = ioc;
    }

    public Integer getIoc_popup_notification() {return ioc_popup_notification;}

    public void setIoc_popup_notification(Integer ioc_popup_notification) {this.ioc_popup_notification = ioc_popup_notification;}


    public AlertProfileDTO() {
    }

    public AlertProfileDTO(String id, String description, Integer email_alert, Integer sms_alert, String profile_id, String workorder_template_id, String name,
                           Integer ioc, Integer ioc_popup_notification) {
        this.id = id;
        this.description = description;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.profile_id = profile_id;
        this.workorder_template_id = workorder_template_id;
        this.name = name;
        this.ioc = ioc;
        this.ioc_popup_notification = ioc_popup_notification;
    }

    @Override
    public String toString() {
        return "AlertProfileDTO{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", email_alert=" + email_alert +
                ", sms_alert=" + sms_alert +
                ", profile_id='" + profile_id + '\'' +
                ", workorder_template_id='" + workorder_template_id + '\'' +
                ", name='" + name + '\'' +
                ", workorder_template=" + workorder_template +
                ", profile=" + profile +
                '}';
    }
}
