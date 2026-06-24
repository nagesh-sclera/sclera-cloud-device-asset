package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrCodeTemplateDTO {

    private String id;
    private String name;
    private String templateName;
    private String companyName;
    private String qrCodeTemplateUrl;
    private String qrCodeLogoUrl;
    private String customerOrgId;
    private String qrTemplateJson;
    private BigInteger creationTimestamp;
    private String addedBy;
    private BigInteger updatedTimestamp;
    private String updatedBy;
    private Integer inUse;
    private Integer isDefault;

    public QrCodeTemplateDTO(String id, String name, String qrCodeTemplateUrl, String qrCodeLogoUrl,
                             String customerOrgId, String qrTemplateJson, Integer inUse, Integer isDefault) {
        this.id = id;
        this.name = name;
        this.qrCodeTemplateUrl = qrCodeTemplateUrl;
        this.qrCodeLogoUrl = qrCodeLogoUrl;
        this.customerOrgId = customerOrgId;
        this.qrTemplateJson = qrTemplateJson;
        this.inUse = inUse;
        this.isDefault=isDefault;
    }
}
