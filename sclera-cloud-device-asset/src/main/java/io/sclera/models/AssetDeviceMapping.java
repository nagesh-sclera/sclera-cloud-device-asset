package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
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
