package io.sclera.model;

import io.sclera.dto.ClientQrCodeDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity

@SqlResultSetMapping(
        name = "clientQrCodeMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ClientQrCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "clientQrCodeId", type = String.class),
                                @ColumnResult(name = "locationId", type = String.class),
                                @ColumnResult(name = "deviceId", type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class),
                                @ColumnResult(name = "createdBy", type = String.class),
                                @ColumnResult(name = "updatedTime", type = BigInteger.class),
                                @ColumnResult(name = "updatedBy", type = String.class),
                                @ColumnResult(name = "creationTime", type = BigInteger.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "clientQrCodeDetailsMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ClientQrCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "locationId", type = String.class),
                                @ColumnResult(name = "deviceId", type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class),
                                @ColumnResult(name = "createdBy", type = String.class),
                                @ColumnResult(name = "updatedTime", type = BigInteger.class),
                                @ColumnResult(name = "updatedBy", type = String.class),
                                @ColumnResult(name = "creationTime", type = BigInteger.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "clientQrCodeSyncMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ClientQrCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "addedAt", type = String.class),
                                @ColumnResult(name = "addedBy", type = String.class),
                                @ColumnResult(name = "batchId", type = String.class),
                                @ColumnResult(name = "clientQrCodeId", type = String.class),
                                @ColumnResult(name = "deviceId", type = String.class),
                                @ColumnResult(name = "locationId", type = String.class),
                                @ColumnResult(name = "updatedAt", type = BigInteger.class),
                                @ColumnResult(name = "updatedBy", type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class)
                        }
                )
        }
)

//@NamedNativeQuery(
//        name = "ClientQrCode.getClientQrCodeByVdmsIdAndQrCodeId",
//        query = "SELECT id,client_qr_code_id as clientQrCodeId,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
//                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_qr_code WHERE vdms_id = ?1 AND client_qr_code_id=?2",
//        resultSetMapping = "clientQrCodeMapping"
//)

@NamedNativeQuery(
        name = "ClientQrCode.getClientQrCodeDetailsByVdmsIdAndLocationId",
        query = "SELECT client_qr_code_id as id,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_qr_code WHERE vdms_id = ?1 AND location_id=?2",
        resultSetMapping = "clientQrCodeDetailsMapping"
)

@NamedNativeQuery(
        name = "ClientQrCode.getClientQrCodeDetailsByVdmsIdAndDeviceId",
        query = "SELECT client_qr_code_id as id,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_qr_code WHERE vdms_id = ?1 AND device_id=?2",
        resultSetMapping = "clientQrCodeDetailsMapping"
)

@NamedNativeQuery(
        name = "ClientQrCode.getQrCodeDataByClientQrCodeId",
        query = "SELECT id,client_qr_code_id as clientQrCodeId,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_qr_code WHERE client_qr_code_id = ?1 ",
        resultSetMapping = "clientQrCodeMapping"
)

@NamedNativeQuery(
        name = "ClientQrCode.getClientQrCodeDetailsByClientQrCodeId",
        query = "SELECT id,client_qr_code_id as clientQrCodeId,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_qr_code WHERE client_qr_code_id=?1",
        resultSetMapping = "clientQrCodeMapping"
)

@NamedNativeQuery(
        name = "ClientQrCode.getClientQrCodeDetailsByVdmsIdAndDeviceIds",
        query = "SELECT client_qr_code_id as id,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_qr_code WHERE vdms_id = ?1 AND device_id IN ?2",
        resultSetMapping = "clientQrCodeDetailsMapping"
)

@NamedNativeQuery(
        name = "ClientQrCode.getClientQrCodeDetailsByVdmsIdAndLocationIds",
        query = "SELECT client_qr_code_id as id,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_qr_code WHERE vdms_id = ?1 AND location_id IN ?2",
        resultSetMapping = "clientQrCodeDetailsMapping"
)

@NamedNativeQuery(
        name = "ClientQrCode.getClientQrCodeTaggedDevicesDetailsByVdmsId",
        query = "SELECT client_qr_code_id as id,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_qr_code WHERE vdms_id = ?1 AND device_id IS NOT NULL",
        resultSetMapping = "clientQrCodeDetailsMapping"
)

@NamedNativeQuery(
        name = "ClientQrCode.getClientQrCodeTaggedLocationsDetailsByVdmsId",
        query = "SELECT client_qr_code_id as id,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_qr_code WHERE vdms_id = ?1 AND location_id IS NOT NULL",
        resultSetMapping = "clientQrCodeDetailsMapping"
)

@NamedNativeQuery(
        name = "ClientQrCode.getAllClientQrCodeByVdmsId",
        query = "SELECT id,added_at AS addedAt, added_by AS addedBy, batch_id As batchId, client_qr_code_id as clientQrCodeId," +
                "device_id as deviceId,location_id As locationId,updated_at AS updatedAt, updated_by AS updatedBy," +
                "vdms_id as vdmsId FROM client_qr_code WHERE vdms_id=?1  LIMIT ?2 OFFSET ?3",
        resultSetMapping = "clientQrCodeSyncMapping"
)

@NamedNativeQuery(
        name = "ClientQrCode.getClientQrCodeRecordsByVdmsIdAndLastSyncTime",
        query = "SELECT client_qr_code_id as id,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_qr_code WHERE vdms_id = ?1 AND (updated_at >= ?2 OR added_at >= ?2)" +
                "LIMIT ?3 OFFSET ?4",
        resultSetMapping = "clientQrCodeDetailsMapping"
)

@NamedNativeQuery(
        name = "ClientQrCode.getAllSyncClientQrCodeByVdmsId",
        query = "SELECT id,added_at AS addedAt, added_by AS addedBy, batch_id As batchId, client_qr_code_id as clientQrCodeId," +
                "device_id as deviceId,location_id As locationId,updated_at AS updatedAt, updated_by AS updatedBy," +
                "vdms_id as vdmsId FROM client_qr_code WHERE vdms_id=?1 AND client_qr_code_sync =1  LIMIT ?2 OFFSET ?3",
        resultSetMapping = "clientQrCodeSyncMapping"
)

@Getter
@Setter
public class ClientQrCode {
    @Id
    private String id;
    private String clientQrCodeId;
    private BigInteger addedAt;
    private String addedBy;
    private String locationId;
    private String deviceId;
    private BigInteger updatedAt;
    private String updatedBy;
    private String batchId;
    @ManyToOne
    private Vdms vdms;

    @Column(columnDefinition = "integer default 0")
    private Integer clientQrCodeSync;

    @Column(columnDefinition = "integer default 0")
    private Integer adcClientQrCodeCheck;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "customer_org_id")
    private Customer_Organisation customer_org;
}