package io.sclera.dto.touchscreen.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.sclera.dto.CorrigoUserSettingsDTO;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private String email;
    private BigInteger creation_timestamp;
    private String name;
    private String phone;
    private String phone_type;
    private String value;
    private String created_by;
    private String role;
    private String company_name;
    private String website;
    private String address_id;
    private String organisation_id;
    private String status;
    private String id;
    private String image_url;
    private String app_url;
    private String device_info_url;
    private String device_map_url;

    private String language;

    private CorrigoUserSettingsDTO corrigoUserSettings;

    //sensor alert changes
    private String sensor_info_url;
    private String sensor_map_url;

    private String profile_id;

    private String ticket_info_url;

    public UserDTO() {
    }

    public UserDTO(String email, BigInteger creation_timestamp, String name, String phone, String phone_type, String value, String created_by, String company_name, String website, String address_id, String organisation_id, String id, String language) {
        this.email = email;
        this.creation_timestamp = creation_timestamp;
        this.name = name;
        this.phone = phone;
        this.phone_type = phone_type;
        this.value = value;
        this.created_by = created_by;
        this.company_name = company_name;
        this.website = website;
        this.address_id = address_id;
        this.organisation_id = organisation_id;
        this.id = id;
        this.language = language;
    }

    public UserDTO(String email, String name, String phone, String phone_type, String value, String created_by,
                   String company_name, String website, String address_id, String organisation_id, String image_url, BigInteger creation_timestamp, String language) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.phone_type = phone_type;
        this.value = value;
        this.created_by = created_by;
        this.company_name = company_name;
        this.website = website;
        this.address_id = address_id;
        this.organisation_id = organisation_id;
        this.image_url = image_url;
        this.creation_timestamp = creation_timestamp;
        this.language = language;
    }

    public UserDTO(String email, BigInteger creation_timestamp, String name, String phone, String phone_type, String value, String created_by, String role, String company_name, String website, String address_id, String organisation_id, String status, String id, String image_url, String app_url, String device_info_url, String device_map_url, String language, CorrigoUserSettingsDTO corrigoUserSettings) {
        this.email = email;
        this.creation_timestamp = creation_timestamp;
        this.name = name;
        this.phone = phone;
        this.phone_type = phone_type;
        this.value = value;
        this.created_by = created_by;
        this.role = role;
        this.company_name = company_name;
        this.website = website;
        this.address_id = address_id;
        this.organisation_id = organisation_id;
        this.status = status;
        this.id = id;
        this.image_url = image_url;
        this.app_url = app_url;
        this.device_info_url = device_info_url;
        this.device_map_url = device_map_url;
        this.language = language;
        this.corrigoUserSettings = corrigoUserSettings;
    }

    //get user data
    public UserDTO(String email, String name, String image_url) {
        this.email = email;
        this.name = name;
        this.image_url = image_url;
    }

    //profileusermapping
    public UserDTO(String email, BigInteger creation_timestamp, String name, String phone, String phone_type, String value, String created_by, String company_name,
                   String website, String address_id, String organisation_id, String id, String language, String profile_id) {
        this.email = email;
        this.creation_timestamp = creation_timestamp;
        this.name = name;
        this.phone = phone;
        this.phone_type = phone_type;
        this.value = value;
        this.created_by = created_by;
        this.company_name = company_name;
        this.website = website;
        this.address_id = address_id;
        this.organisation_id = organisation_id;
        this.id = id;
        this.language = language;
        this.profile_id = profile_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigInteger getCreation_timestamp() {
        return creation_timestamp;
    }

    public void setCreation_timestamp(BigInteger creation_timestamp) {
        this.creation_timestamp = creation_timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone_type() {
        return phone_type;
    }

    public void setPhone_type(String phone_type) {
        this.phone_type = phone_type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress_id() {
        return address_id;
    }

    public void setAddress_id(String address_id) {
        this.address_id = address_id;
    }

    public String getOrganisation_id() {
        return organisation_id;
    }

    public void setOrganisation_id(String organisation_id) {
        this.organisation_id = organisation_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getApp_url() {
        return app_url;
    }

    public void setApp_url(String app_url) {
        this.app_url = app_url;
    }

    public String getDevice_info_url() {
        return device_info_url;
    }

    public void setDevice_info_url(String device_info_url) {
        this.device_info_url = device_info_url;
    }

    public String getDevice_map_url() {
        return device_map_url;
    }

    public void setDevice_map_url(String device_map_url) {
        this.device_map_url = device_map_url;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public CorrigoUserSettingsDTO getCorrigoUserSettings() {
        return corrigoUserSettings;
    }

    public void setCorrigoUserSettings(CorrigoUserSettingsDTO corrigoUserSettings) {
        this.corrigoUserSettings = corrigoUserSettings;
    }

    public String getSensor_info_url() {
        return sensor_info_url;
    }

    public void setSensor_info_url(String sensor_info_url) {
        this.sensor_info_url = sensor_info_url;
    }

    public String getSensor_map_url() {
        return sensor_map_url;
    }

    public void setSensor_map_url(String sensor_map_url) {
        this.sensor_map_url = sensor_map_url;
    }

    public String getProfile_id() {
        return profile_id;
    }

    public void setProfile_id(String profile_id) {
        this.profile_id = profile_id;
    }

    public String getTicket_info_url() {
        return ticket_info_url;
    }

    public void setTicket_info_url(String ticket_info_url) {
        this.ticket_info_url = ticket_info_url;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "email='" + email + '\'' +
                ", creation_timestamp=" + creation_timestamp +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", phone_type='" + phone_type + '\'' +
                ", value='" + value + '\'' +
                ", created_by='" + created_by + '\'' +
                ", role='" + role + '\'' +
                ", company_name='" + company_name + '\'' +
                ", website='" + website + '\'' +
                ", address_id='" + address_id + '\'' +
                ", organisation_id='" + organisation_id + '\'' +
                ", status='" + status + '\'' +
                ", id='" + id + '\'' +
                ", image_url='" + image_url + '\'' +
                ", app_url='" + app_url + '\'' +
                ", device_info_url='" + device_info_url + '\'' +
                ", device_map_url='" + device_map_url + '\'' +
                ", language='" + language + '\'' +
                ", corrigoUserSettings=" + corrigoUserSettings +
                ", sensor_info_url='" + sensor_info_url + '\'' +
                ", sensor_map_url='" + sensor_map_url + '\'' +
                ", profile_id='" + profile_id + '\'' +
                ", ticket_info_url='" + ticket_info_url + '\'' +
                '}';
    }
}
