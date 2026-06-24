package io.sclera.model;


import io.sclera.dto.ClientNfcDTO;
import jakarta.persistence.*;

import java.math.BigInteger;


@SqlResultSetMapping(
        name = "clientNfcDataMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ClientNfcDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "nfc_id", type = String.class),
                                @ColumnResult(name ="uid",type = String.class),
                                @ColumnResult(name = "locationId",type = String.class),
                                @ColumnResult(name = "deviceId",type = String.class),
                                @ColumnResult(name = "creationTime" , type = BigInteger.class),
                                @ColumnResult(name = "createdBy" , type = String.class),
                                @ColumnResult(name = "vdmsId" , type = String.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "clientNfcMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ClientNfcDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "nfc_id", type = String.class),
                                @ColumnResult(name ="uid",type = String.class),
                                @ColumnResult(name = "locationId",type = String.class),
                                @ColumnResult(name = "deviceId",type = String.class),
                                @ColumnResult(name = "creationTime" , type = BigInteger.class),
                                @ColumnResult(name = "createdBy" , type = String.class),
                                @ColumnResult(name = "vdmsId" , type = String.class),
                                @ColumnResult(name = "clientNfcSync" , type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "ClientNfc.getClientNfcDetailsByDeviceIdAndVdmsId",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM client_nfc WHERE device_id =?1 AND vdms_id = ?2",
        resultSetMapping = "clientNfcDataMapping"
)

@NamedNativeQuery(
        name = "ClientNfc.getClientNfcDetailsByLocationIdAndVdmsId",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM client_nfc WHERE location_id =?1 AND vdms_id = ?2",
        resultSetMapping = "clientNfcDataMapping"
)

@NamedNativeQuery(
        name = "ClientNfc.getClientNfcDetailsByVdmsIdTaggedByDevice",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM client_nfc WHERE vdms_id = ?1 AND device_id IS NOT NULL",
        resultSetMapping = "clientNfcDataMapping"
)

@NamedNativeQuery(
        name = "ClientNfc.getClientNfcDetailsByVdmsIdTaggedByLocation",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM client_nfc WHERE vdms_id = ?1 AND location_id IS NOT NULL",
        resultSetMapping = "clientNfcDataMapping"
)

@NamedNativeQuery(
        name = "ClientNfc.getClientNfcDetailsByDeviceIds",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM client_nfc WHERE vdms_id = ?1 AND device_id IN ?2",
        resultSetMapping = "clientNfcDataMapping"
)

@NamedNativeQuery(
        name = "ClientNfc.getClientNfcDetailsByLocationIds",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM client_nfc WHERE vdms_id = ?1 AND location_id IN ?2",
        resultSetMapping = "clientNfcDataMapping"
)

@NamedNativeQuery(
        name = "ClientNfc.getClientNfcDetailsByVdmsIdAndDeviceIds",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM client_nfc WHERE  vdms_id = ?1 AND device_id IN ?2 ",
        resultSetMapping = "clientNfcDataMapping"
)


@NamedNativeQuery(
        name = "ClientNfc.getClientNfcDetailsByVdmsIdAndLocationIds",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM client_nfc WHERE vdms_id = ?1 AND location_id IN ?2",
        resultSetMapping = "clientNfcDataMapping"
)


@NamedNativeQuery(
        name = "ClientNfc.getClientNfcDetailsByNfcId",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM client_nfc WHERE nfc_id =?1",
        resultSetMapping = "clientNfcDataMapping"
)

@NamedNativeQuery(
        name = "ClientNfc.getClientNfcRecordsByVdmsId",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM client_nfc WHERE vdms_id =?1 " +
                "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "clientNfcDataMapping"
)

@NamedNativeQuery(
        name = "ClientNfc.getSyncClientNfcRecordsByVdmsId",
        query = "SELECT id,nfc_id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId,client_nfc_sync AS clientNfcSync FROM client_nfc WHERE vdms_id =?1 AND client_nfc_sync = 1 " +
                "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "clientNfcMapping"
)

@Entity
public class ClientNfc {

    @Id
    private String id;
    private String nfc_id;
    private String uid;
    private String locationId;
    private String deviceId;
    @Column(columnDefinition = "bigint default 0")
    private BigInteger creation_time;
    private String created_by;
    @ManyToOne
    private Vdms vdms;
    private String batchId;

    @Column(columnDefinition = "integer default 0")
    private Integer clientNfcSync;

    @Column(columnDefinition = "integer default 0")
    private Integer adcClientNfcCheck;

    private BigInteger updatedAt;
    private String updatedBy;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "customer_org_id")
    private Customer_Organisation customer_org;
}
