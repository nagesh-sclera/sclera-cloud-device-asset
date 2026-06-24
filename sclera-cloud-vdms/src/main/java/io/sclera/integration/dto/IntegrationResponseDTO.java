package io.sclera.integration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegrationResponseDTO {

    private Object data;
    private Integer errorCode;
    private String errorMessage;
    private String path;
    private Boolean success;
    private String timestamp;

}
