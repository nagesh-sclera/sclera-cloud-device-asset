package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String id;
    private String email;
    private String password;
    private Integer block;
    private Integer disable;
    private String company_name;
    private Integer is_enterprise = 1;
    private BigInteger creation_timestamp;
    private String name;
    private String phone;
    private String phone_type;
    private String value;
    private String website;
    private String organisation_id;
    private String customer_id;
    private String roleId;
    private String role;
    private String address;
    private String city;
    private String country;
    private String state;
    private String zip;
    private String created_by;
    private Integer status_code;
    private String vdms_id;
    private String activation_status;
    private BigInteger last_updated_time;
    private Integer active;
    private String base64image;
    private String image_url;
    private String extension;
    private Integer isMFAEnabled;
    private Integer mfaRequiredAction;
    private String twoFAType;
    private String user_profile_id;
    private Integer tempBlock;
    private String tempBlockTime;
    private Boolean emailMute;
    private Boolean smsMute;
    private Set<VdmsDTO> visible_vdms;
    private String provider_name;
    private String providerId;

    private String timeZone;
    private String user_status;
    private String app_url;
    private String device_info_url;
    private String device_map_url;
    private String emailVerificationToken;
    private String user_profile_name;
    private String recaptchaToken;
    private String language = "EN";
    private String lastlogintime;
    private Integer fullAccess;
    private List<String> devUIds;
    private List<VdmsAccessVisibilityDTO> vdmsAccessVisibility;
    private String theme_mode;
    private String theme_color;
    private String provider_label;

    private Integer terms_and_conditions;

    private UserProfileDTO userProfileDTO;
    private String application_language;

    private List<String> vdmsIds;
    private Integer inventory_visibility;

    private List<String> profileIds;
    List<UserProfileDTO> userProfiles;

    private CorrigoUserSettingsDTO corrigoUserSettings;
    private String sensor_info_url;
    private String sensor_map_url;
    List<IpAclDTO> ipAclDetails;
    private String ticket_info_url;
    private Integer isUserRemoteDesktop = 0;

    public UserDTO(String email, String company_name, Integer is_enterprise, BigInteger creation_timestamp, String name, String phone,
                   String phone_type, String value, String website, String organisation_id, String role, String image_url, String address,
                   String city, String country, String state, String zip, String time_zone) {
        super();
        this.email = email;
        this.company_name = company_name;
        this.is_enterprise = is_enterprise;
        this.creation_timestamp = creation_timestamp;
        this.name = name;
        this.phone = phone;
        this.phone_type = phone_type;
        this.value = value;
        this.website = website;
        this.organisation_id = organisation_id;
        this.role = role;
        this.image_url = image_url;
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.zip = zip;
        this.timeZone = time_zone;
    }

    public UserDTO(String email, String company_name, Integer is_enterprise, BigInteger creation_timestamp, String name, String phone,
                   String phone_type, String value, String website, String organisation_id, String role, String image_url, String address,
                   String city, String country, String state, String zip, String activation_status,
                   BigInteger last_updated_time) {
        super();
        this.email = email;
        this.company_name = company_name;
        this.is_enterprise = is_enterprise;
        this.creation_timestamp = creation_timestamp;
        this.name = name;
        this.phone = phone;
        this.phone_type = phone_type;
        this.value = value;
        this.website = website;
        this.organisation_id = organisation_id;
        this.role = role;
        this.image_url = image_url;
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.zip = zip;
        this.activation_status = activation_status;
        this.last_updated_time = last_updated_time;
    }

    public UserDTO(String email, String organisation_id, String role, String image_url) {
        this.email = email;
        this.organisation_id = organisation_id;
        this.role = role;
        this.image_url = image_url;
    }

    public UserDTO(String email, String role, String organisation_id, String user_profile_id, String user_profile_name) {
        this.email = email;
        this.role = role;
        this.organisation_id = organisation_id;
        this.user_profile_id = user_profile_id;
        this.user_profile_name = user_profile_name;
    }

    public UserDTO(String email, String role, String organisation_id, String user_profile_id, String user_profile_name, String image_url) {
        this.email = email;
        this.role = role;
        this.organisation_id = organisation_id;
        this.user_profile_id = user_profile_id;
        this.user_profile_name = user_profile_name;
        this.image_url = image_url;
    }

    public UserDTO(String email, String password, Integer block, Integer disable, String company_name, Integer is_enterprise,
                   BigInteger creation_timestamp, String name, String phone, String phone_type, String value, String website,
                   String organisation_id, String customer_id, String role, String address, String city, String country,
                   String state, String zip, String created_by, Integer status_code, String vdms_id, String activation_status,
                   BigInteger last_updated_time, Integer active, String base64image, String image_url, String extension,
                   Integer isMFAEnabled, String twoFAType, String user_profile_id, Integer tempBlock, String tempBlockTime,
                   Boolean emailMute, Boolean smsMute, Set<VdmsDTO> visible_vdms, String provider, String providerId, String timeZone,
                   String user_status) {
        this.email = email;
        this.password = password;
        this.block = block;
        this.disable = disable;
        this.company_name = company_name;
        this.is_enterprise = is_enterprise;
        this.creation_timestamp = creation_timestamp;
        this.name = name;
        this.phone = phone;
        this.phone_type = phone_type;
        this.value = value;
        this.website = website;
        this.organisation_id = organisation_id;
        this.customer_id = customer_id;
        this.role = role;
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.zip = zip;
        this.created_by = created_by;
        this.status_code = status_code;
        this.vdms_id = vdms_id;
        this.activation_status = activation_status;
        this.last_updated_time = last_updated_time;
        this.active = active;
        this.base64image = base64image;
        this.image_url = image_url;
        this.extension = extension;
        this.isMFAEnabled = isMFAEnabled;
        this.twoFAType = twoFAType;
        this.user_profile_id = user_profile_id;
        this.tempBlock = tempBlock;
        this.tempBlockTime = tempBlockTime;
        this.emailMute = emailMute;
        this.smsMute = smsMute;
        this.visible_vdms = visible_vdms;
        this.provider_name = provider_name;
        this.providerId = providerId;
        this.timeZone = timeZone;
        this.user_status = user_status;
    }

    public UserDTO(String email, String company_name, Integer is_enterprise, BigInteger creation_timestamp, String name, String phone,
                   String phone_type, String value, String website, String organisation_id, String role, String image_url, String address,
                   String city, String country, String state, String zip, String time_zone, String language) {
        super();
        this.email = email;
        this.company_name = company_name;
        this.is_enterprise = is_enterprise;
        this.creation_timestamp = creation_timestamp;
        this.name = name;
        this.phone = phone;
        this.phone_type = phone_type;
        this.value = value;
        this.website = website;
        this.organisation_id = organisation_id;
        this.role = role;
        this.image_url = image_url;
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.zip = zip;
        this.timeZone = time_zone;
        this.language = language;
    }

    public UserDTO(String email, String image_url) {
        this.email = email;
        this.image_url = image_url;
    }

    public UserDTO(String email, BigInteger creation_timestamp, String name, String phone,
                   String phone_type, String value, String website, String organisation_id, String role, String image_url, String address,
                   String city, String country, String state, String zip, String time_zone, String language) {
        super();
        this.email = email;
        this.creation_timestamp = creation_timestamp;
        this.name = name;
        this.phone = phone;
        this.phone_type = phone_type;
        this.value = value;
        this.website = website;
        this.organisation_id = organisation_id;
        this.role = role;
        this.image_url = image_url;
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.zip = zip;
        this.timeZone = time_zone;
        this.language = language;
    }

    public UserDTO(String email, BigInteger creation_timestamp) {
        this.email = email;
        this.creation_timestamp = creation_timestamp;
    }
}