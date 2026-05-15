package io.sclera.dto.touchscreen.assetmapper;

import java.util.List;

public class AssetDeviceDTO {

  private String id; //72
  private String display_name; //47
  private String mac_address; //Not present
  private String model;  //60
  private String vendor; //29 //59
  private String type; //31
  private String ip_address;
  private Integer network_layer;
  private String serial_number;
  private String warranty;
  private String customFields;
  private List<AssetDTO> linkedAssets;

  public AssetDeviceDTO(String id, String display_name, String mac_address, String model, String vendor, String type, String ip_address, Integer network_layer,
      String serial_number, String warranty, String customFields) {
    this.id = id;
    this.display_name = display_name;
    this.mac_address = mac_address;
    this.model = model;
    this.vendor = vendor;
    this.type = type;
    this.ip_address = ip_address;
    this.network_layer = network_layer;
    this.serial_number = serial_number;
    this.warranty = warranty;
    this.customFields = customFields;
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

  public String getCustomFields() {
    return customFields;
  }

  public void setCustomFields(String customFields) {
    this.customFields = customFields;
  }

  public List<AssetDTO> getLinkedAssets() {
    return linkedAssets;
  }

  public void setLinkedAssets(List<AssetDTO> linkedAssets) {
    this.linkedAssets = linkedAssets;
  }

  @Override
  public String toString() {
    return "AssetDeviceDTO{" +
        "id='" + id + '\'' +
        ", display_name='" + display_name + '\'' +
        ", mac_address='" + mac_address + '\'' +
        ", model='" + model + '\'' +
        ", vendor='" + vendor + '\'' +
        ", type='" + type + '\'' +
        ", ip_address='" + ip_address + '\'' +
        ", network_layer=" + network_layer +
        ", serial_number='" + serial_number + '\'' +
        ", warranty='" + warranty + '\'' +
        ", customFields='" + customFields + '\'' +
        ", linkedAssets=" + linkedAssets +
        '}';
  }
}

