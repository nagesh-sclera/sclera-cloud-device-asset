package io.sclera.models;

import java.math.BigInteger;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.sclera.dto.BacnetDeviceDTO;
import io.sclera.dto.ConditionsDTO;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Bacnet_Device.class)


@SqlResultSetMapping(
        name = "bacnetdeviceinfomapping",
        classes = {
                @ConstructorResult(
                        targetClass = BacnetDeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "vendor_id", type = Integer.class),
                                @ColumnResult(name = "subscribe_cov", type = Boolean.class),
                                @ColumnResult(name = "max_apdu", type = Integer.class),
                                @ColumnResult(name = "segmentation_support", type = Integer.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "network_no", type = Integer.class),
                                @ColumnResult(name = "bacnet_type", type = Integer.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "network_router", type = String.class),
                                @ColumnResult(name = "write_property", type = Boolean.class),
                                @ColumnResult(name = "is_added", type = Integer.class),
                                @ColumnResult(name = "sync_frequency", type = Long.class),
                                @ColumnResult(name = "connectivity_status", type = Integer.class)

                        })
        })


//to be removed after pagination api works
@NamedNativeQuery(
        name = "Bacnet_Device.ListAllBacnetDevices",
        query = "SELECT bd.id , bd.name, bd.ip_address, bd.model, bd.vendor, bd.vendor_id, bd.subscribe_cov, bd.max_apdu, bd.segmentation_support,"
                + " bd.mac_address, bd.network_no, bd.bacnet_type, bd.docker_name, bd.network_router, bd.write_property, bd.is_added, bd.sync_frequency, bd.connectivity_status "
                + " FROM bacnet_device bd "
                + " WHERE (?1 = 'null' OR bd.docker_vdms_id = ?1) AND (?2 = 'all' OR bd.docker_name = ?2) AND  bd.is_added = ?3 ",
        resultSetMapping = "bacnetdeviceinfomapping")

//Added pagination for ListAllBacnetDevices
@NamedNativeQuery(
        name = "Bacnet_Device.listAllBacnetDevicesByPagination",
        query = "SELECT bd.id , bd.name, bd.ip_address, bd.model, bd.vendor, bd.vendor_id, bd.subscribe_cov, bd.max_apdu, bd.segmentation_support,"
                + " bd.mac_address, bd.network_no, bd.bacnet_type, bd.docker_name, bd.network_router, bd.write_property, bd.is_added, bd.sync_frequency, bd.connectivity_status "
                + " FROM bacnet_device bd "
                + " WHERE (?1 = 'null' OR bd.docker_vdms_id = ?1) AND (?2 = 'all' OR bd.docker_name = ?2) AND (?6 = '-1' OR bd.is_added = ?6) "
                + " AND (?3 = 'null' or CONCAT_WS('', bd.ip_address, bd.name, bd.id,  bd.vendor, bd.mac_address, bd.network_no) LIKE CONCAT('%',?3,'%'))"
                + " ORDER BY bd.is_added, bd.id"
                + " LIMIT ?4 OFFSET ?5",
        resultSetMapping = "bacnetdeviceinfomapping")


//new query
@NamedNativeQuery(
        name = "Bacnet_Device.getBacnetDeviceById",
        query = "SELECT bd.id , bd.name, bd.ip_address, bd.model, bd.vendor, bd.vendor_id, bd.subscribe_cov, bd.max_apdu, bd.segmentation_support,"
                + " bd.mac_address, bd.network_no, bd.bacnet_type, bd.docker_name, bd.network_router, bd.write_property, bd.is_added, bd.sync_frequency, bd.connectivity_status "
                + " FROM bacnet_device bd "
                + " WHERE bd.id=?1",
        resultSetMapping = "bacnetdeviceinfomapping")

