package io.sclera.model;


import io.sclera.dto.ClientBarCodeDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity

@SqlResultSetMapping(
        name = "clientBarCodeSyncMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ClientBarCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "addedAt", type = String.class),
                                @ColumnResult(name = "addedBy", type = String.class),
                                @ColumnResult(name = "batchId", type = String.class),
                                @ColumnResult(name = "clientBarCodeId", type = String.class),
                                @ColumnResult(name = "deviceId", type = String.class),
                                @ColumnResult(name = "locationId", type = String.class),
                                @ColumnResult(name = "updatedAt", type = BigInteger.class),
                                @ColumnResult(name = "updatedBy", type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "clientBarCodeMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ClientBarCodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "clientBarCodeId", type = String.class),
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
        name = "clientBarCodeDetailsMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ClientBarCodeDTO.class,
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


@NamedNativeQuery(
        name = "ClientBarCode.getAllClientBarCodeByVdmsId",
        query = "SELECT id,added_at AS addedAt, added_by AS addedBy, batch_id As batchId, client_bar_code_id as clientBarCodeId," +
                "device_id as deviceId,location_id As locationId,updated_at AS updatedAt, updated_by AS updatedBy," +
                "vdms_id as vdmsId FROM client_bar_code WHERE vdms_id=?1  LIMIT ?2 OFFSET ?3",
        resultSetMapping = "clientBarCodeSyncMapping"
)

@NamedNativeQuery(
        name = "ClientBarCode.getClientBarCodeDetailsByClientBarCodeId",
        query = "SELECT id,client_bar_code_id as clientBarCodeId,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_bar_code WHERE client_bar_code_id=?1",
        resultSetMapping = "clientBarCodeMapping"
)

@NamedNativeQuery(
        name = "ClientBarCode.getBarCodeDataByClientBarCodeId",
        query = "SELECT id,client_bar_code_id as clientBarCodeId,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_bar_code WHERE client_bar_code_id = ?1 ",
        resultSetMapping = "clientBarCodeMapping"
)

@NamedNativeQuery(
        name = "ClientBarCode.getClientBarCodeDetailsByVdmsIdAndDeviceId",
        query = "SELECT client_bar_code_id as id,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_bar_code WHERE vdms_id = ?1 AND device_id=?2",
        resultSetMapping = "clientBarCodeDetailsMapping"
)

@NamedNativeQuery(
        name = "ClientBarCode.getClientBarCodeDetailsByVdmsIdAndLocationId",
        query = "SELECT client_bar_code_id as id,location_id As locationId,device_id as deviceId,vdms_id as vdmsId,added_by AS createdBy, " +
                "updated_at AS updatedTime, updated_by AS updatedBy,added_at AS creationTime FROM client_bar_code WHERE vdms_id = ?1 AND location_id=?2",
        resultSetMapping = "clientBarCodeDetailsMapping"
)

@NamedNativeQuery(
        name = "ClientBarCode.getBarCodeRecordsByVdmsIdAndLastSyncTime",
        query = "SELECT id,added_at AS addedAt, added_by AS addedBy, batch_id As batchId, client_bar_code_id as clientBarCodeId," +
                "device_id as deviceId,location_id As locationId,updated_at AS updatedAt, updated_by AS updatedBy," +
                "vdms_id as vdmsId FROM client_bar_code WHERE vdms_id=?1 AND (updated_at >= ?2 OR added_at >= ?2) " +
                "LIMIT ?3 OFFSET ?4",
        resultSetMapping = "clientBarCodeSyncMapping"
)

@NamedNativeQuery(
        name = "ClientBarCode.getSyncedClientBarCodeByVdmsId",
        query = "SELECT id,added_at AS addedAt, added_by AS addedBy, batch_id As batchId, client_bar_code_id as clientBarCodeId," +
                "device_id as deviceId,location_id As locationId,updated_at AS updatedAt, updated_by AS updatedBy," +
                "vdms_id as vdmsId FROM client_bar_code WHERE vdms_id=?1 AND bar_code_sync=1   LIMIT ?2 OFFSET ?3",
        resultSetMapping = "clientBarCodeSyncMapping"
)


@Getter
@Setter
public class ClientBarCode {
    @Id
    private String id;
    private String clientBarCodeId;
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
    private Integer barCodeSync;

    @Column(columnDefinition = "integer default 0")
    private Integer adcBarCodeCheck;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "customer_org_id")
    private Customer_Organisation customer_org;
}
