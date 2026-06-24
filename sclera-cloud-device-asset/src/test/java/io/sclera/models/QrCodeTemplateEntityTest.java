package io.sclera.models;

import io.sclera.dto.QrCodeTemplateDTO;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * POJO round-trip test for QrCodeTemplate entity and its companion DTO.
 * No Spring context / no DB required — validates field wiring and DTO constructor.
 */
class QrCodeTemplateEntityTest {

    @Test
    void entityFieldRoundTrip() {
        QrCodeTemplate t = new QrCodeTemplate();
        t.setId("tmpl-001");
        t.setName("Default Template");
        t.setQrTemplateJson("{\"color\":\"#fff\"}");
        t.setQrCodeTemplateUrl("https://cdn.example.com/template.png");
        t.setQrCodeLogoUrl("https://cdn.example.com/logo.png");
        t.setCustomerOrgId("org-42");
        t.setCreationTimestamp(BigInteger.valueOf(1_700_000_000L));
        t.setAddedBy("admin");
        t.setUpdatedTimestamp(BigInteger.valueOf(1_710_000_000L));
        t.setUpdatedBy("editor");
        t.setInUse(1);
        t.setIsDefault(0);

        assertThat(t.getId()).isEqualTo("tmpl-001");
        assertThat(t.getName()).isEqualTo("Default Template");
        assertThat(t.getQrTemplateJson()).isEqualTo("{\"color\":\"#fff\"}");
        assertThat(t.getQrCodeTemplateUrl()).isEqualTo("https://cdn.example.com/template.png");
        assertThat(t.getQrCodeLogoUrl()).isEqualTo("https://cdn.example.com/logo.png");
        assertThat(t.getCustomerOrgId()).isEqualTo("org-42");
        assertThat(t.getCreationTimestamp()).isEqualTo(BigInteger.valueOf(1_700_000_000L));
        assertThat(t.getAddedBy()).isEqualTo("admin");
        assertThat(t.getUpdatedTimestamp()).isEqualTo(BigInteger.valueOf(1_710_000_000L));
        assertThat(t.getUpdatedBy()).isEqualTo("editor");
        assertThat(t.getInUse()).isEqualTo(1);
        assertThat(t.getIsDefault()).isEqualTo(0);
    }

    @Test
    void dtoEightArgConstructorMatchesColumnResultOrder() {
        // Column order in @ConstructorResult: id, name, qrCodeTemplateUrl, qrCodeLogoUrl,
        //   customerOrgId, qrTemplateJson, inUse, isDefault
        QrCodeTemplateDTO dto = new QrCodeTemplateDTO(
                "tmpl-002", "Template B",
                "https://cdn.example.com/t2.png",
                "https://cdn.example.com/logo2.png",
                "org-99",
                "{\"size\":200}",
                0, 1
        );

        assertThat(dto.getId()).isEqualTo("tmpl-002");
        assertThat(dto.getName()).isEqualTo("Template B");
        assertThat(dto.getQrCodeTemplateUrl()).isEqualTo("https://cdn.example.com/t2.png");
        assertThat(dto.getQrCodeLogoUrl()).isEqualTo("https://cdn.example.com/logo2.png");
        assertThat(dto.getCustomerOrgId()).isEqualTo("org-99");
        assertThat(dto.getQrTemplateJson()).isEqualTo("{\"size\":200}");
        assertThat(dto.getInUse()).isEqualTo(0);
        assertThat(dto.getIsDefault()).isEqualTo(1);
    }

    @Test
    void dtoSettersWork() {
        QrCodeTemplateDTO dto = new QrCodeTemplateDTO();
        dto.setCreationTimestamp(BigInteger.valueOf(9999L));
        dto.setAddedBy("tester");
        dto.setUpdatedTimestamp(BigInteger.valueOf(10_000L));
        dto.setUpdatedBy("reviewer");
        dto.setTemplateName("tpl");
        dto.setCompanyName("Acme");

        assertThat(dto.getCreationTimestamp()).isEqualTo(BigInteger.valueOf(9999L));
        assertThat(dto.getAddedBy()).isEqualTo("tester");
        assertThat(dto.getUpdatedTimestamp()).isEqualTo(BigInteger.valueOf(10_000L));
        assertThat(dto.getUpdatedBy()).isEqualTo("reviewer");
        assertThat(dto.getTemplateName()).isEqualTo("tpl");
        assertThat(dto.getCompanyName()).isEqualTo("Acme");
    }
}
