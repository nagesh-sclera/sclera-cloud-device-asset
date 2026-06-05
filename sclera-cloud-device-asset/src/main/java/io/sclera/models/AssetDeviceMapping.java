package io.sclera.models;

import io.sclera.dto.touchscreen.assetmapper.AssetDeviceMappingDTO;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.SqlResultSetMapping;

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

/**
 * JPA entity representing the many-to-many link between an {@link Asset} and a {@link Device},
 * recording the match score for the pairing. Used to track which assets are mapped to which devices.
 */
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
