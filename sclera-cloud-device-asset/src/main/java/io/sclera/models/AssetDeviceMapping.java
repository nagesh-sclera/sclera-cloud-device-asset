package io.sclera.models;

import io.sclera.dto.touchscreen.assetmapper.AssetDeviceMappingDTO;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

@Entity
@SqlResultSetMapping(
    name = "assetDeviceRelationMapping",
    classes = {
        @ConstructorResult(
            targetClass = AssetDeviceMappingDTO.class,
            columns = {
                @ColumnResult(name = "asset_id", type = String.class),
                @ColumnResult(name = "device_id", type = String.class)
            }
        )
    }
)

@NamedNativeQuery(name = "AssetDeviceMapping.findMappings", query = "SELECT asset_id,device_id FROM asset_device_mapping", resultSetMapping = "assetDeviceRelationMapping")

public class AssetDeviceMapping {

  @Id
  private String id;

  @ManyToOne(fetch = FetchType.EAGER)
  private Device device;

  @ManyToOne(fetch = FetchType.EAGER)
  private Asset asset;

  private Integer matchScore;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    this.device = device;
  }

  public Asset getAsset() {
    return asset;
  }

  public void setAsset(Asset asset) {
    this.asset = asset;
  }

  public Integer getMatchScore() {
    return matchScore;
  }

  public void setMatchScore(Integer matchScore) {
    this.matchScore = matchScore;
  }
}
