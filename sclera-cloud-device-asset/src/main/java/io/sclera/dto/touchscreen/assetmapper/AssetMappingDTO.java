package io.sclera.dto.touchscreen.assetmapper;

public class AssetMappingDTO {

  private String previous_device_id;
  private String new_device_id;
  private String asset_id;
  private Boolean remove_match = false;

  AssetMappingDTO() {
  }

  public AssetMappingDTO(String previous_device_id, String new_device_id, String asset_id, Boolean remove_match) {
    this.previous_device_id = previous_device_id;
    this.new_device_id = new_device_id;
    this.asset_id = asset_id;
    this.remove_match = remove_match;
  }

  public String getPrevious_device_id() {
    return previous_device_id;
  }

  public void setPrevious_device_id(String previous_device_id) {
    this.previous_device_id = previous_device_id;
  }

  public String getNew_device_id() {
    return new_device_id;
  }

  public void setNew_device_id(String new_device_id) {
    this.new_device_id = new_device_id;
  }

  public String getAsset_id() {
    return asset_id;
  }

  public void setAsset_id(String asset_id) {
    this.asset_id = asset_id;
  }

  public Boolean getRemove_match() {
    return remove_match;
  }

  public void setRemove_match(Boolean remove_match) {
    this.remove_match = remove_match;
  }

  @Override
  public String toString() {
    return "AssetMappingDTO{" +
        "previous_device_id='" + previous_device_id + '\'' +
        ", new_device_id='" + new_device_id + '\'' +
        ", asset_id='" + asset_id + '\'' +
        ", remove_match=" + remove_match +
        '}';
  }
}
