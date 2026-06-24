package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.*;

import java.math.BigInteger;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VdmsDTO {

    public String vdms_id;
    private String property_name;
    private Boolean is_block;
    private String activation_status;
    private BigInteger creation_timestamp;
    private BigInteger last_seen;
    private String private_ip;
    private String status;
    private String vdms_status;
    private BigInteger block_timestamp;
    private String customer_org_id;
    private String vendor_org_id;
    private String address;
    private String city;
    private String country;
    private String state;
    private String zip;
    private Integer network_count;
    private String end_date;
    private String plan;
    private Integer progress;
    private String visibility_id;
    private String profile_id;
    private String vdms_profile_id;
    private String profile_name;
    private String primary_proxy_profile_id;
    private String proxy_profile_name;
    private String base64image;
    private String extension;
    private String image_url;
    private String devuid;
    private String latitude;
    private String longitude;
    private ProxyProfileDTO proxy_profile;
    private Set<DockerDTO> dockers;
    private Set<String> network_names;
    //vdms id from touchscreen
    private String id;
    private String timezone;
    // master-slave fields
    private Integer is_master;
    private String secondary_device_id;
    //vdms time duration
    private BigInteger first_seen;
    private BigInteger timestamp;

    private BigInteger activation_timestamp;
    private String deployment_type;
    private String password;
    private String coordinates;
    private Integer email_alert;

    private Boolean trialStatus = false;
    private BigInteger trialStartDate = BigInteger.ZERO;
    private BigInteger trialEndDate = BigInteger.ZERO;
    private Long trialDaysRemaining = 0L;
    private String permissions;
    private Boolean isGenerated;
    private Boolean isAgentSubscription;
    private String region;
    private String assetCount;
    private Integer trialActivationStatus = 1;
    private String corrigoConfigId;
    private Integer isMultiTenant;
    private String awsRegion;

    public VdmsDTO(String vdms_id, String property_name, String activation_status, Boolean is_block,
                   BigInteger creation_timestamp, BigInteger last_seen, String status,
                   BigInteger block_timestamp, String customer_org_id, String image_url, String address, String city, String country,
                   String state, String zip, String end_date, String plan, Integer progress, String primary_proxy_profile_id,
                   String longitude, String latitude, BigInteger first_seen, String deployment_type, BigInteger activation_timestamp, String region, String awsRegion) {
        super();
        this.vdms_id = vdms_id;
        this.property_name = property_name;
        this.activation_status = activation_status;
        this.is_block = is_block;
        this.creation_timestamp = creation_timestamp;
        this.last_seen = last_seen;
        this.status = status;
        this.block_timestamp = block_timestamp;
        this.customer_org_id = customer_org_id;
        this.image_url = image_url;
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.zip = zip;
        this.end_date = end_date;
        this.plan = plan;
        this.progress = progress;
        this.primary_proxy_profile_id = primary_proxy_profile_id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.first_seen = first_seen;
        this.deployment_type = deployment_type;
        this.activation_timestamp = activation_timestamp;
        this.region = region;
        this.awsRegion = awsRegion;
    }

    public VdmsDTO(String vdms_id, String property_name, String image_url) {
        super();
        this.vdms_id = vdms_id;
        this.property_name = property_name;
        this.image_url = image_url;
    }

    public VdmsDTO(String vdms_id, String property_name, String customer_org_id, String vendor_org_id,
                   String profile_id, String vdms_profile_id, String profile_name) {
        super();
        this.vdms_id = vdms_id;
        this.property_name = property_name;
        this.customer_org_id = customer_org_id;
        this.vendor_org_id = vendor_org_id;
        this.profile_id = profile_id;
        this.vdms_profile_id = vdms_profile_id;
        this.profile_name = profile_name;
    }

    public VdmsDTO(String vdms_id, String property_name, String primary_proxy_profile_id, String proxy_profile_name, BigInteger lastSeen, BigInteger first_seen) {
        this.vdms_id = vdms_id;
        this.property_name = property_name;
        this.primary_proxy_profile_id = primary_proxy_profile_id;
        this.proxy_profile_name = proxy_profile_name;
        this.last_seen = lastSeen;
        this.first_seen = first_seen;
    }

    public VdmsDTO(String vdms_id, BigInteger last_seen, String property_name) {
        this.vdms_id = vdms_id;
        this.last_seen = last_seen;
        this.property_name = property_name;
    }

    public VdmsDTO(String vdms_id, BigInteger last_seen) {
        this.vdms_id = vdms_id;
        this.last_seen = last_seen;
    }

    public VdmsDTO(String vdms_id, String property_name, Integer email_alert, BigInteger last_seen) {
        this.vdms_id = vdms_id;
        this.property_name = property_name;
        this.email_alert = email_alert;
        this.last_seen = last_seen;
    }

    public VdmsDTO(String vdms_id, String property_name, String activation_status, Boolean is_block,
                   BigInteger creation_timestamp, BigInteger last_seen, String status,
                   BigInteger block_timestamp, String customer_org_id, String image_url, String address, String city, String country,
                   String state, String zip, String end_date, String plan, Integer progress, String primary_proxy_profile_id,
                   String longitude, String latitude, BigInteger first_seen, String deployment_type, BigInteger activation_timestamp,
                   Boolean trialStatus, BigInteger trialStartDate, BigInteger trialEndDate, String assetCount, String region, String awsRegion, Integer isMultiTenant) {
        super();
        this.vdms_id = vdms_id;
        this.property_name = property_name;
        this.activation_status = activation_status;
        this.is_block = is_block;
        this.creation_timestamp = creation_timestamp;
        this.last_seen = last_seen;
        this.status = status;
        this.block_timestamp = block_timestamp;
        this.customer_org_id = customer_org_id;
        this.image_url = image_url;
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.zip = zip;
        this.end_date = end_date;
        this.plan = plan;
        this.progress = progress;
        this.primary_proxy_profile_id = primary_proxy_profile_id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.first_seen = first_seen;
        this.deployment_type = deployment_type;
        this.activation_timestamp = activation_timestamp;
        this.trialStatus = trialStatus;
        this.trialStartDate = trialStartDate;
        this.trialEndDate = trialEndDate;
        this.assetCount = assetCount;
        this.region = region;
        this.awsRegion = awsRegion;
        this.isMultiTenant = isMultiTenant;
    }
}
