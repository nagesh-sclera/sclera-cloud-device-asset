package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubCategoryDTO {
    private String id;
    private String name;
    private String iconUrl;
    private String categoryId;
    private String displayName;
    private BigInteger creationTimestamp;

    public SubCategoryDTO(String id, String name, String iconUrl, String displayName, BigInteger creationTimestamp) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.displayName = displayName;
        this.creationTimestamp = creationTimestamp;
    }

    public SubCategoryDTO(String id, String name, String iconUrl, BigInteger creationTimestamp) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.creationTimestamp = creationTimestamp;
    }
}
