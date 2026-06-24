package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrCodeDTO {

    private String id;
    private String imageUrl;
    private String locationId;
    private String deviceId;
    private String vdmsId;
    private byte[] qrCodeContent;
    private String createdBy;
    private String qr_code_link;
    private String type;
    public String updated_time;
    private String updated_by;
    private String creationTime;
    public String updatedTime;
    private String updatedBy;
    private String batch_id;
    private Integer isTagged;
    private Boolean isAdc;

    public QrCodeDTO(String id, String imageUrl, String qr_code_link, byte[] qrCodeContent) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.qr_code_link = qr_code_link;
        this.qrCodeContent = qrCodeContent;
    }

    public QrCodeDTO(String id, String imageUrl, String locationId, String deviceId, String vdmsId) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.vdmsId = vdmsId;
    }

    public QrCodeDTO(String id, String imageUrl, String locationId, String deviceId, String vdmsId, String createdBy, String creationTime) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.vdmsId = vdmsId;
        this.createdBy = createdBy;
        this.creationTime = creationTime;
    }

    public QrCodeDTO(String locationId, String deviceId, String vdmsId) {
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.vdmsId = vdmsId;
    }

    public QrCodeDTO(String id, String imageUrl, String locationId, String deviceId, String vdmsId, String createdBy, String updated_time, String updated_by, String creationTime) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.vdmsId = vdmsId;
        this.createdBy = createdBy;
        this.updated_time = updated_time;
        this.updated_by = updated_by;
        this.creationTime = creationTime;
    }

    public QrCodeDTO(String id, String imageUrl, String createdBy, String locationId, String deviceId, String vdmsId,
                     String qr_code_link, String updatedTime, String updatedBy, String creationTime, String batch_id) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.createdBy = createdBy;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.vdmsId = vdmsId;
        this.qr_code_link = qr_code_link;
        this.updatedTime = updatedTime;
        this.updatedBy = updatedBy;
        this.creationTime = creationTime;
        this.batch_id = batch_id;
    }

    public QrCodeDTO(String id, String locationId, String deviceId, String vdmsId, String qr_code_link, String updated_time, String updated_by, String creationTime) {
        this.id = id;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.vdmsId = vdmsId;
        this.qr_code_link = qr_code_link;
        this.updated_time = updated_time;
        this.updated_by = updated_by;
        this.creationTime = creationTime;
    }


    @Override
    public String toString() {
        return "QrCodeDTO{" +
                "id='" + id + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", locationId='" + locationId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", vdmsId='" + vdmsId + '\'' +
                '}';
    }

}
