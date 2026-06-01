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

//******************************New Changes for Lorawan**********************************************
// PG-port: IF(user_data_name IS NULL OR...)->CASE WHEN (all lorawan sensor queries below)
//to be removed after pagination api works
//Added pagination for getLorawanSensors
//get lorawan sensor for touchscreen popup socket event
//get lorawan sensor info tagged to a device
//**************************New Changes for Lorawan*************************
//to be removed after pagination api works
//Added pagination for getNetworkLorawanSensors
//Touchscreen New Code***************************************************************

//To be removed after pagination api works
//Added pagination for getLorawanSensorsTS
////
//
//
//
//
////get lorawan sensor details by id for all required platforms
// PG-port: IF->CASE WHEN (device_name, model, vendor, type, sensor_alert); monnit_status bare->='alert'
// PG-gap: references lorawan_sensor_attributes via conditions join (sensor-integration table)
// PG-port: UNIX_TIMESTAMP()->EXTRACT(EPOCH FROM NOW())::bigint
// last_seen is numeric(38,0) epoch-ms; UNIX_TIMESTAMP()*1000 -> EXTRACT(EPOCH FROM NOW())::bigint*1000
// PG-port: UNIX_TIMESTAMP()->EXTRACT(EPOCH FROM NOW())::bigint
// last_seen is numeric(38,0) epoch-ms; UNIX_TIMESTAMP()*1000 -> EXTRACT(EPOCH FROM NOW())::bigint*1000
// PG-port: UNIX_TIMESTAMP()->EXTRACT(EPOCH FROM NOW())::bigint
// last_seen is numeric(38,0) epoch-ms; UNIX_TIMESTAMP()*1000 -> EXTRACT(EPOCH FROM NOW())::bigint*1000
// PG-port: UNIX_TIMESTAMP()->EXTRACT(EPOCH FROM NOW())::bigint
// last_seen is numeric(38,0) epoch-ms; UNIX_TIMESTAMP()*1000 -> EXTRACT(EPOCH FROM NOW())::bigint*1000
public class Lorawan_Sensor {

    @Id
    private String id;

    @Column(length = 128)
    private String name;

    @Column(length = 64)
    private String app_key;

    @Column(length = 64)
    private String sensor_device_id;

    @Column(length = 64)
    private String sensor_type;

    @Column(columnDefinition = "boolean default false")
    private Boolean is_battery_low;

    @Column(columnDefinition = "boolean default false")
    private Boolean configuration;

    @Column(columnDefinition = "boolean default false")
    private Boolean is_deleted;

    private String model_id;

    private String model_name;

    private String manufacturer;

    private String image_url;

    private BigInteger last_seen;

    @Column(length = 64)
    private Integer signal_strength;

    @Column(length = 8)
    private Integer lorawan_device_type;

    @Column(length = 64)
    private String app_session_key;

    @Column(length = 64)
    private String network_session_key;

    @Column(length = 64)
    private String sensor_device_address;

    @Column(length = 64)
    private String serving_network_session_key;

    @Column(length = 64)
    private String forwarding_network_session_key;

    @Column(length = 64)
    private String network_key;

    @Column(length = 64)
    private String sensor_device_profile_name;

    @Column(length = 1, columnDefinition = "integer default 0")
    private Integer sensor_join_status;

    @Column(length = 64)
    private String battery;

    @Column(columnDefinition = "boolean default false")
    private Boolean alert;

    @Column(columnDefinition = "text")
    private String sensor_info;


    // TODO: replace with Dapr call when lorawan-sensor-attributes module is ready
    @jakarta.persistence.Transient
    private Set<Lorawan_Sensor_Attributes> lorawan_sensor_attributes;

    // PG-port: scalar FK (Device entity lives in cloud-device-asset).
    @Column(name = "device_id")
    private String device_id;

    // PG-port: scalar FK (Vdms entity lives in cloud-device-asset / vdms-service).
    @Column(name = "vdms_id")
    private String vdms_id;

    @ManyToOne
    private LorawanConfiguration lorawan_configuration;

