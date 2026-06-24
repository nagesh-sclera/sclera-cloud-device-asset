package io.sclera.model;

import io.sclera.dto.ProxyProfileDTO;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity

@SqlResultSetMapping(
        name = "proxyprofilemapping",
        classes = {
                @ConstructorResult(
                        targetClass = ProxyProfileDTO.class,
                        columns = {
                                @ColumnResult(name = "id" , type = String.class),
                                @ColumnResult(name = "name" , type = String.class),
                                @ColumnResult(name = "public_ip" , type = String.class),
                                @ColumnResult(name = "tcp_port" , type = String.class),
                                @ColumnResult(name = "udp_port" , type = String.class),
                                @ColumnResult(name = "host_machine" , type = String.class),
                                @ColumnResult(name = "customer_org_id" , type = String.class),
                                @ColumnResult(name = "is_global" , type = Integer.class),
                                @ColumnResult(name = "ssl_enabled" , type = Integer.class),
                        }
                )
        }
)





@NamedNativeQuery(
        name = "ProxyProfile.getProxyProfileByCustomerOrganisationId",
        query = "SELECT id , name ,public_ip ,tcp_port ,udp_port ,host_machine ,customer_org_id ,is_global ,ssl_enabled FROM proxy_profile WHERE customer_org_id = ?1 OR customer_org_id IS NULL ",
        resultSetMapping = "proxyprofilemapping"
)

@NamedNativeQuery(
        name = "ProxyProfile.getServerProxyProfileByVdmsId",
        query = "SELECT id , name ,public_ip ,tcp_port ,udp_port ,host_machine ,customer_org_id ,is_global ,ssl_enabled FROM proxy_profile WHERE host_machine = ?1 ",
        resultSetMapping = "proxyprofilemapping"
)


@NamedNativeQuery(
        name = "ProxyProfile.getVdmsProxyProfileByVdmsId",
        query = "SELECT pp.id , pp.name ,pp.public_ip ,pp.tcp_port ,pp.udp_port ,pp.host_machine ,pp.customer_org_id ,pp.is_global ,pp.ssl_enabled FROM proxy_profile pp LEFT JOIN vdms v on v.primary_proxy_profile_id = pp.id WHERE v.id = ?1",
        resultSetMapping = "proxyprofilemapping"
)


@NamedNativeQuery(
        name = "ProxyProfile.getProxyProfileByProxyProfileId",
        query = "SELECT id , name ,public_ip ,tcp_port ,udp_port ,host_machine ,customer_org_id ,is_global ,ssl_enabled FROM proxy_profile WHERE id = ?1 ",
        resultSetMapping = "proxyprofilemapping"
)

@NamedNativeQuery(
        name = "ProxyProfile.getGlobalProxyProfiles",
        query = "SELECT id , name ,public_ip ,tcp_port ,udp_port ,host_machine ,customer_org_id ,is_global ,ssl_enabled FROM proxy_profile WHERE is_global = 1 ",
        resultSetMapping = "proxyprofilemapping"
)





@Getter
@Setter
public class ProxyProfile {

    @Id
    private String id;

    @Column(length = 64)
    private String name;

    @Column(length = 16)
    private String public_ip;

    @Column(length = 8)
    private String tcp_port;

    @Column(length = 8)
    private String udp_port;

    @Column(length = 16)
    private String host_machine;

    @Column(length = 4 ,columnDefinition = "integer default 0")
    private Integer ssl_enabled;

    @Column(length = 1 ,columnDefinition = "integer default 0")
    private Integer is_global;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id" , name = "customer_org_id")
    private Customer_Organisation customer_org;

}
