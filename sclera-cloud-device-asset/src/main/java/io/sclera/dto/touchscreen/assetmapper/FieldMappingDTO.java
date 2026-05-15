package io.sclera.dto.touchscreen.assetmapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties
public class FieldMappingDTO {

  private ArrayList<Integer> index;
  private ArrayList<String> originalKey;
  private String deviceKey;
  private Boolean isAdvanced = false;
  private Boolean isCustom = false;
  private Boolean isComposite = false;

  FieldMappingDTO() {
  }

  public FieldMappingDTO(ArrayList<Integer> index, ArrayList<String> originalKey, String deviceKey, Boolean isAdvanced, Boolean isCustom, Boolean isComposite) {
    this.index = index;
    this.originalKey = originalKey;
    this.deviceKey = deviceKey;
    this.isAdvanced = isAdvanced;
    this.isCustom = isCustom;
    this.isComposite = isComposite;
  }

  public ArrayList<Integer> getIndex() {
    return index;
  }

  public void setIndex(ArrayList<Integer> index) {
    this.index = index;
  }

  public ArrayList<String> getOriginalKey() {
    return originalKey;
  }

  public void setOriginalKey(ArrayList<String> originalKey) {
    this.originalKey = originalKey;
  }

  public String getDeviceKey() {
    return deviceKey;
  }

  public void setDeviceKey(String deviceKey) {
    this.deviceKey = deviceKey;
  }

  public Boolean getAdvanced() {
    return isAdvanced;
  }

  public void setAdvanced(Boolean isAdvanced) {
    this.isAdvanced = isAdvanced;
  }

  public Boolean getCustom() {
    return isCustom;
  }

  public void setCustom(Boolean isCustom) {
    this.isCustom = isCustom;
  }

  public Boolean getComposite() {
    return isComposite;
  }

  public void setComposite(Boolean isComposite) {
    this.isComposite = isComposite;
  }

  @Override
  public String toString() {
    return "FieldMappingDTO{" +
            "index=" + index +
            ", originalKey=" + originalKey +
            ", deviceKey='" + deviceKey + '\'' +
            ", isAdvanced=" + isAdvanced +
            ", isCustom=" + isCustom +
            ", isComposite=" + isComposite +
            '}';
  }
}
