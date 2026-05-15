package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.sclera.dto.touchscreen.assetmapper.AssetDTO;

import javax.persistence.*;
import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@SqlResultSetMapping(
        name = "filteredAssetMapping",
        classes = {
                @ConstructorResult(
                        targetClass = AssetDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "network_layer", type = Integer.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "match_score", type = Integer.class),
                                @ColumnResult(name = "original_keys", type = String.class),
                                @ColumnResult(name = "custom_fields", type = String.class),
                                @ColumnResult(name = "matched_product_ids", type = String.class),
                                @ColumnResult(name = "subsystem_parent_id", type = String.class),
                                @ColumnResult(name = "import_type", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Asset.getPaginatedAssets",
        query = "SELECT id,"
                + "display_name,"
                + "description,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "NULL as match_score ,"
                + "original_keys,"
                + "custom_fields,"
                + "matched_products AS matched_product_ids,"
                + "subsystem_parent_id, import_type FROM asset WHERE import_type = ?3 "
                + " AND(?4 = 'null' or CONCAT_WS('',display_name,description) LIKE CONCAT('%',?4,'%')) "
                + " ORDER BY display_name"
                + " LIMIT ?1 OFFSET ?2",
        resultSetMapping = "filteredAssetMapping")

@NamedNativeQuery(
        name = "Asset.getLinkedAssets",
        query = "SELECT a.id,"
                + "a.display_name,"
                + "a.description,"
                + "a.mac_address,"
                + "a.model,"
                + "a.vendor,"
                + "a.type,"
                + "a.ip_address,"
                + "a.network_layer,"
                + "a.serial_number,"
                + "a.warranty,"
                + "adm.match_score,"
                + "a.original_keys,"
                + "a.custom_fields,"
                + "a.matched_products AS matched_product_ids,"
                + "subsystem_parent_id, import_type FROM asset a RIGHT JOIN asset_device_mapping adm ON a.id=adm.asset_id WHERE a.id IN (SELECT asset_id FROM asset_device_mapping WHERE device_id=?1)",
        resultSetMapping = "filteredAssetMapping")

@NamedNativeQuery(
        name = "Asset.getUnmappedAssets",
        query = "SELECT a.id,"
                + "a.display_name,"
                + "a.description,"
                + "a.mac_address,"
                + "a.model,"
                + "a.vendor,"
                + "a.type,"
                + "a.ip_address,"
                + "a.network_layer,"
                + "a.serial_number,"
                + "a.warranty,"
                + "NULL as match_score,"
                + "a.original_keys,"
                + "a.custom_fields,"
                + "a.matched_products AS matched_product_ids,"
                + "subsystem_parent_id, import_type "
                + " FROM asset a WHERE a.id NOT IN(SELECT asset_id FROM asset_device_mapping) AND is_matched=0 LIMIT ?1 OFFSET ?2",
        resultSetMapping = "filteredAssetMapping")

@NamedNativeQuery(
        name = "Asset.getUnmappedAssetsByIds",
        query = "SELECT a.id,"
                + "a.display_name,"
                + "a.description,"
                + "a.mac_address,"
                + "a.model,"
                + "a.vendor,"
                + "a.type,"
                + "a.ip_address,"
                + "a.network_layer,"
                + "a.serial_number,"
                + "a.warranty,"
                + "NULL as match_score,"
                + "a.original_keys,"
                + "a.custom_fields,"
                + "a.matched_products AS matched_product_ids,"
                + "subsystem_parent_id, import_type "
                + " FROM asset a WHERE a.id NOT IN(SELECT asset_id FROM asset_device_mapping) AND is_matched=0 AND a.id IN(?1)",
        resultSetMapping = "filteredAssetMapping")

@NamedNativeQuery(
        name = "Asset.getFilteredAssets",
        query = "SELECT id,"
                + "display_name,"
                + "description,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "NULL as match_score,"
                + "original_keys,"
                + "custom_fields,matched_products AS matched_product_ids,"
                + "subsystem_parent_id, import_type FROM asset ORDER BY ?1 LIMIT ?2 OFFSET ?3",
        resultSetMapping = "filteredAssetMapping")

@NamedNativeQuery(
        name = "Asset.getAssetsById",
        query = "SELECT id,"
                + "display_name,"
                + "description,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "NULL as match_score,"
                + "original_keys,"
                + "custom_fields,matched_products AS matched_product_ids,"
                + "subsystem_parent_id, import_type FROM asset WHERE id IN ?1",
        resultSetMapping = "filteredAssetMapping")

@NamedNativeQuery(
        name = "Asset.getUnmappedMatchedAssets",
        query = "SELECT id,"
                + "display_name,"
                + "description,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "NULL as match_score,"
                + "original_keys,"
                + "custom_fields,matched_products AS matched_product_ids,"
                + "subsystem_parent_id, import_type FROM asset WHERE is_matched=1 AND id NOT IN(SELECT asset_id FROM asset_device_mapping)",
        resultSetMapping = "filteredAssetMapping")

@NamedNativeQuery(
        name = "Asset.getSubAssetsByParentId",
        query = "SELECT id,"
                + "display_name,"
                + "description,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "NULL as match_score,"
                + "original_keys,"
                + "custom_fields,matched_products AS matched_product_ids,"
                + "subsystem_parent_id, import_type FROM asset WHERE subsystem_parent_id=?1",
        resultSetMapping = "filteredAssetMapping")

//parent asset sub system api
@SqlResultSetMapping(
        name = "subsystemAssetMapping",
        classes = {
                @ConstructorResult(
                        targetClass = AssetDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "network_layer", type = Integer.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "original_keys", type = String.class),
                                @ColumnResult(name = "custom_fields", type = String.class),
                                @ColumnResult(name = "matched_product_ids", type = String.class),
                                @ColumnResult(name = "subsystem_parent_id", type = String.class),
                                @ColumnResult(name = "subsystem_count", type = Integer.class)
                        }
                )
        }
)

