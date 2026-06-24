package io.sclera.integration.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.sclera.dto.ProxyProfileDTO;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VdmsAccessDTO {

    public String vdmsId;
    private String propertyName;
    private String primaryProxyProfileId;
    private String url;
    private ProxyProfileDTO proxyProfile;

    public VdmsAccessDTO(String vdmsId, String propertyName, String primaryProxyProfileId) {
        this.vdmsId = vdmsId;
        this.propertyName = propertyName;
        this.primaryProxyProfileId = primaryProxyProfileId;
    }

    public VdmsAccessDTO(String vdmsId, String propertyName) {
        this.vdmsId = vdmsId;
        this.propertyName = propertyName;
    }
}
