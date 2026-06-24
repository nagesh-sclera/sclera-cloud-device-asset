package io.sclera.model;

import io.sclera.dto.DockerDTO;
import io.sclera.dto.DockerSyncDTO;
import io.sclera.model.compositeclass.DockerIds;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigInteger;


@Entity
@IdClass(DockerIds.class)

@SqlResultSetMapping(
        name = "dockermapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerDTO.class,
                        columns = {
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "network_name", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "gateway", type = String.class),
                                @ColumnResult(name = "host", type = Boolean.class),
                                @ColumnResult(name = "public_ip_address", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "internet_status", type = Boolean.class),
                                @ColumnResult(name = "internet_required", type = Boolean.class),
                                @ColumnResult(name = "internet_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "is_block", type = Boolean.class),
                                @ColumnResult(name = "block_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "invitee_org_id", type = String.class),
                                @ColumnResult(name = "invite_status", type = String.class)
                        }
                )
        }
)


@SqlResultSetMapping(
        name = "dockersyncmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerSyncDTO.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "vendor_sync", type = Integer.class),
                                @ColumnResult(name = "vendor_transfer", type = Integer.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "inviteeorganisationidmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerDTO.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "network_name", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "invitee_org_id", type = String.class),
                                @ColumnResult(name = "invite_status", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Docker.getAllDockersByInviteeOrganisationIdAndVdmsId",
        query = "SELECT d.name ,d.network_name ,d.vdms_id ,d.system_type ,d.invitee_org_id ,d.invite_status FROM docker d WHERE d.invitee_org_id = ?1 " +
                "AND d.vdms_id = ?2 " +
                "AND (d.invite_status IS NULL OR d.invite_status = 'invited')",
        resultSetMapping = "inviteeorganisationidmapping"
)

@NamedNativeQuery(
        name = "Docker.getDockerSyncByVdmsId",
        query = "SELECT d.name ,d.vendor_sync ,d.vendor_transfer FROM docker d WHERE d.vdms_id = ?1",
        resultSetMapping = "dockersyncmapping"
)


@NamedNativeQuery(
        name = "Docker.getDockerInfoByVdmsIdAndDockerName",
        query = "SELECT d.name AS docker_name ,d.network_name ,d.vdms_id ,d.gateway ,d.host ,d.public_ip_address ,d.mac_address ,d.system_type ,"
                + "d.internet_status ,d.internet_required ,d.internet_timestamp ,d.is_block ,d.block_timestamp ,"
                + " d.invitee_org_id ,d.invite_status "
                + "FROM docker d WHERE d.vdms_id = ?1 AND d.name = ?2",
        resultSetMapping = "dockermapping"
)


@NamedNativeQuery(
        name = "Docker.getAllDockersByVdmsId",
        query = "SELECT d.name AS docker_name ,d.network_name,d.vdms_id ,d.gateway ,d.host ,d.public_ip_address ,d.mac_address ,d.system_type ,"
                + "d.internet_status ,d.internet_required ,d.internet_timestamp ,d.is_block ,d.block_timestamp ,"
                + " d.invitee_org_id ,d.invite_status "
                + "FROM docker d WHERE d.vdms_id = ?1",
        resultSetMapping = "dockermapping"
)

@Getter
@Setter
public class Docker {

    @Id
    private String name;

    @MapsId
    @ManyToOne
    private Vdms vdms;

    @Column(length = 15)
    private String gateway;

    @Column(length = 1)
    private Boolean host;

    @Column(length = 15)
    private String external_ip_address;

    @Column(length = 17)
    private String mac_address;

    @Column(length = 32)
    private String system_type;

    @Column(length = 1)
    private Boolean internet_status;

    @Column(length = 1)
    private Boolean internet_required;

    private BigInteger internet_timestamp;

    @Column(length = 1)
    private Boolean is_block;

    private BigInteger block_timestamp;

    @Column(length = 5)
    private Integer cidr;

    @Column(length = 15)
    private String primary_dns;

    @Column(length = 15)
    private String secondary_dns;

    @Column(length = 16)
    private Integer vlan_id;

    @Column(length = 15)
    private String interface_in;

    @Column(length = 15)
    private String interface_out;

    @Column(length = 1)
    private Boolean isStatic;

    @Column(length = 1)
    private Boolean isTagged;

    @Column(length = 32)
    private String macvlan_name;

    @Column(length = 15)
    private String public_ip_address;

    @Column(length = 15)
    private String internal_ip_address;

    @Column(length = 64)
    private String vendor_org_id;

    @Column(length = 16)
    private String approval_status;

    @Column(columnDefinition = "integer default 0")
    private Integer vendor_sync;

    @Column(columnDefinition = "integer default 0")
    private Integer vendor_transfer;

    @Column(length = 64)
    private String invitee_org_id;

    @Column(length = 8)
    private String invite_status;

    @Column(length = 64)
    private String primary_proxy_profile_id;

    @Column(length = 128)
    private String network_name;

}
