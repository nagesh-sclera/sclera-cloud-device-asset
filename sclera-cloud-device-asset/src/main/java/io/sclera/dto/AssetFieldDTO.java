package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetFieldDTO {
    private String id;
    private String name;
    private String type;
    private String toolTip;
    private String defaultValue;
    private Boolean isActive;
    private String options;
    private Boolean isDeleted;
    private Integer showInSection;
    private BigInteger createdAt;
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
    public String getToolTip() {return toolTip;}
    public void setToolTip(String toolTip) {this.toolTip = toolTip;}
    public String getDefaultValue() {return defaultValue;}
    public void setDefaultValue(String defaultValue) {this.defaultValue = defaultValue;}
    public Boolean getIsActive() {return isActive;}
    public void setIsActive(Boolean isActive) {this.isActive = isActive;}
    public String getOptions() {return options;}
    public void setOptions(String options) {this.options = options;}
    public Boolean getIsDeleted() {return isDeleted;}
    public void setIsDeleted(Boolean isDeleted) {this.isDeleted = isDeleted;}
    public Integer getShowInSection() {return showInSection;}
    public void setShowInSection(Integer showInSection) {this.showInSection = showInSection;}
    public BigInteger getCreatedAt() {return createdAt;}
    public void setCreatedAt(BigInteger createdAt) {this.createdAt = createdAt;}

    public AssetFieldDTO( String id, String name) {
        this.id = id;
        this.name = name;
    }

    public AssetFieldDTO(String id, String name, String type, String toolTip, String defaultValue, Boolean isActive, String options, Boolean isDeleted, Integer showInSection, BigInteger createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.toolTip = toolTip;
        this.defaultValue = defaultValue;
        this.isActive = isActive;
        this.options = options;
        this.isDeleted = isDeleted;
        this.showInSection = showInSection;
        this.createdAt = createdAt;
    }
    @Override
    public String toString() {
        return "AssetFieldDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", toolTip='" + toolTip + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", isActive=" + isActive +
                ", options='" + options + '\'' +
                ", isDeleted=" + isDeleted +
                ", showInSection=" + showInSection +
                ", createdAt=" + createdAt +
                '}';
    }

}
