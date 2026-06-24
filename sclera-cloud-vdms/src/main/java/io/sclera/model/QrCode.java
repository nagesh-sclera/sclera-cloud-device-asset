package io.sclera.model;

import io.sclera.dto.QrCodeDTO;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@SqlResultSetMapping(
        name = "qrCodeMapping",
        classes = {
                @ConstructorResult(
                        targetClass = QrCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "imageUrl", type = String.class),
                                @ColumnResult(name = "locationId", type = String.class),
                                @ColumnResult(name = "deviceId", type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class),
                                @ColumnResult(name = "createdBy", type = String.class),
                                @ColumnResult(name = "updatedTime", type = String.class),
                                @ColumnResult(name = "updatedBy", type = String.class),
                                @ColumnResult(name = "creationTime", type = String.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "qrCodeDataMapping",
        classes = {
                @ConstructorResult(
                        targetClass = QrCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "locationId", type = String.class),
                                @ColumnResult(name = "deviceId", type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "qrCodeSyncMapping",
        classes = {
                @ConstructorResult(
                        targetClass = QrCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "imageUrl", type = String.class),
                                @ColumnResult(name = "createdBy", type = String.class),
                                @ColumnResult(name = "locationId", type = String.class),
                                @ColumnResult(name = "deviceId", type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class),
                                @ColumnResult(name = "qrCodeLink", type = String.class),
                                @ColumnResult(name = "updatedTime", type = String.class),
                                @ColumnResult(name = "updatedBy", type = String.class),
                                @ColumnResult(name = "creationTime", type = String.class),
                                @ColumnResult(name = "batchId", type = String.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "qrCodeRecordsMapping",
        classes = {
                @ConstructorResult(
                        targetClass = QrCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "locationId", type = String.class),
                                @ColumnResult(name = "deviceId", type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class),
                                @ColumnResult(name = "qrCodeLink", type = String.class),
                                @ColumnResult(name = "updatedTime", type = String.class),
                                @ColumnResult(name = "updatedBy", type = String.class),
                                @ColumnResult(name = "creationTime", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "QrCode.getQrCodeDetailsByQrCodeId",
        query = "SELECT id,image_url as imageUrl,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,created_by AS createdBy, " +
                " creation_time AS creationTime,updated_time AS updatedTime, updated_by AS updatedBy FROM qr_code WHERE id=?1",
        resultSetMapping = "qrCodeMapping"
)

@NamedNativeQuery(
        name = "QrCode.getQrCodeDetailsByVdmsIdAndDeviceId",
        query = "SELECT id,image_url as imageUrl,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,created_by AS createdBy, " +
                "updated_time AS updatedTime, updated_by AS updatedBy,creation_time AS creationTime FROM qr_code WHERE vdms_id = ?1 AND device_id=?2",
        resultSetMapping = "qrCodeMapping"
)


@NamedNativeQuery(
        name = "QrCode.getQrCodeDetailsByLocationId",
        query = "SELECT id,image_url as imageUrl,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,created_by AS createdBy, " +
                "updated_time AS updatedTime, updated_by AS updatedBy,creation_time AS creationTime FROM qr_code WHERE location_id=?1",
        resultSetMapping = "qrCodeMapping"
)

@NamedNativeQuery(
        name = "QrCode.getQrCodeDetailsByDeviceIds",
        query = "SELECT id,image_url as imageUrl,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,created_by AS createdBy, " +
                "updated_time AS updatedTime, updated_by AS updatedBy,creation_time AS creationTime FROM qr_code WHERE vdms_id = ?1 AND device_id IN ?2 ",
        resultSetMapping = "qrCodeMapping"
)

@NamedNativeQuery(
        name = "QrCode.getQrCodeDetailsByLocationIds",
        query = "SELECT id,image_url as imageUrl,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,created_by AS createdBy, " +
                "updated_time AS updatedTime, updated_by AS updatedBy,creation_time AS creationTime FROM qr_code WHERE vdms_id = ?1 AND location_id IN ?2",
        resultSetMapping = "qrCodeMapping"
)

@NamedNativeQuery(
        name = "QrCode.getTaggedDevicesByVdmsId",
        query = "SELECT id,image_url AS  imageUrl,location_id As locationId, device_id As deviceId,vdms_id As vdmsId ,created_by As createdBy, " +
                "updated_time AS updatedTime, updated_by AS updatedBy,creation_time As creationTime From qr_code WHERE vdms_id = ?1 AND device_id IS NOT NULL",
        resultSetMapping = "qrCodeMapping"
)

@NamedNativeQuery(
        name = "QrCode.getTaggedLocationsByVdmsId",
        query = "SELECT id,image_url AS  imageUrl,location_id As locationId, device_id As deviceId,vdms_id As vdmsId ,created_by As createdBy, " +
                "updated_time AS updatedTime, updated_by AS updatedBy, creation_time As creationTime From qr_code WHERE vdms_id = ?1 AND location_id IS NOT NULL",
        resultSetMapping = "qrCodeMapping"
)

@NamedNativeQuery(
        name = "QrCode.getQrCodeDetailsByVdmsIdAndLocationId",
        query = "SELECT id,image_url as imageUrl,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,created_by AS createdBy, " +
                "updated_time AS updatedTime, updated_by AS updatedBy, creation_time AS creationTime FROM qr_code WHERE vdms_id = ?1 AND location_id=?2",
        resultSetMapping = "qrCodeMapping"
)

@NamedNativeQuery(
        name = "QrCode.getVdmsInfoByQrCodeId",
        query = "SELECT location_id As locationId,device_id as deviceId,vdms_id as vdmsId FROM qr_code WHERE id=?1 ",
        resultSetMapping = "qrCodeDataMapping"
)

@NamedNativeQuery(
        name = "QrCode.getAllQrCodeByVdmsId",
        query = "SELECT id,image_url As imageUrl,created_by AS createdBy, location_id AS locationId, device_id AS deviceId," +
                "vdms_id AS vdmsId, qr_code_link AS qrCodeLink, " +
                "updated_time AS updatedTime, updated_by AS updatedBy, creation_time AS creationTime, batch_id AS batchId FROM qr_code WHERE vdms_id = ?1 LIMIT ?2 OFFSET ?3",
        resultSetMapping = "qrCodeSyncMapping"
)

@NamedNativeQuery(
        name = "QrCode.getQrCodeRecordsByVdmsIdAndLastSyncTime",
        query = "SELECT id, location_id AS locationId, device_id AS deviceId, vdms_id AS vdmsId, qr_code_link AS qrCodeLink, " +
                "updated_time AS updatedTime, updated_by AS updatedBy, creation_time AS creationTime FROM qr_code WHERE vdms_id = ?1 AND (creation_time >= ?2 OR updated_time >= ?2)  " +
                "LIMIT ?3 OFFSET ?4",
        resultSetMapping = "qrCodeRecordsMapping"
)

// qr code sync changes
@NamedNativeQuery(
        name = "QrCode.getAllSyncQrCodeByVdmsId",
        query = "SELECT id,image_url As imageUrl,created_by AS createdBy, location_id AS locationId, device_id AS deviceId," +
                "vdms_id AS vdmsId, qr_code_link AS qrCodeLink, " +
                "updated_time AS updatedTime, updated_by AS updatedBy, creation_time AS creationTime, batch_id AS batchId FROM qr_code WHERE vdms_id = ?1 AND qr_code_sync = 1  LIMIT ?2 OFFSET ?3",
        resultSetMapping = "qrCodeSyncMapping"
)

@Getter
@Setter
public class QrCode {

    @Id
    private String id;
    private String imageUrl;

    @Column(columnDefinition = "bigint default 0")
    private BigInteger creation_time;

    private String created_by;
    private String locationId;
    private String deviceId;
    private String qr_code_link;
    private String updated_time;
    private String batch_id;
    private String updated_by;

    @ManyToOne
    private Vdms vdms;

    @Column(columnDefinition = "integer default 0")
    private Integer qrCodeSync;

    @Column(columnDefinition = "integer default 0")
    private Integer adcQrCodeCheck;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "customer_org_id")
    private Customer_Organisation customer_org;
}

