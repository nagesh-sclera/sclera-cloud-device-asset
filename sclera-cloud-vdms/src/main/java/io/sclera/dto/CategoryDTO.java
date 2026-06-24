package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO {
    private String id;
    private String name;
    private String iconUrl;
    private String displayName;
    private String sensorCategoryGroupName;
    private BigInteger creationTimestamp;
    private BigInteger updatedTimestamp;
    private String subCategoryCount;
    private String assetTypeGroupName;
    private String assetGroupId;
    private List<SubCategoryDTO> subCategoryDTOS;

    public CategoryDTO(String id, String name, String iconUrl, String displayName, BigInteger creationTimestamp, String subCategoryCount) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.displayName = displayName;
        this.creationTimestamp = creationTimestamp;
        this.subCategoryCount = subCategoryCount;
    }

    public CategoryDTO(String id, String name, String iconUrl, List<SubCategoryDTO> subCategoryDTOS) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.subCategoryDTOS = subCategoryDTOS;
    }

    public CategoryDTO(String id, String name, String iconUrl, String displayName, BigInteger creationTimestamp) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.displayName = displayName;
        this.creationTimestamp = creationTimestamp;
    }

    public CategoryDTO(String id, String name, String iconUrl, String displayName, BigInteger creationTimestamp,
                       String assetTypeGroupName, String assetGroupId) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.displayName = displayName;
        this.creationTimestamp = creationTimestamp;
        this.assetTypeGroupName = assetTypeGroupName;
        this.assetGroupId = assetGroupId;
    }

    public CategoryDTO(String iconUrl, String assetTypeGroupName) {
        this.iconUrl = iconUrl;
        this.assetTypeGroupName = assetTypeGroupName;
    }

    public CategoryDTO(String id, String name, String iconUrl, String displayName, BigInteger creationTimestamp,
                       String assetTypeGroupName, String assetGroupId,BigInteger updatedTimestamp) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.displayName = displayName;
        this.creationTimestamp = creationTimestamp;
        this.assetTypeGroupName = assetTypeGroupName;
        this.assetGroupId = assetGroupId;
        this.updatedTimestamp=updatedTimestamp;
    }
}
