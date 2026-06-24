package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL) @Data
@AllArgsConstructor
@NoArgsConstructor
public class VdmsFeatureDTO {

    private String id;
    private String featureId;
    private String vdmsId;
}
