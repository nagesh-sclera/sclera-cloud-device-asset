package io.sclera.model;

import io.sclera.dto.NfcDTO;

import jakarta.persistence.*;
import java.math.BigInteger;


@SqlResultSetMapping(
        name = "nfcDataMapping",
        classes = {
                @ConstructorResult(
                        targetClass = NfcDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
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

@NamedNativeQuery(
        name = "NFC.getNfcDetailsByDeviceIdAndVdmsId",
        query = "SELECT id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM nfc WHERE device_id =?1 AND vdms_id = ?2",
        resultSetMapping = "nfcDataMapping"
)

@NamedNativeQuery(
        name = "NFC.getNfcDetailsByLocationIdAndVdmsId",
        query = "SELECT id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM nfc WHERE location_id =?1 AND vdms_id = ?2",
        resultSetMapping = "nfcDataMapping"
)

@NamedNativeQuery(
        name = "NFC.getNfcDetailsByVdmsIdTaggedByDevice",
        query = "SELECT id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM nfc WHERE vdms_id = ?1 AND device_id IS NOT NULL",
        resultSetMapping = "nfcDataMapping"
)

@NamedNativeQuery(
        name = "NFC.getNfcDetailsByVdmsIdTaggedByLocation",
        query = "SELECT id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM nfc WHERE vdms_id = ?1 AND location_id IS NOT NULL",
        resultSetMapping = "nfcDataMapping"
)

@NamedNativeQuery(
        name = "NFC.getNfcDetailsByDeviceIds",
        query = "SELECT id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM nfc WHERE vdms_id = ?1 AND device_id IN ?2",
        resultSetMapping = "nfcDataMapping"
)

@NamedNativeQuery(
        name = "NFC.getNfcDetailsByLocationIds",
        query = "SELECT id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM nfc WHERE vdms_id = ?1 AND location_id IN ?2",
        resultSetMapping = "nfcDataMapping"
)

@NamedNativeQuery(
        name = "NFC.getNfcRecordsByVdmsId",
        query = "SELECT id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId FROM nfc WHERE vdms_id = ?1 " +
                "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "nfcDataMapping"
)

//nfc sync changes
@NamedNativeQuery(
        name = "NFC.getNfcSyncRecordsByVdmsId",
        query = "SELECT id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId,nfc_sync_status AS nfcSyncStatus FROM nfc WHERE vdms_id = ?1 AND nfc_sync_status = 1 " +
                "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "nfcDataMapping"
)
//nfc sync changes
@NamedNativeQuery(
        name = "NFC.getNfcDetailsById",
        query = "SELECT id,uid,location_id As locationId,device_id as deviceId,creation_time AS creationTime , created_by As createdBy, vdms_id As vdmsId,nfc_sync_status AS nfcSyncStatus FROM nfc WHERE id = ?1 " +
                "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "nfcDataMapping"
)

@Entity
public class NFC {
    @Id
    private String id;
    private String uid;
    private String locationId;
    private String deviceId;
    @Column(columnDefinition = "bigint default 0")
    private BigInteger creation_time;
    private String created_by;
    @ManyToOne
    private Vdms vdms;

    @Column(columnDefinition = "integer default 0")
    private Integer nfcSyncStatus;
}
