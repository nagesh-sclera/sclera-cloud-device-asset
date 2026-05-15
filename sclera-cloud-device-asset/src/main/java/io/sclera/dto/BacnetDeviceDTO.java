package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BacnetDeviceDTO {


    private String id;
    private String name;
    private String ip_address;
    private String model;
    private String vendor;
    private Integer vendor_id;
    private Boolean subscribe_cov;
    private Integer max_apdu;
    private Integer segmentation_support;
    private Set<BacnetObjectDTO> bacnet_object;
    private String mac_address;
    private Integer network_no;
    private Integer bacnet_type;
    private String docker_name;

    private String network_router;
    private Boolean write_property;
    private Integer error_code;

    private Integer is_added;

    private Long sync_frequency;
    private BigInteger last_sync;

    private Integer connectivity_status;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIp_address() {
        return ip_address;
    }

    public String getModel() {
        return model;
    }

    public Boolean getSubscribe_cov() {
        return subscribe_cov;
    }

    public Integer getMax_apdu() {
        return max_apdu;
    }

    public Integer getSegmentation_support() {
        return segmentation_support;
    }

    public Set<BacnetObjectDTO> getBacnet_object() {
        return bacnet_object;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setSubscribe_cov(Boolean subscribe_cov) {
        this.subscribe_cov = subscribe_cov;
    }

    public void setMax_apdu(Integer max_apdu) {
        this.max_apdu = max_apdu;
    }

    public void setSegmentation_support(Integer segmentation_support) {
        this.segmentation_support = segmentation_support;
    }

    public void setBacnet_object(Set<BacnetObjectDTO> bacnet_object) {
        this.bacnet_object = bacnet_object;
    }

    public Integer getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(Integer vendor_id) {
        this.vendor_id = vendor_id;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Integer getNetwork_no() {
        return network_no;
    }

    public void setNetwork_no(Integer network_no) {
        this.network_no = network_no;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public Integer getBacnet_type() {
        return bacnet_type;
    }

    public void setBacnet_type(Integer bacnet_type) {
        this.bacnet_type = bacnet_type;
    }

    public String getDocker_name() {
        return docker_name;
    }

    public void setDocker_name(String docker_name) {
        this.docker_name = docker_name;
    }

    public String getNetwork_router() {
        return network_router;
    }

    public void setNetwork_router(String network_router) {
        this.network_router = network_router;
    }

    public Boolean getWrite_property() {
        return write_property;
    }

    public void setWrite_property(Boolean write_property) {
        this.write_property = write_property;
    }

    public Integer getError_code() {
        return error_code;
    }

    public void setError_code(Integer error_code) {
        this.error_code = error_code;
    }

    public Integer getIs_added() {
        return is_added;
    }

    public void setIs_added(Integer is_added) {
        this.is_added = is_added;
    }

    public Long getSync_frequency() {
        return sync_frequency;
    }

    public void setSync_frequency(Long sync_frequency) {
        this.sync_frequency = sync_frequency;
    }

    public BigInteger getLast_sync() {
        return last_sync;
    }

    public void setLast_sync(BigInteger last_sync) {
        this.last_sync = last_sync;
    }

    public Integer getConnectivity_status() {
        return connectivity_status;
    }

    public void setConnectivity_status(Integer connectivity_status) {
        this.connectivity_status = connectivity_status;
    }

    public BacnetDeviceDTO() {
        super();
        // TODO Auto-generated constructor stub
    }

    public BacnetDeviceDTO(String id, String name, String ip_address, String model, String vendor, Integer vendor_id,
                           Boolean subscribe_cov, Integer max_apdu, Integer segmentation_support, String mac_address,
                           Integer network_no, Integer bacnet_type, String docker_name, String network_router,
                           Boolean write_property, Integer is_added, Long sync_frequency, Integer connectivity_status) {
        super();
        this.id = id;
        this.name = name;
        this.ip_address = ip_address;
        this.model = model;
        this.vendor = vendor;
        this.vendor_id = vendor_id;
        this.subscribe_cov = subscribe_cov;
        this.max_apdu = max_apdu;
        this.segmentation_support = segmentation_support;
        this.mac_address = mac_address;
        this.network_no = network_no;
        this.bacnet_type = bacnet_type;
        this.docker_name = docker_name;
        this.network_router = network_router;
        this.write_property = write_property;
        this.is_added = is_added;
        this.sync_frequency = sync_frequency;
        this.connectivity_status = connectivity_status;
    }

    public BacnetDeviceDTO(String id, String name, String ip_address, String model, String vendor, Integer vendor_id,
                           Boolean subscribe_cov, Integer max_apdu, Integer segmentation_support, Set<BacnetObjectDTO> bacnet_object,
                           String mac_address, Integer network_no, Integer bacnet_type, String docker_name, String network_router,
                           Boolean write_property, Integer error_code) {
        super();
        this.id = id;
        this.name = name;
        this.ip_address = ip_address;
        this.model = model;
        this.vendor = vendor;
        this.vendor_id = vendor_id;
        this.subscribe_cov = subscribe_cov;
        this.max_apdu = max_apdu;
        this.segmentation_support = segmentation_support;
        this.bacnet_object = bacnet_object;
        this.mac_address = mac_address;
        this.network_no = network_no;
        this.bacnet_type = bacnet_type;
        this.docker_name = docker_name;
        this.network_router = network_router;
        this.write_property = write_property;
        this.error_code = error_code;
    }

    @Override
    public String toString() {
        return "BacnetDeviceDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ip_address='" + ip_address + '\'' +
                ", model='" + model + '\'' +
                ", vendor='" + vendor + '\'' +
                ", vendor_id=" + vendor_id +
                ", subscribe_cov=" + subscribe_cov +
                ", max_apdu=" + max_apdu +
                ", segmentation_support=" + segmentation_support +
                ", bacnet_object=" + bacnet_object +
                ", mac_address='" + mac_address + '\'' +
                ", network_no=" + network_no +
                ", bacnet_type=" + bacnet_type +
                ", docker_name='" + docker_name + '\'' +
                ", network_router='" + network_router + '\'' +
                ", write_property=" + write_property +
                ", error_code=" + error_code +
                ", is_added=" + is_added +
                ", sync_frequency=" + sync_frequency +
                ", last_sync=" + last_sync +
                ", connectivity_status=" + connectivity_status +
                '}';
    }
}
