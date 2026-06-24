package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientBarCodeDTO {

    private String id;
    private String clientBarCodeId;
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
    private Integer isTagged;
    private Boolean isAdc;

    public ClientBarCodeDTO(String id, String clientBarCodeId, String locationId, String deviceId, String vdmsId,
                            String createdBy, BigInteger updated_time, String updated_by, BigInteger creationTime) {
        this.id = id;
        this.clientBarCodeId = clientBarCodeId;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.vdmsId = vdmsId;
        this.createdBy = createdBy;
        this.updated_time = updated_time;
        this.updated_by = updated_by;
        this.creationTime = creationTime;
    }

    public ClientBarCodeDTO(String id, String locationId, String deviceId, String vdmsId, String createdBy,
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

    public ClientBarCodeDTO(String id, String addedAt, String addedBy, String batchId, String clientBarCodeId, String deviceId, String locationId,
                            BigInteger updatedTime, String updatedBy, String vdmsId) {
        this.id = id;
        this.addedAt = addedAt;
        this.addedBy = addedBy;
        this.batchId = batchId;
        this.clientBarCodeId = clientBarCodeId;
        this.deviceId = deviceId;
        this.locationId = locationId;
        this.updatedTime = updatedTime;
        this.updatedBy = updatedBy;
        this.vdmsId = vdmsId;
    }
}
