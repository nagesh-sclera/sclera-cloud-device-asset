package io.sclera.vdms.dto;

import java.math.BigInteger;

public class VdmsDTO {
    private String id;
    private String property_name;
    private String activation_status;
    private Integer status;
    private String location;
    private String timezone;
    private Boolean isConfigured;
    private BigInteger activation_timestamp;
    private String deployment_type;
    private String address;
    private String city;
    private String country;
    private String state;
    private Integer zip;
    private String image_url;
    private String latitude;
    private String longitude;
    private String region;
    private String customer_org_id;
    private String adc_configuration_id;
    private Integer is_master;
    private Integer has_secondary_device;
    private String secondary_device_id;
    private String master_ip;
    private String slave_ip;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProperty_name() { return property_name; }
    public void setProperty_name(String v) { this.property_name = v; }
    public String getActivation_status() { return activation_status; }
    public void setActivation_status(String v) { this.activation_status = v; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer v) { this.status = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String v) { this.timezone = v; }
    public Boolean getIsConfigured() { return isConfigured; }
    public void setIsConfigured(Boolean v) { this.isConfigured = v; }
    public BigInteger getActivation_timestamp() { return activation_timestamp; }
    public void setActivation_timestamp(BigInteger v) { this.activation_timestamp = v; }
    public String getDeployment_type() { return deployment_type; }
    public void setDeployment_type(String v) { this.deployment_type = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getCity() { return city; }
    public void setCity(String v) { this.city = v; }
    public String getCountry() { return country; }
    public void setCountry(String v) { this.country = v; }
    public String getState() { return state; }
    public void setState(String v) { this.state = v; }
    public Integer getZip() { return zip; }
    public void setZip(Integer v) { this.zip = v; }
    public String getImage_url() { return image_url; }
    public void setImage_url(String v) { this.image_url = v; }
    public String getLatitude() { return latitude; }
    public void setLatitude(String v) { this.latitude = v; }
    public String getLongitude() { return longitude; }
    public void setLongitude(String v) { this.longitude = v; }
    public String getRegion() { return region; }
    public void setRegion(String v) { this.region = v; }
    public String getCustomer_org_id() { return customer_org_id; }
    public void setCustomer_org_id(String v) { this.customer_org_id = v; }
    public String getAdc_configuration_id() { return adc_configuration_id; }
    public void setAdc_configuration_id(String v) { this.adc_configuration_id = v; }
    public Integer getIs_master() { return is_master; }
    public void setIs_master(Integer v) { this.is_master = v; }
    public Integer getHas_secondary_device() { return has_secondary_device; }
    public void setHas_secondary_device(Integer v) { this.has_secondary_device = v; }
    public String getSecondary_device_id() { return secondary_device_id; }
    public void setSecondary_device_id(String v) { this.secondary_device_id = v; }
    public String getMaster_ip() { return master_ip; }
    public void setMaster_ip(String v) { this.master_ip = v; }
    public String getSlave_ip() { return slave_ip; }
    public void setSlave_ip(String v) { this.slave_ip = v; }
}
