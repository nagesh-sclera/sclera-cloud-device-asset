// Generated for device-asset; ported from sclera-cloud-vdms QrCodeTemplateDTO (delombok-style, no Lombok).
package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigInteger;

/**
 * Projection DTO for QR code template query results.
 * The 8-arg constructor matches the @ConstructorResult column order declared on QrCodeTemplate entity.
 */
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

    public QrCodeTemplateDTO() {
    }

    /** Constructor matched by @ConstructorResult in QrCodeTemplate entity (8 columns). */
    public QrCodeTemplateDTO(String id, String name, String qrCodeTemplateUrl, String qrCodeLogoUrl,
                             String customerOrgId, String qrTemplateJson, Integer inUse, Integer isDefault) {
        this.id = id;
        this.name = name;
        this.qrCodeTemplateUrl = qrCodeTemplateUrl;
        this.qrCodeLogoUrl = qrCodeLogoUrl;
        this.customerOrgId = customerOrgId;
        this.qrTemplateJson = qrTemplateJson;
        this.inUse = inUse;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getQrCodeTemplateUrl() { return qrCodeTemplateUrl; }
    public void setQrCodeTemplateUrl(String qrCodeTemplateUrl) { this.qrCodeTemplateUrl = qrCodeTemplateUrl; }

    public String getQrCodeLogoUrl() { return qrCodeLogoUrl; }
    public void setQrCodeLogoUrl(String qrCodeLogoUrl) { this.qrCodeLogoUrl = qrCodeLogoUrl; }

    public String getCustomerOrgId() { return customerOrgId; }
    public void setCustomerOrgId(String customerOrgId) { this.customerOrgId = customerOrgId; }

    public String getQrTemplateJson() { return qrTemplateJson; }
    public void setQrTemplateJson(String qrTemplateJson) { this.qrTemplateJson = qrTemplateJson; }

    public BigInteger getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(BigInteger creationTimestamp) { this.creationTimestamp = creationTimestamp; }

    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }

    public BigInteger getUpdatedTimestamp() { return updatedTimestamp; }
    public void setUpdatedTimestamp(BigInteger updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public Integer getInUse() { return inUse; }
    public void setInUse(Integer inUse) { this.inUse = inUse; }

    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
}
