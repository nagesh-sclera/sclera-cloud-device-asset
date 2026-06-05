package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Aggregates asset classification, location, and tag (QR/barcode/NFC) details for a device collection entry.
 * Used to present consolidated collection records in the asset-management API.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectionDTO {
    private String id;
    private String systemTypeName;
    private String assetTypeName;
    private String assetSubTypeName;
    private String locationName;
    private String value;
    private String createdBy;
    private String assignee;
    private String systemTypeId;
    private String assetTypeId;
    private String assetSubTypeId;
    private String locationId;
    private String networkName;
    private String qrCodeData;
    private String barCodeData;
    private String nfcData;
    private String qrCodeUpdatedBy;
    private String barCodeUpdatedBy;
    private String nfcUpdatedBy;
}