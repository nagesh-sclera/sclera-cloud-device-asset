package io.sclera.model;


import io.sclera.dto.QrCodeTemplateDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity

@SqlResultSetMapping(
        name = "QrCodeTemplateMapping",
        classes = {
                @ConstructorResult(
                        targetClass = QrCodeTemplateDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "qrCodeTemplateUrl", type = String.class),
                                @ColumnResult(name = "qrCodeLogoUrl", type = String.class),
                                @ColumnResult(name = "customerOrgId", type = String.class),
                                @ColumnResult(name = "qrTemplateJson", type = String.class),
                                @ColumnResult(name = "inUse",type = Integer.class),
                                @ColumnResult(name = "isDefault",type = Integer.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "QrCodeTemplate.getAllQrCodeTemplateByOrgId",
        query = "SELECT id,name,qr_code_template_url AS qrCodeTemplateUrl,qr_code_logo_url AS qrCodeLogoUrl,customer_org_id AS customerOrgId, " +
                "qr_template_json AS qrTemplateJson,in_use AS inUse, is_default AS isDefault FROM qr_code_template WHERE (?1 = 'all' OR CONCAT_WS('',name) " +
                "LIKE CONCAT('%',?1,'%')) AND customer_org_id IS NOT NULL AND customer_org_id = ?2 LIMIT ?3 OFFSET ?4",
        resultSetMapping = "QrCodeTemplateMapping"
)


@NamedNativeQuery(
        name = "QrCodeTemplate.getAllQrCodeTemplate",
        query = "SELECT id,name,qr_code_template_url AS qrCodeTemplateUrl,qr_code_logo_url AS qrCodeLogoUrl,customer_org_id AS customerOrgId, " +
                "qr_template_json AS qrTemplateJson,in_use AS inUse, is_default AS isDefault FROM qr_code_template WHERE (?1 = 'all' OR CONCAT_WS('',name) " +
                "LIKE CONCAT('%',?1,'%'))LIMIT ?2 OFFSET ?3",
        resultSetMapping = "QrCodeTemplateMapping"
)

@NamedNativeQuery(
        name = "QrCodeTemplate.getInUseUrlByOrgId",
        query = "SELECT id,name,qr_code_template_url AS qrCodeTemplateUrl,qr_code_logo_url AS qrCodeLogoUrl,customer_org_id AS customerOrgId," +
                "qr_template_json AS qrTemplateJson,in_use AS inUse, is_default AS isDefault FROM qr_code_template WHERE in_use = ?1 AND customer_org_id = ?2",
        resultSetMapping = "QrCodeTemplateMapping"
)

@NamedNativeQuery(
        name = "QrCodeTemplate.getDefaultTemplate",
        query = "SELECT id,name,qr_code_template_url AS qrCodeTemplateUrl,qr_code_logo_url AS qrCodeLogoUrl,customer_org_id AS customerOrgId," +
                "qr_template_json AS qrTemplateJson,in_use AS inUse, is_default AS isDefault FROM qr_code_template WHERE is_default = 1",
        resultSetMapping = "QrCodeTemplateMapping"
)

@NamedNativeQuery(
        name = "QrCodeTemplate.getDataByIds",
        query = "SELECT id,name,qr_code_template_url AS qrCodeTemplateUrl,qr_code_logo_url AS qrCodeLogoUrl,customer_org_id AS customerOrgId," +
                "qr_template_json AS qrTemplateJson,in_use AS inUse, is_default AS isDefault FROM qr_code_template WHERE id IN ?1",
        resultSetMapping = "QrCodeTemplateMapping"
)

@Getter
@Setter
public class QrCodeTemplate {

    @Id
    private String id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String qrTemplateJson;

    private String qrCodeTemplateUrl;

    private String qrCodeLogoUrl;

    private BigInteger creationTimestamp;

    private String addedBy;

    private BigInteger updatedTimestamp;

    private String updatedBy;

    @Column(columnDefinition = "integer default 0")
    private Integer inUse;

    @Column(columnDefinition = "integer default 0")
    private Integer isDefault;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "customer_org_id")
    private Customer_Organisation customer_org;
}