    private String sensor_device_profile_id;

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

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }

    public String getSensor_device_id() {
        return sensor_device_id;
    }

    public void setSensor_device_id(String sensor_device_id) {
        this.sensor_device_id = sensor_device_id;
    }

    public String getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(String sensor_type) {
        this.sensor_type = sensor_type;
    }

    public Boolean getIs_battery_low() {
        return is_battery_low;
    }

    public void setIs_battery_low(Boolean is_battery_low) {
        this.is_battery_low = is_battery_low;
    }

    public Boolean getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Boolean configuration) {
        this.configuration = configuration;
    }

    public Boolean getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(Boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public String getModel_id() {
        return model_id;
    }

    public void setModel_id(String model_id) {
        this.model_id = model_id;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

//	public Integer getEmail_alert() {
//		return email_alert;
//	}
//
//	public void setEmail_alert(Integer email_alert) {
//		this.email_alert = email_alert;
//	}
//
//	public Integer getSms_alert() {
//		return sms_alert;
//	}
//
//	public void setSms_alert(Integer sms_alert) {
//		this.sms_alert = sms_alert;
//	}

    public BigInteger getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(BigInteger last_seen) {
        this.last_seen = last_seen;
    }

    public Integer getSignal_strength() {
        return signal_strength;
    }

    public void setSignal_strength(Integer signal_strength) {
        this.signal_strength = signal_strength;
    }


//	public Lorawan_Gateway getLorawan_gateway() {
//		return lorawan_gateway;
//	}
//
//	public void setLorawan_gateway(Lorawan_Gateway lorawan_gateway) {
//		this.lorawan_gateway = lorawan_gateway;
//	}

    public Integer getLorawan_device_type() {
        return lorawan_device_type;
    }

    public void setLorawan_device_type(Integer lorawan_device_type) {
        this.lorawan_device_type = lorawan_device_type;
    }

    public String getApp_session_key() {
        return app_session_key;
    }

    public void setApp_session_key(String app_session_key) {
        this.app_session_key = app_session_key;
    }

    public String getNetwork_session_key() {
        return network_session_key;
    }

    public void setNetwork_session_key(String network_session_key) {
        this.network_session_key = network_session_key;
    }

    public String getSensor_device_address() {
        return sensor_device_address;
    }

    public void setSensor_device_address(String sensor_device_address) {
        this.sensor_device_address = sensor_device_address;
    }

    public String getServing_network_session_key() {
        return serving_network_session_key;
    }

    public void setServing_network_session_key(String serving_network_session_key) {
        this.serving_network_session_key = serving_network_session_key;
    }

    public String getForwarding_network_session_key() {
        return forwarding_network_session_key;
    }

    public void setForwarding_network_session_key(String forwarding_network_session_key) {
        this.forwarding_network_session_key = forwarding_network_session_key;
    }

    public String getNetwork_key() {
        return network_key;
    }

    public void setNetwork_key(String network_key) {
        this.network_key = network_key;
    }

    public String getSensor_device_profile_name() {
        return sensor_device_profile_name;
    }

    public void setSensor_device_profile_name(String sensor_device_profile_name) {
        this.sensor_device_profile_name = sensor_device_profile_name;
    }

    public Integer getSensor_join_status() {
        return sensor_join_status;
    }

    public void setSensor_join_status(Integer sensor_join_status) {
        this.sensor_join_status = sensor_join_status;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public String getSensor_info() {
        return sensor_info;
    }

    public void setSensor_info(String sensor_info) {
        this.sensor_info = sensor_info;
    }

    public Set<Lorawan_Sensor_Attributes> getLorawan_sensor_attributes() {
        return lorawan_sensor_attributes;
    }

    public void setLorawan_sensor_attributes(Set<Lorawan_Sensor_Attributes> lorawan_sensor_attributes) {
        this.lorawan_sensor_attributes = lorawan_sensor_attributes;
    }


//	public Location getLocation() {
//		return location;
//	}
//
//	public void setLocation(Location location) {
//		this.location = location;
//	}

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getVdms_id() {
        return vdms_id;
    }

    public void setVdms_id(String vdms_id) {
        this.vdms_id = vdms_id;
    }

    public LorawanConfiguration getLorawan_configuration() {
        return lorawan_configuration;
    }

    public void setLorawan_configuration(LorawanConfiguration lorawan_configuration) {
        this.lorawan_configuration = lorawan_configuration;
    }
    public String getSensor_device_profile_id() {
        return sensor_device_profile_id;
    }

    public void setSensor_device_profile_id(String sensor_device_profile_id) {
        this.sensor_device_profile_id = sensor_device_profile_id;
    }

    public Lorawan_Sensor() {
        super();
    }

    public Lorawan_Sensor(String id) {
        super();
        this.id = id;
    }


}