//get parent assets
@NamedNativeQuery(
        name = "Asset.getSubSystemParentAssets",
        query = "SELECT id,"
                + "display_name,"
                + "description,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "original_keys,"
                + "custom_fields,"
                + "matched_products AS matched_product_ids,"
                + "subsystem_parent_id, subsystem_count "
                + "FROM asset WHERE subsystem_parent_id IS NULL AND import_type = ?3 "
                + "LIMIT ?1 OFFSET ?2",
        resultSetMapping = "subsystemAssetMapping")

//get sub system assets
@NamedNativeQuery(
        name = "Asset.getSubSystemAssets",
        query = "SELECT id,"
                + "display_name,"
                + "description,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "original_keys,"
                + "custom_fields,"
                + "matched_products AS matched_product_ids,"
                + "subsystem_parent_id, subsystem_count "
                + "FROM asset WHERE subsystem_parent_id = ?1 "
                + "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "subsystemAssetMapping")

//get parent unmapped assets
@NamedNativeQuery(
        name = "Asset.getUnmappedSubSystemParentAssets",
        query = "SELECT id,"
                + "display_name,"
                + "description,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "original_keys,"
                + "custom_fields,"
                + "matched_products AS matched_product_ids,"
                + "subsystem_parent_id, subsystem_count "
                + "FROM asset WHERE subsystem_parent_id IS NULL AND id NOT IN(SELECT asset_id FROM asset_device_mapping) AND is_matched=0 "
                + "LIMIT ?1 OFFSET ?2",
        resultSetMapping = "subsystemAssetMapping")

//get parent unmapped assets by asset ids
@NamedNativeQuery(
        name = "Asset.getUnmappedSubSystemParentAssetsByAssetIds",
        query = "SELECT id,"
                + "display_name,"
                + "description,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "original_keys,"
                + "custom_fields,"
                + "matched_products AS matched_product_ids,"
                + "subsystem_parent_id, subsystem_count "
                + "FROM asset WHERE subsystem_parent_id IS NULL AND id NOT IN(SELECT asset_id FROM asset_device_mapping) AND is_matched=0 AND "
                + "id IN ?1",
        resultSetMapping = "subsystemAssetMapping")

//get all assets
@NamedNativeQuery(
        name = "Asset.getAllAssets",
        query = "SELECT id,"
                + "display_name,"
                + "description,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "original_keys,"
                + "custom_fields,"
                + "matched_products AS matched_product_ids,"
                + "subsystem_parent_id, subsystem_count "
                + "FROM asset WHERE import_type = ?1",
        resultSetMapping = "subsystemAssetMapping")

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

    @Column(name = "original_keys", columnDefinition = "LONGTEXT", nullable = false)
    public String originalKeys;

    @Column(name = "custom_fields", columnDefinition = "LONGTEXT", nullable = true)
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
