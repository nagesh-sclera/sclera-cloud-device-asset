package io.sclera.integrations.model;

import java.math.BigInteger;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
@Entity
//to be removed after pagination api works
//Added pagination for ListAllBacnetDevices
//new query
// testing to be removed
public class Bacnet_Device {

    @Id
    private String id;

    @Column(length = 64)
    private String ip_address;

    @Column(length = 128)
    private String name;

    private String model;

    private String vendor;

    @Column(length = 64)
    private Integer vendor_id;

    @Column(length = 64)
    private Integer max_Apdu;

    private Integer segmentation_support;

    private Boolean subscribe_cov;

    private Integer network_no;

    private String mac_address;

    @Column(columnDefinition = "integer default 1", length = 1)
    private Integer bacnet_type;

    @Column(length = 64)
    private String network_router;

    @Column(length = 64)
    private Boolean write_property;

    @Column(columnDefinition = "integer default 0", length = 8)
    private Integer is_added;

    @Column(name = "object_list", columnDefinition = "text")
    private String object_list;

    @Column
    private Long sync_frequency;

    @Column
    private BigInteger last_sync;

    private Integer connectivity_status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bacnet_device")
    private Set<Bacnet_Object> bacnet_object;


    // PG-port: scalar FK (Docker entity lives in cloud-device-asset).
    @Column(name = "docker_name")
    private String docker_name;

    @Column(name = "docker_vdms_id")
    private String docker_vdms_id;


    public String getId() {
        return id;
    }


    public String getIp_address() {
        return ip_address;
    }


    public String getName() {
        return name;
    }


    public String getModel() {
        return model;
    }


    public Integer getMax_Apdu() {
        return max_Apdu;
    }


    public Integer getSegmentation_support() {
        return segmentation_support;
    }


    public Boolean getSubscribe_cov() {
        return subscribe_cov;
    }


    public Set<Bacnet_Object> getBacnet_object() {
        return bacnet_object;
    }


    public String getDocker_name() {
        return docker_name;
    }

    public String getDocker_vdms_id() {
        return docker_vdms_id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }


    public void setName(String name) {
        this.name = name;
    }


    public void setModel(String model) {
        this.model = model;
    }


    public void setMax_Apdu(Integer max_Apdu) {
        this.max_Apdu = max_Apdu;
    }


    public void setSegmentation_support(Integer segmentation_support) {
        this.segmentation_support = segmentation_support;
    }


    public void setSubscribe_cov(Boolean subscribe_cov) {
        this.subscribe_cov = subscribe_cov;
    }


    public void setBacnet_object(Set<Bacnet_Object> bacnet_object) {
        this.bacnet_object = bacnet_object;
    }


    public void setDocker_name(String docker_name) {
        this.docker_name = docker_name;
    }

    public void setDocker_vdms_id(String docker_vdms_id) {
        this.docker_vdms_id = docker_vdms_id;
    }


    public String getVendor() {
        return vendor;
    }


    public void setVendor(String vendor) {
        this.vendor = vendor;
    }


    public Integer getVendor_id() {
        return vendor_id;
    }


    public void setVendor_id(Integer vendor_id) {
        this.vendor_id = vendor_id;
    }


    public Bacnet_Device() {
        super();
    }


    public Bacnet_Device(String id) {
        super();
        this.id = id;
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

    public Integer getIs_added() {
        return is_added;
    }

    public void setIs_added(Integer is_added) {
        this.is_added = is_added;
    }

    public String getObject_list() {
        return object_list;
    }

    public void setObject_list(String object_list) {
        this.object_list = object_list;
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

}
