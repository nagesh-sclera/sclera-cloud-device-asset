package io.sclera.dto.touchscreen.assetmapper;

public class AssetDeviceMappingDTO {

  private String asset_id;
  private String device_id;

  public AssetDeviceMappingDTO(String asset_id, String device_id) {
    this.asset_id = asset_id;
    this.device_id = device_id;
  }

  public String getAsset_id() {
    return asset_id;
  }

  public void setAsset_id(String asset_id) {
    this.asset_id = asset_id;
  }

  public String getDevice_id() {
    return device_id;
  }

  public void setDevice_id(String device_id) {
    this.device_id = device_id;
  }

  @Override
  public String toString() {
    return "AssetDeviceMappingDTO{" +
        "asset_id='" + asset_id + '\'' +
        ", device_id='" + device_id + '\'' +
        '}';
  }
}
