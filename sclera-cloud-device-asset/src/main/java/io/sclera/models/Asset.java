package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.*;
import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * JPA entity representing a managed asset (e.g. a discovered or imported piece of equipment) with
 * its descriptive, network, and matching attributes. Supports hierarchical subsystems, links to
 * devices via {@link AssetDeviceMapping}, and backs the paginated, filtered, and subsystem asset
 * queries.
 */
public class Asset {

    @Id
    public String id; //72

    @Column(length = 128)
    public String display_name = null; //47

    @Column(columnDefinition = "TEXT")
    public String description = null; //4 //46

    @Column(length = 32)
    public String mac_address = null; //Not present

    public String model = null;  //60
    public String vendor = null; //29 //59

    @Column(length = 128)
    public String type = null; //31

    @Column(length = 64)
    public String ip_address = null;

    @Column(length = 64)
    public Integer network_layer = null;


    public String serial_number = null;

    public String import_type = null;

    @Column(length = 32)
    public String warranty = null;
    public Boolean isMatched = false;
    public String subsystem_parent_id = null;
    public Integer subsystem_count = 0;

    @Column(name = "original_keys", columnDefinition = "text", nullable = false)
    public String originalKeys;

    @Column(name = "custom_fields", columnDefinition = "text", nullable = true)
    public String customFields;

    @Column(name = "matched_products", columnDefinition = "TEXT", nullable = true)
    public String matchedProductIds;

    @Transient
    public List<Integer> scores;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "asset")
    List<AssetDeviceMapping> assetDeviceMappings;

    @ManyToOne
    private Vdms vdms;

    public Asset() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public Integer getNetwork_layer() {
        return network_layer;
    }

    public void setNetwork_layer(Integer network_layer) {
        this.network_layer = network_layer;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
    }

    public String getOriginalKeys() {
        return originalKeys;
    }

    public void setOriginalKeys(String originalKeys) {
        this.originalKeys = originalKeys;
    }

    public String getCustomFields() {
        return customFields;
    }

    public void setCustomFields(String customFields) {
        this.customFields = customFields;
    }

    public String getMatchedProductIds() {
        return matchedProductIds;
    }

    public void setMatchedProductIds(String matchedProductIds) {
        this.matchedProductIds = matchedProductIds;
    }

    public List<Integer> getScores() {
        return scores;
    }

    public void setScores(List<Integer> scores) {
        this.scores = scores;
    }

    public List<AssetDeviceMapping> getAssetDeviceMappings() {
        return assetDeviceMappings;
    }

    public void setAssetDeviceMappings(List<AssetDeviceMapping> assetDeviceMappings) {
        this.assetDeviceMappings = assetDeviceMappings;
    }

    public Vdms getVdms() {
        return vdms;
    }

    public void setVdms(Vdms vdms) {
        this.vdms = vdms;
    }

    public Boolean getMatched() {
        return isMatched;
    }

    public void setMatched(Boolean matched) {
        isMatched = matched;
    }

    public String getSubsystem_parent_id() {
        return subsystem_parent_id;
    }

    public void setSubsystem_parent_id(String subsystem_parent_id) {
        this.subsystem_parent_id = subsystem_parent_id;
    }

    public Boolean getIsMatched() {
        return isMatched;
    }

    public void setIsMatched(Boolean isMatched) {
        this.isMatched = isMatched;
    }

    public Integer getSubsystem_count() {
        return subsystem_count;
    }

    public void setSubsystem_count(Integer subsystem_count) {
        this.subsystem_count = subsystem_count;
    }

    public String getImport_type() {
        return import_type;
    }

    public void setImport_type(String import_type) {
        this.import_type = import_type;
    }

    @Override
    public String toString() {
        return "Asset [id=" + id + ", display_name=" + display_name + ", description=" + description + ", mac_address="
                + mac_address + ", model=" + model + ", vendor=" + vendor + ", type=" + type + ", ip_address=" + ip_address
                + ", network_layer=" + network_layer + ", serial_number=" + serial_number + ", warranty=" + warranty
                + ", isMatched=" + isMatched + ", subsystem_parent_id=" + subsystem_parent_id + ", subsystem_count="
                + subsystem_count + ", originalKeys=" + originalKeys + ", customFields=" + customFields
                + ", matchedProductIds=" + matchedProductIds + ", scores=" + scores + ", assetDeviceMappings="
                + assetDeviceMappings + "]";
    }


}