@NamedNativeQuery(
        name = "Bacnet_Device.getBacnetDeviceBySyncTime",
        query = "SELECT bd.id , bd.name, bd.ip_address, bd.model, bd.vendor, bd.vendor_id, bd.subscribe_cov, bd.max_apdu, bd.segmentation_support,"
                + " bd.mac_address, bd.network_no, bd.bacnet_type, bd.docker_name, bd.network_router, bd.write_property, bd.is_added, bd.sync_frequency, bd.connectivity_status "
                + " FROM bacnet_device bd "
                + " WHERE bd.last_sync IS NOT NULL AND bd.last_sync != 0 AND bd.sync_frequency > 0 AND (bd.last_sync + (bd.sync_frequency - 300000) <= ?2) "
                + " AND bd.docker_name = ?1",
        resultSetMapping = "bacnetdeviceinfomapping")

@NamedNativeQuery(
        name = "Bacnet_Device.getBacnetDevicesForConnectivityStatus",
        query = "SELECT bd.id , bd.name, bd.ip_address, bd.model, bd.vendor, bd.vendor_id, bd.subscribe_cov, bd.max_apdu, bd.segmentation_support,"
                + " bd.mac_address, bd.network_no, bd.bacnet_type, bd.docker_name, bd.network_router, bd.write_property, bd.is_added, bd.sync_frequency,bd.connectivity_status "
                + " FROM bacnet_device bd WHERE bd.docker_name = ?1",
        resultSetMapping = "bacnetdeviceinfomapping")

@NamedNativeQuery(
        name = "Bacnet_Device.getBacnetDeviceByIdForRefresh",
        query = "SELECT bd.id , bd.name, bd.ip_address, bd.model, bd.vendor, bd.vendor_id, bd.subscribe_cov, bd.max_apdu, bd.segmentation_support,"
                + " bd.mac_address, bd.network_no, bd.bacnet_type, bd.docker_name, bd.network_router, bd.write_property, bd.is_added, bd.sync_frequency, bd.connectivity_status "
                + " FROM bacnet_device bd "
                + " WHERE (?1 = 'null' OR bd.docker_vdms_id = ?1) AND (?2 = 'all' OR bd.docker_name = ?2) AND  bd.is_added = ?3 AND bd.id = ?4 ",
        resultSetMapping = "bacnetdeviceinfomapping")


@SqlResultSetMapping(
        name = "bacnetdevicealertmapping",
        classes = {
                @ConstructorResult(
                        targetClass = ConditionsDTO.class,
                        columns = {
                                @ColumnResult(name = "alert_message", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class)
                        })
        })

@NamedNativeQuery(
        name = "Bacnet_Device.listBacnetDevicesAlertMessagesByDeviceIds",
        query = "select distinct(c.alert_message), bo.device_id from bacnet_object bo JOIN conditions c ON bo.bacnet_device_id = c.bacnet_object_bacnet_device_id AND bo.id = c.bacnet_object_id  where c.alert = 1 AND bo.device_id IN ?1 ",
        resultSetMapping = "bacnetdevicealertmapping")


// testing to be removed
@NamedNativeQuery(
        name = "Bacnet_Device.getBacnetDevices",
        query = "SELECT bd.id , bd.name, bd.ip_address, bd.model, bd.vendor, bd.vendor_id, bd.subscribe_cov, bd.max_apdu, bd.segmentation_support,"
                + " bd.mac_address, bd.network_no, bd.bacnet_type, bd.docker_name, bd.network_router, bd.write_property, bd.is_added, bd.sync_frequency "
                + " FROM bacnet_device bd ",
        resultSetMapping = "bacnetdeviceinfomapping")


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

    @Column(name = "object_list", columnDefinition = "LONGTEXT")
    private String object_list;

    @Column
    private Long sync_frequency;

    @Column
    private BigInteger last_sync;

    private Integer connectivity_status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bacnet_device")
    private Set<Bacnet_Object> bacnet_object;


    @ManyToOne
    private Docker docker;


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


    public Docker getDocker() {
        return docker;
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


    public void setDocker(Docker docker) {
        this.docker = docker;
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
