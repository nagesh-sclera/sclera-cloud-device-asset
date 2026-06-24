package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DigitalTwinTemplateDTO {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String vdmsId;
    private String categoryId;
    private String categoryName;
    private String subCategoryId;
    private String subCategoryName;
    private List<DigitalTwinMeasuringInstrumentDTO> digitalTwinMeasuringInstrumentList;
}
