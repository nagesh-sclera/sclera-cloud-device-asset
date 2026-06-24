package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientNfcDTO {

    private String id;

    @JsonAlias("nfcId")
    private String nfc_id;

    private String uid;
    private String locationId;
    private String deviceId;
    private BigInteger creationTime;
    private String createdBy;
    private String vdmsId;
    private Integer is_validated=0;
    private String validationMessage;
    private Integer isTagged;
    private Boolean isAdc;
    private String updatedBy;
    private String clientNfcSync;

    public ClientNfcDTO(String id, String nfc_id, String uid, String locationId, String deviceId, BigInteger creationTime, String createdBy, String vdmsId, String clientNfcSync) {
        this.id = id;
        this.nfc_id = nfc_id;
        this.uid = uid;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.creationTime = creationTime;
        this.createdBy = createdBy;
        this.vdmsId = vdmsId;
        this.clientNfcSync = clientNfcSync;
    }

    public ClientNfcDTO(String id, String nfc_id, String uid, String locationId, String deviceId, BigInteger creationTime, String createdBy, String vdmsId) {
        this.id = id;
        this.nfc_id = nfc_id;
        this.uid = uid;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.creationTime = creationTime;
        this.createdBy = createdBy;
        this.vdmsId = vdmsId;
    }
}
