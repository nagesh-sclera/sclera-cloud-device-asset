package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientQrCodeDTO {

    private String id;
    private String clientQrCodeId;
    private String locationId;
    private String deviceId;
    private String vdmsId;
    private String createdBy;
    public BigInteger updated_time;
    private String updated_by;
    private BigInteger creationTime;
    private String addedAt;
    private String addedBy;
    private String batchId;
    public BigInteger updatedTime;
    private String updatedBy;
    private Integer is_validated=0;
    private String validationMessage;
    private Integer isTagged;
    private Boolean isAdc;

    public ClientQrCodeDTO(String id, String clientQrCodeId, String locationId, String deviceId, String vdmsId,
                           String createdBy, BigInteger updated_time, String updated_by, BigInteger creationTime) {
        this.id = id;
        this.clientQrCodeId = clientQrCodeId;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.vdmsId = vdmsId;
        this.createdBy = createdBy;
        this.updated_time = updated_time;
        this.updated_by = updated_by;
        this.creationTime = creationTime;
    }

    public ClientQrCodeDTO(String id, String locationId, String deviceId, String vdmsId, String createdBy,
                           BigInteger updated_time, String updated_by, BigInteger creationTime) {
        this.id = id;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.vdmsId = vdmsId;
        this.createdBy = createdBy;
        this.updated_time = updated_time;
        this.updated_by = updated_by;
        this.creationTime = creationTime;
    }

    public ClientQrCodeDTO(String id, String addedAt, String addedBy, String batchId, String clientQrCodeId, String deviceId, String locationId,
                           BigInteger updatedTime, String updatedBy, String vdmsId) {
        this.id = id;
        this.addedAt = addedAt;
        this.addedBy = addedBy;
        this.batchId = batchId;
        this.clientQrCodeId = clientQrCodeId;
        this.deviceId = deviceId;
        this.locationId = locationId;
        this.updatedTime = updatedTime;
        this.updatedBy = updatedBy;
        this.vdmsId = vdmsId;
    }
}
