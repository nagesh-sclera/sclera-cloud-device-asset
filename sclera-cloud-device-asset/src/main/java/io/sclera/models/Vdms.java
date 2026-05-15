package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.touchscreen.settings.VdmsDTO;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;


@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Vdms.class)


////// TouchScreen Settings API
@SqlResultSetMapping(
        name = "vdmsinfomapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "activation_status", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "timezone", type = String.class),
                                @ColumnResult(name = "property_name", type = String.class),
                                @ColumnResult(name = "isConfigured", type = Boolean.class),
                                @ColumnResult(name = "activation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "deployment_type", type = String.class)
                        }
                )
        }

)

@NamedNativeQuery(
        name = "Vdms.getVdmsInfo",
        query = "SELECT vdms.id,vdms.activation_status,vdms.status,vdms.location,vdms.timezone,vdms.property_name,vdms_configuration.is_configured AS isConfigured, vdms.activation_timestamp, vdms.deployment_type FROM vdms,vdms_configuration WHERE vdms.vdms_configuration_id=vdms_configuration.id",
        resultSetMapping = "vdmsinfomapping"
)


@SqlResultSetMapping(
        name = "vdmsinfomappingstatus",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "property_name", type = String.class),
                                @ColumnResult(name = "address", type = String.class),
                                @ColumnResult(name = "city", type = String.class),
                                @ColumnResult(name = "country", type = String.class),
                                @ColumnResult(name = "state", type = String.class),
                                @ColumnResult(name = "zip", type = Integer.class),
                                @ColumnResult(name = "timezone", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "latitude", type = String.class),
                                @ColumnResult(name = "longitude", type = String.class),
                                @ColumnResult(name = "activation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "deployment_type", type = String.class),
                                @ColumnResult(name = "region", type = String.class)


                        }
                )
        }

)
@NamedNativeQuery(
        name = "Vdms.getVdmsDetails",
        query = "SELECT vdms.id as id, vdms.property_name, vdms.address, vdms.city, vdms.country, vdms.state, vdms.zip, vdms.timezone, vdms.image_url, vdms.latitude, vdms.longitude, vdms.activation_timestamp, vdms.deployment_type, vdms.region FROM vdms",
        resultSetMapping = "vdmsinfomappingstatus"
)

@SqlResultSetMapping(
        name = "syncVdmsMapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "customer_org_id", type = String.class),
                                @ColumnResult(name = "adc_configuration_id", type = String.class),
                                @ColumnResult(name = "zip", type = Integer.class)
                        }
                )
        }

)

@NamedNativeQuery(
        name = "Vdms.getSyncDetailsForADC",
        query = "SELECT id, customer_org_id, adc_configuration_id, zip FROM vdms LIMIT 1",
        resultSetMapping = "syncVdmsMapping"
)

@SqlResultSetMapping(
        name = "vdmsmasterslaveinfomapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "is_master", type = Integer.class),
                                @ColumnResult(name = "has_secondary_device", type = Integer.class),
                                @ColumnResult(name = "secondary_device_id", type = String.class),
                                @ColumnResult(name = "master_ip", type = String.class),
                                @ColumnResult(name = "slave_ip", type = String.class)
                        }
                )
        }

)
@NamedNativeQuery(
        name = "Vdms.getVdmsMasterSlaveDetails",
        query = "SELECT vdms.id as id, vdms.is_master, vdms.has_secondary_device, vdms.secondary_device_id, vdms.master_ip, vdms.slave_ip FROM vdms",
        resultSetMapping = "vdmsmasterslaveinfomapping"
)

