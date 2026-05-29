package io.sclera.integrations.model;

import java.math.BigInteger;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import io.sclera.integrations.model.compositeclass.Bacnet_ObjectIds;
@Entity
@IdClass(Bacnet_ObjectIds.class)
//New Changes ***************************************************************
//to be removed after pagination api works
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
//Added pagination for getBacnetObjectList
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
//get bacnet object for touchscreen popup socket event
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
//get bacnet object info tagged to a device
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
//Touchscreen
//to be removed after pagination api works
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
//Added pagination for listBacnetSensorsTS
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
//to be removed after pagination api works
//get all bacnet objects of a network
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
//Added pagination for getNetworkBacnetObjects
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
//**********************New code changes*******************************************************
//get bacnet object info for bacnet alert
// PG-port: IF(...)->CASE WHEN (x2) (MySQL->PostgreSQL)
////
//
//
////
////
//
//
////


// PG-port: IF(...)->CASE WHEN (x2), IFNULL->COALESCE (MySQL->PostgreSQL)
//get bancet object all details required for all platforms by bancet device id and bacnet object id
// PG-port: IF(...)->CASE WHEN (x5); bare OR d.monnit_status -> OR d.monnit_status = 'alert' (MySQL truthy->PG boolean) (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (x2), IFNULL->COALESCE (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (x2), IFNULL->COALESCE (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (x2), IFNULL->COALESCE (MySQL->PostgreSQL)
////
//// PG-port: IF(...)->CASE WHEN (x3) (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (x2) (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (x2), IFNULL->COALESCE (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (x2), IFNULL->COALESCE (MySQL->PostgreSQL)
// PG-port: IF(...)->CASE WHEN (x2) (MySQL->PostgreSQL)
public class Bacnet_Object {

    @Id
    private String id;

    @MapsId
    @ManyToOne
    private Bacnet_Device bacnet_device;

    @Column(length = 128)
    private String name;

    private Integer type;

    private Integer instance;

    @Column(length = 64)
    private String present_value;

    private Boolean validity;

    @Column(length = 64)
    private String unit;

    @Column(length = 256)
    private String state_text;

    @Column(length = 128)
    private String user_data_name;

    @Column(length = 64)
    private String user_data_value;

    @Column(length = 64)
    private String category;

    private String sub_category;

    @Column(columnDefinition = "boolean default false")
    private Boolean configuration;

    private String condition_type;

    private String possible_values;

    @Column(columnDefinition = "boolean default false")
    private Boolean alert;

    private BigInteger last_seen;

    @Column(length = 64)
    private Integer cov_subscription;

    @Column(length = 64)
    private String high_limit;

    @Column(length = 64)
    private String low_limit;

    @Column(length = 64)
    private Integer present_value_data_type;

    @Column(columnDefinition = "integer default 1")
    private Integer show_on_map;

    @Column(columnDefinition = "integer default 1")
    private Integer show_on_scan;

    //	@ManyToOne
    //	private Location location;

    // removed: @OneToMany(mappedBy="bacnet_object") — Conditions uses scalar FK fields (loose coupling)

    // PG-port: History entity lives in cloud-device-asset; relation removed (loose coupling).

    // PG-port: scalar FK (Device entity lives in cloud-device-asset).
    @Column(name = "device_id")
    private String device_id;

    @Column
    private Boolean off_normal;

    @Column
    private Boolean normal;

    @Column
    private Boolean fault;

    @OneToMany(mappedBy = "bacnet_object", cascade = CascadeType.ALL)
    private Set<Bacnet_Attributes> bacnet_attributes;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getType() {
        return type;
    }

    public Integer getInstance() {
        return instance;
    }

    public String getPresent_value() {
        return present_value;
    }

    public Boolean getValidity() {
        return validity;
    }

    public String getUnit() {
        return unit;
    }

    public String getState_text() {
        return state_text;
    }

    public String getUser_data_name() {
        return user_data_name;
    }

    public String getUser_data_value() {
        return user_data_value;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getConfiguration() {
        return configuration;
    }

    public Bacnet_Device getBacnet_device() {
        return bacnet_device;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setInstance(Integer instance) {
        this.instance = instance;
    }

    public void setPresent_value(String present_value) {
        this.present_value = present_value;
    }

    public void setValidity(Boolean validity) {
        this.validity = validity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setState_text(String state_text) {
        this.state_text = state_text;
    }

    public void setUser_data_name(String user_data_name) {
        this.user_data_name = user_data_name;
    }

    public void setUser_data_value(String user_data_value) {
        this.user_data_value = user_data_value;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setConfiguration(Boolean configuration) {
        this.configuration = configuration;
    }

    public String getCondition_type() {
        return condition_type;
    }

    public void setCondition_type(String condition_type) {
        this.condition_type = condition_type;
    }

    public String getPossible_values() {
        return possible_values;
    }

    public void setPossible_values(String possible_values) {
        this.possible_values = possible_values;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public BigInteger getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(BigInteger last_seen) {
        this.last_seen = last_seen;
    }

    public Integer getCov_subscription() {
        return cov_subscription;
    }

    public void setCov_subscription(Integer cov_subscription) {
        this.cov_subscription = cov_subscription;
    }

    public String getHigh_limit() {
        return high_limit;
    }

    public void setHigh_limit(String high_limit) {
        this.high_limit = high_limit;
    }

    public String getLow_limit() {
        return low_limit;
    }

    public void setLow_limit(String low_limit) {
        this.low_limit = low_limit;
    }

    public Integer getPresent_value_data_type() {
        return present_value_data_type;
    }

    public void setPresent_value_data_type(Integer present_value_data_type) {
        this.present_value_data_type = present_value_data_type;
    }

    public Integer getShow_on_map() {
        return show_on_map;
    }

    public void setShow_on_map(Integer show_on_map) {
        this.show_on_map = show_on_map;
    }

    public Integer getShow_on_scan() {
        return show_on_scan;
    }

    public void setShow_on_scan(Integer show_on_scan) {
        this.show_on_scan = show_on_scan;
    }

    public void setBacnet_device(Bacnet_Device bacnet_device) {
        this.bacnet_device = bacnet_device;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getSub_category() {
        return sub_category;
    }

    public void setSub_category(String sub_category) {
        this.sub_category = sub_category;
    }

    public Boolean getOff_normal() {
        return off_normal;
    }

    public void setOff_normal(Boolean off_normal) {
        this.off_normal = off_normal;
    }

    public Boolean getNormal() {
        return normal;
    }

    public void setNormal(Boolean normal) {
        this.normal = normal;
    }

    public Boolean getFault() {
        return fault;
    }


    public void setFault(Boolean fault) {
        this.fault = fault;


    }
}
