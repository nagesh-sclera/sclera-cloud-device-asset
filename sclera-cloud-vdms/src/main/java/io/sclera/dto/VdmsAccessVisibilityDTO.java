package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VdmsAccessVisibilityDTO {
    private String devUId;
    private String vdmsId;
    private String propertyName;
}