public class Vdms {
    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 128)
    private String property_name;

    @Column(length = 16)
    private String activation_status;

    @Column(length = 1)
    private Boolean is_block;

    @Column
    private BigInteger creation_timestamp;

    @Column
    private BigInteger last_seen;

    @Column(length = 64)
    private String public_ip;

    @Column(length = 16)
    private String public_port;

    @Column(length = 8)
    private Integer status;

    @Column(length = 32)
    private String mac_address;

    private String location;

    @Column(length = 128)
    private String timezone;

    @Column(length = 8)
    private String start_port;

    @Column(length = 8)
    private String end_port;

    @Column(length = 10)
    private BigInteger block_timestamp;

    @Column(length = 255)
    private String address;

    @Column(length = 64)
    private String city;

    @Column(length = 64)
    private String country;

    @Column(length = 64)
    private String state;

    @Column(length = 255)
    private Integer zip;

    @Column(length = 255)
    private Integer is_master;

    @Column(length = 255)
    private Integer has_secondary_device;

    @Column(length = 255)
    private String secondary_device_id;

    @Column
    private String slave_ip;

    @Column
    private String master_ip;

    private String latitude;

    private String longitude;

    private String password;

    @Column(length = 128)
    private String image_url;

    private BigInteger activation_timestamp;

    @Column(length = 16)
    private String deployment_type;

    @Column(length = 32)
    private String region;

    @Column(length = 64)
    private String customer_org_id;

    @Column(length = 64)
    private String adc_configuration_id;

    // removed: relation to Bucket-D entity Docker
    // removed: relation to Bucket-D entity Vdms_port
    // removed: relation to Bucket-D entity Vdms_configuration
    // removed: relation to Bucket-C entity Customer_Organisation (CP-2)
    // removed: relation to Bucket-C entity DisruptiveConfiguration (AP-C2)
    // removed: relation to Bucket-C entity Datahoist (AP-C2)
    // removed: relation to Bucket-C entity Lorawan_Sensor (AP-C2)
    // removed: relation to Bucket-C entity MyDevicesCompany (AP-C2)
    // removed: relation to Bucket-D entity Proxy_Profile
    // removed: relation to Bucket-C entity MonnitConfiguration (AP-C2)
    // removed: relation to Bucket-C entity PelicanConfiguration (AP-C2)
    // removed: relation to Bucket-D entity VdmsDetails
    // removed: relation to Bucket-D entity PropertyService
    // removed: relation to Bucket-D entity CorrigoConfiguration
    // removed: relation to Bucket-D entity PortUtility
    // removed: relation to Bucket-D entity Connection
    // removed: relation to Bucket-C entity PolyLensConfiguration (AP-C2)
    // removed: relation to Bucket-C entity MqttConfiguration (AP-C2)
    // removed: relation to Bucket-C entity AwairConfiguration (AP-C2)
    // removed: relation to Bucket-C entity VergeSenseConfiguration (AP-C2)
    // removed: relation to Bucket-C entity MaximoConfiguration (AP-C2)
    // removed: relation to Bucket-C entity GaiameshConfiguration (AP-C2)
    // removed: relation to Bucket-C entity Technician (AP-C3)

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vdms")
    private Set<Building> building;

    @OneToMany(mappedBy = "vdms", cascade = CascadeType.ALL)
    private Set<Asset> asset;

    @OneToMany(mappedBy = "vdms", cascade = CascadeType.ALL)
    private Set<DeviceTechnicianAISuggestion> deviceTechnicianAISuggestion;

    @Column(name = "inspection_activity_timeout", columnDefinition = "INT DEFAULT 0")
    private Integer inspection_activity_timeout = 0;

    @Column
    private BigInteger updated_timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProperty_name() {
        return property_name;
    }

    public void setProperty_name(String property_name) {
        this.property_name = property_name;
    }

    public String getActivation_status() {
        return activation_status;
    }

    public void setActivation_status(String activation_status) {
        this.activation_status = activation_status;
    }

    public Boolean getIs_block() {
        return is_block;
    }

    public void setIs_block(Boolean is_block) {
        this.is_block = is_block;
    }

    public BigInteger getCreation_timestamp() {
        return creation_timestamp;
    }

    public void setCreation_timestamp(BigInteger creation_timestamp) {
        this.creation_timestamp = creation_timestamp;
    }

    public BigInteger getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(BigInteger last_seen) {
        this.last_seen = last_seen;
    }

    public String getPublic_ip() {
        return public_ip;
    }

    public void setPublic_ip(String public_ip) {
        this.public_ip = public_ip;
    }

    public String getPublic_port() {
        return public_port;
    }

    public void setPublic_port(String public_port) {
        this.public_port = public_port;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getStart_port() {
        return start_port;
    }

    public void setStart_port(String start_port) {
        this.start_port = start_port;
    }

    public String getEnd_port() {
        return end_port;
    }

    public void setEnd_port(String end_port) {
        this.end_port = end_port;
    }

    public BigInteger getBlock_timestamp() {
        return block_timestamp;
    }

    public void setBlock_timestamp(BigInteger block_timestamp) {
        this.block_timestamp = block_timestamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getZip() {
        return zip;
    }

    public void setZip(Integer zip) {
        this.zip = zip;
    }

    public Integer getIs_master() {
        return is_master;
    }

    public void setIs_master(Integer is_master) {
        this.is_master = is_master;
    }

    public Integer getHas_secondary_device() {
        return has_secondary_device;
    }

    public void setHas_secondary_device(Integer has_secondary_device) {
        this.has_secondary_device = has_secondary_device;
    }

    public String getSecondary_device_id() {
        return secondary_device_id;
    }

    public void setSecondary_device_id(String secondary_device_id) {
        this.secondary_device_id = secondary_device_id;
    }

    public String getSlave_ip() {
        return slave_ip;
    }

    public void setSlave_ip(String slave_ip) {
        this.slave_ip = slave_ip;
    }

    public String getMaster_ip() {
        return master_ip;
    }

    public void setMaster_ip(String master_ip) {
        this.master_ip = master_ip;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public BigInteger getActivation_timestamp() {
        return activation_timestamp;
    }

    public void setActivation_timestamp(BigInteger activation_timestamp) {
        this.activation_timestamp = activation_timestamp;
    }

    public String getDeployment_type() {
        return deployment_type;
    }

    public void setDeployment_type(String deployment_type) {
        this.deployment_type = deployment_type;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCustomer_org_id() {
        return customer_org_id;
    }

    public void setCustomer_org_id(String customer_org_id) {
        this.customer_org_id = customer_org_id;
    }

    public String getAdc_configuration_id() {
        return adc_configuration_id;
    }

    public void setAdc_configuration_id(String adc_configuration_id) {
        this.adc_configuration_id = adc_configuration_id;
    }

    public Set<Building> getBuilding() {
        return building;
    }

    public void setBuilding(Set<Building> building) {
        this.building = building;
    }

    public Set<Asset> getAsset() {
        return asset;
    }

    public void setAsset(Set<Asset> asset) {
        this.asset = asset;
    }

    public Set<DeviceTechnicianAISuggestion> getDeviceTechnicianAISuggestion() {
        return deviceTechnicianAISuggestion;
    }

    public void setDeviceTechnicianAISuggestion(Set<DeviceTechnicianAISuggestion> deviceTechnicianAISuggestion) {
        this.deviceTechnicianAISuggestion = deviceTechnicianAISuggestion;
    }

    public Integer getInspection_activity_timeout() {
        return inspection_activity_timeout;
    }

    public void setInspection_activity_timeout(Integer inspection_activity_timeout) {
        this.inspection_activity_timeout = inspection_activity_timeout;
    }

    public BigInteger getUpdated_timestamp() {
        return updated_timestamp;
    }

    public void setUpdated_timestamp(BigInteger updated_timestamp) {
        this.updated_timestamp = updated_timestamp;
    }
}