package io.sclera.dto.touchscreen.assetmapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssetDTO {

    private String id; //72
    private String display_name; //47
    private String description; //4 //46
    private String mac_address; //Not present
    private String model;  //60
    private String vendor; //29 //59
    private String type; //31
    private String ip_address;
    private Integer network_layer;
    private String serial_number;
    private String warranty;
    private Integer match_score;
    private Set<String> matchedProducts = new HashSet<>();
    private String matched_product_ids;
    private String originalKeys;
    private String customFields;
    private Boolean exactMatch;
    private String subsystem_parent_id;
    private String import_type;
    private Integer subsystem_count;
    private List<AssetDTO> subsystems;

    public AssetDTO() {
    }

    public AssetDTO(String id, String display_name, String description, String mac_address, String model, String vendor, String type, String ip_address, Integer network_layer,
                    String serial_number, String warranty, String originalKeys, String customFields) {
        this.id = id;
        this.display_name = display_name;
        this.description = description;
        this.mac_address = mac_address;
        this.model = model;
        this.vendor = vendor;
        this.type = type;
        this.ip_address = ip_address;
        this.network_layer = network_layer;
        this.serial_number = serial_number;
        this.warranty = warranty;
        this.originalKeys = originalKeys;
        this.customFields = customFields;
    }

    public AssetDTO(String id, String display_name, String description, String mac_address, String model, String vendor, String type, String ip_address, Integer network_layer,
                    String serial_number, String warranty, Integer match_score, String originalKeys, String customFields, String matchedProductsIds, String subsystem_parent_id, String import_type) {
        this.id = id;
        this.display_name = display_name;
        this.description = description;
        this.mac_address = mac_address;
        this.model = model;
        this.vendor = vendor;
        this.type = type;
        this.ip_address = ip_address;
        this.network_layer = network_layer;
        this.serial_number = serial_number;
        this.warranty = warranty;
        this.match_score = match_score;
        this.originalKeys = originalKeys;
        this.customFields = customFields;
        this.matched_product_ids = matchedProductsIds;
        this.subsystem_parent_id = subsystem_parent_id;
        this.import_type = import_type;
    }

    public AssetDTO(String id, String display_name, String description, String mac_address, String model, String vendor, String type, String ip_address, Integer network_layer,
                    String serial_number, String warranty, Boolean exactMatch, String originalKeys, String customFields) {
        this.id = id;
        this.display_name = display_name;
        this.description = description;
        this.mac_address = mac_address;
        this.model = model;
        this.vendor = vendor;
        this.type = type;
        this.ip_address = ip_address;
        this.network_layer = network_layer;
        this.serial_number = serial_number;
        this.warranty = warranty;
        this.exactMatch = exactMatch;
        this.originalKeys = originalKeys;
        this.customFields = customFields;
    }

    //constructor used for parent sub system mapping api
    public AssetDTO(String id, String display_name, String description, String mac_address, String model, String vendor, String type, String ip_address, Integer network_layer,
                    String serial_number, String warranty, String originalKeys, String customFields, String matchedProductsIds, String subsystem_parent_id, Integer subsystem_count) {
        this.id = id;
        this.display_name = display_name;
        this.description = description;
        this.mac_address = mac_address;
        this.model = model;
        this.vendor = vendor;
        this.type = type;
        this.ip_address = ip_address;
        this.network_layer = network_layer;
        this.serial_number = serial_number;
        this.warranty = warranty;
        this.originalKeys = originalKeys;
        this.customFields = customFields;
        this.matched_product_ids = matchedProductsIds;
        this.subsystem_parent_id = subsystem_parent_id;
        this.subsystem_count = subsystem_count;
    }

    //constructor used for setting unmatched devices details for getting product information
    public AssetDTO(String id, String display_name, String model, String vendor, String type, String matched_product_ids) {
        this.id = id;
        this.display_name = display_name;
        this.model = model;
        this.vendor = vendor;
        this.type = type;
        this.matched_product_ids = matched_product_ids;
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

    public Integer getMatch_score() {
        return match_score;
    }

    public void setMatch_score(Integer match_score) {
        this.match_score = match_score;
    }

    public Set<String> getMatchedProducts() {
        return matchedProducts;
    }

    public void setMatchedProducts(Set<String> matchedProducts) {
        this.matchedProducts = matchedProducts;
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

    public Boolean getExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(Boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public String getMatched_product_ids() {
        return matched_product_ids;
    }

    public void setMatched_product_ids(String matched_product_ids) {
        this.matched_product_ids = matched_product_ids;
    }

    public String getSubsystem_parent_id() {
        return subsystem_parent_id;
    }

    public void setSubsystem_parent_id(String subsystem_parent_id) {
        this.subsystem_parent_id = subsystem_parent_id;
    }

    public Integer getSubsystem_count() {
        return subsystem_count;
    }

    public void setSubsystem_count(Integer subsystem_count) {
        this.subsystem_count = subsystem_count;
    }

    public List<AssetDTO> getSubsystems() {
        return subsystems;
    }

    public void setSubsystems(List<AssetDTO> subsystems) {
        this.subsystems = subsystems;
    }

    public String getImport_type() {
        return import_type;
    }

    public void setImport_type(String import_type) {
        this.import_type = import_type;
    }

    @Override
    public String toString() {
        return "AssetDTO [id=" + id + ", display_name=" + display_name + ", description=" + description + ", mac_address="
                + mac_address + ", model=" + model + ", vendor=" + vendor + ", type=" + type + ", ip_address=" + ip_address
                + ", network_layer=" + network_layer + ", serial_number=" + serial_number + ", warranty=" + warranty
                + ", match_score=" + match_score + ", matchedProducts=" + matchedProducts + ", matched_product_ids="
                + matched_product_ids + ", originalKeys=" + originalKeys + ", customFields=" + customFields
                + ", exactMatch=" + exactMatch + ", subsystem_parent_id=" + subsystem_parent_id + ", subsystem_count="
                + subsystem_count + ", subsystems=" + subsystems + "]";
    }


}
