package io.sclera.models;

import io.sclera.dto.DockerInfoDto;
import io.sclera.dto.VendorDTO;
import io.sclera.dto.touchscreen.DockerPhonebookDTO;
import io.sclera.dto.touchscreen.NetworkListDTO;
import io.sclera.dto.touchscreen.settings.DockerDTO;
import io.sclera.dto.touchscreen.settings.NetworkBoundaryConditionsDTO;
import io.sclera.models.compositeclass.DockerIds;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Set;


@Entity
@IdClass(DockerIds.class)
@SqlResultSetMapping(
        name = "dockerinfomapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerInfoDto.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "gateway", type = String.class),
                                @ColumnResult(name = "host", type = Boolean.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "external_ip_address", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),

                                @ColumnResult(name = "internal_ip_address", type = String.class),
                                @ColumnResult(name = "public_ip_address", type = String.class),
                                @ColumnResult(name = "internet_required", type = Boolean.class),
                                @ColumnResult(name = "internet_status", type = String.class),
                                @ColumnResult(name = "internet_timestamp", type = BigInteger.class),

                                @ColumnResult(name = "cidr", type = String.class),
                                @ColumnResult(name = "permission", type = String.class),
                                @ColumnResult(name = "state", type = Integer.class),
                                @ColumnResult(name = "support", type = String.class),

                                @ColumnResult(name = "isp_account_number", type = String.class),
                                @ColumnResult(name = "network_origin", type = Integer.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Docker.getDockerInfobyDockerName",
        query = "SELECT d.name, v.id as vdms_id, d.gateway, d.host, d.mac_address, d.external_ip_address, d.system_type, d.internal_ip_address, d.public_ip_address, d.internet_required, d.internet_status, d.internet_timestamp,d.cidr, d.isp_id AS isp_account_number, d.network_origin, "
                + "r.permission, r.state, r.support "
                + "FROM docker as d "
                + "JOIN vdms as v ON d.vdms_id = v.id "
                + "JOIN user as u ON u.customer_org_id = v.customer_org_id "
                + "LEFT JOIN remote_access as r ON d.name = r.docker_name "
                + "WHERE u.email =?1 AND v.id = ?2 AND d.name =?3",
        resultSetMapping = "dockerinfomapping"
)


//@NamedNativeQuery(
//		name = "Docker.getDockerInfoByVendorOrganisationId",
//		query = "SELECT d.name, v.id as vdms_id, d.gateway, d.host, d.mac_address, d.external_ip_address, d.system_type, d.internal_ip_address, d.public_ip_address, d.internet_required, d.internet_status, d.internet_timestamp, ve.email, d.cidr, d.isp_id AS isp_account_number, "
//				+ "r.permission, r.state, r.support "
//				+ "FROM docker as d "
//				+ "JOIN vdms as v ON d.vdms_id = v.id "
//				+ "LEFT JOIN vendor_organisation vo ON vo.id = d.vendor_org_id "
//				+ "LEFT JOIN vendor ve ON ve.vendor_org_id = vo.id "
//				+ "LEFT JOIN remote_access as r ON d.name = r.docker_name "
//				+ "WHERE d.vendor_org_id = ?1 AND d.vdms_id = ?2 AND d.name =?3",
//				resultSetMapping = "dockerinfomapping"
//		)


@NamedNativeQuery(
        name = "Docker.getDockerInfoByVendorOrganisationId",
        query = "SELECT d.name, v.id as vdms_id, d.gateway, d.host, d.mac_address, d.external_ip_address, d.system_type, d.internal_ip_address, d.public_ip_address, d.internet_required, d.internet_status, d.internet_timestamp, d.cidr, d.isp_id AS isp_account_number, d.network_origin, "
                + "r.permission, r.state, r.support "
                + "FROM docker as d "
                + "JOIN vdms as v ON d.vdms_id = v.id "
                + "LEFT JOIN remote_access as r ON d.name = r.docker_name "
                + "WHERE d.vendor_org_id = ?1 AND d.vdms_id = ?2 AND d.name =?3",
        resultSetMapping = "dockerinfomapping"
)




@SqlResultSetMapping(name = "dockerlistmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerInfoDto.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "approval_status", type = String.class),
                                @ColumnResult(name = "configuration_status", type = String.class),
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "permission", type = String.class),
                                @ColumnResult(name = "support", type = String.class),
                                @ColumnResult(name = "timestamp", type = BigInteger.class)

                        }
                )
        }
)

@NamedNativeQuery (
        name = "Docker.listdocker",
        query = "SELECT d.name , d.approval_status, d.configuration_status, d.system_type, r.permission , ve.email, r.support, r.timestamp"
                + " FROM docker as d "
                + "LEFT JOIN vendor_organisation vo ON vo.id = d.vendor_org_id "
                + "LEFT JOIN vendor ve ON ve.vendor_org_id = vo.id "
                + "LEFT JOIN remote_access as r ON d.name = r.docker_name "
                + "WHERE ve.role = 'master-vendor'",
        resultSetMapping = "dockerlistmapping"
)



@SqlResultSetMapping(name = "dockerlistmappingTS",
        classes = {
                @ConstructorResult(
                        targetClass = DockerInfoDto.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "approval_status", type = String.class),
                                @ColumnResult(name = "configuration_status", type = String.class),
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "permission", type = String.class),
                                @ColumnResult(name = "support", type = String.class),
                                @ColumnResult(name = "timestamp", type = BigInteger.class),
                                @ColumnResult(name = "otp", type = Integer.class)

                        }
                )
        }
)



@NamedNativeQuery (
        name = "Docker.listdockerTS",
        query = "SELECT d.name , d.approval_status, d.configuration_status, d.system_type, r.permission , r.email, r.support, r.timestamp , r.otp "
                + " FROM docker as d "
//				+ "LEFT JOIN vendor_organisation vo ON vo.id = d.vendor_org_id "
//				+ "LEFT JOIN vendor ve ON ve.vendor_org_id = vo.id "
                + "LEFT JOIN remote_access as r ON d.name = r.docker_name "
//				+ "WHERE r.support != 'request' AND ve.role = 'master-vendor'",
                + "WHERE r.support != 'request' ",
        resultSetMapping = "dockerlistmappingTS"
)



// TouchScreen FrontEnd



@SqlResultSetMapping(name = "networklistMapping",
        classes = {
                @ConstructorResult(
                        targetClass = NetworkListDTO.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "vendor_org_id", type = String.class),
                                @ColumnResult(name = "external_ip_address", type = String.class),

                        }
                )
        }
)

@NamedNativeQuery (
        name = "Docker.getNetworkList",
        query = "SELECT d.name , d.system_type, d.vendor_org_id, d.external_ip_address"
                + " FROM docker as d",
        resultSetMapping = "networklistMapping"
)








// Touchscreen query Settings ************************************************************************************


//@SqlResultSetMapping(
//		name = "networklistmapping",
//		classes = {
//				@ConstructorResult(
//						targetClass = DockerDTO.class,
//						columns = {
//								@ColumnResult(name="name",type = String.class),
//								@ColumnResult(name="vdms_id",type = String.class),
//								@ColumnResult(name="host",type=Boolean.class),
//								@ColumnResult(name="gateway",type=String.class),
//								@ColumnResult(name="external_ip_address",type=String.class),
//								@ColumnResult(name="system_type",type = String.class),
//								@ColumnResult(name="internal_ip_address",type = String.class),
//								@ColumnResult(name="public_ip_address",type = String.class),
//								@ColumnResult(name="internet_required",type = Boolean.class),
//								@ColumnResult(name="primary_dns",type = String.class),
//								@ColumnResult(name="secondary_dns",type = String.class),
//								@ColumnResult(name="is_static",type = Boolean.class),
//								@ColumnResult(name="is_tagged",type = Boolean.class),
//								@ColumnResult(name="vlan_id",type = Integer.class),
//								@ColumnResult(name="interface_out",type=String.class),
//								@ColumnResult(name="macvlan_name",type=String.class),
//								@ColumnResult(name="cidr",type = Integer.class),
//								@ColumnResult(name="configuration_status",type = String.class),
//								@ColumnResult(name="approval_status",type = String.class),
//								@ColumnResult(name="email",type = String.class)
//						}
//						)
//		}
//		)
//
//@NamedNativeQuery(
//		name="Docker.getAllNetworks",
//		query="SELECT d.name,d.vdms_id,d.host,d.gateway,d.external_ip_address,d.system_type,d.internal_ip_address,d.public_ip_address,d.internet_required,d.primary_dns,d.secondary_dns,d.is_static,d.is_tagged,d.vlan_id,d.interface_out,d.macvlan_name,d.cidr,d.configuration_status,d.approval_status,v.email from docker as d "
//				+ "LEFT JOIN vendor as v ON d.vendor_org_id=v.vendor_org_id "
//				+ "WHERE v.role = 'master-vendor' ",
//		resultSetMapping = "networklistmapping"
//		)

@SqlResultSetMapping(
        name="networkconditionsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = NetworkBoundaryConditionsDTO.class,
                        columns = {
                                @ColumnResult(name="network_name",type=String.class),
                                @ColumnResult(name="interface_name",type=String.class),
                                @ColumnResult(name="isTagged",type = Boolean.class),
                                @ColumnResult(name = "vlan_id",type=Integer.class),
                                @ColumnResult(name="host",type = Boolean.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name="Docker.getNetworkConditions",
        query="SELECT d.name AS network_name,d.interface_out AS interface_name,d.is_tagged AS isTagged,d.vlan_id,d.host FROM docker as d",
        resultSetMapping = "networkconditionsmapping"
)






@SqlResultSetMapping(
        name = "networklistbyvendormapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerDTO.class,
                        columns = {
                                @ColumnResult(name="name",type = String.class),
                                @ColumnResult(name="vdms_id",type = String.class),
                                @ColumnResult(name="host",type=Boolean.class),
                                @ColumnResult(name="gateway",type=String.class),
                                @ColumnResult(name="external_ip_address",type=String.class),
                                @ColumnResult(name="system_type",type = String.class),
                                @ColumnResult(name="internal_ip_address",type = String.class),
                                @ColumnResult(name="public_ip_address",type = String.class),
                                @ColumnResult(name="internet_required",type = Boolean.class),
                                @ColumnResult(name="primary_dns",type = String.class),
                                @ColumnResult(name="secondary_dns",type = String.class),
                                @ColumnResult(name="is_static",type = Boolean.class),
                                @ColumnResult(name="is_tagged",type = Boolean.class),
                                @ColumnResult(name="vlan_id",type = Integer.class),
                                @ColumnResult(name="interface_out",type=String.class),
                                @ColumnResult(name="macvlan_name",type=String.class),
                                @ColumnResult(name="cidr",type = Integer.class),
                                @ColumnResult(name="configuration_status",type = String.class),
                                @ColumnResult(name="approval_status",type = String.class),
                                @ColumnResult(name="email",type = String.class),
                                @ColumnResult(name="vendor_org_id",type = String.class),
                                @ColumnResult(name="network_origin", type = Integer.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name="Docker.getNetworksByVendorEmail",
        query="SELECT d.name,d.vdms_id,d.host,d.gateway,d.external_ip_address,d.system_type,d.internal_ip_address,d.public_ip_address,d.internet_required,d.primary_dns,d.secondary_dns,d.is_static,d.is_tagged,d.vlan_id,d.interface_out,d.macvlan_name,d.cidr,d.configuration_status,d.approval_status,v.email,d.vendor_org_id,d.network_origin from docker as d LEFT JOIN vendor as v ON d.vendor_org_id=v.vendor_org_id WHERE v.email=?1",
        resultSetMapping = "networklistbyvendormapping"
)


@SqlResultSetMapping(
        name = "vendorinfomapping",
        classes = {
                @ConstructorResult(
                        targetClass = VendorDTO.class,
                        columns = {
                                @ColumnResult(name = "email",type = String.class),
                                @ColumnResult(name = "name",type = String.class),
                                @ColumnResult(name = "phone",type = String.class),
                                @ColumnResult(name = "phone_type",type = String.class),
                                @ColumnResult(name = "value",type = String.class),
                                @ColumnResult(name = "company_name",type = String.class),
                                @ColumnResult(name = "website",type = String.class),
                                @ColumnResult(name = "address",type = String.class),
                                @ColumnResult(name = "city",type = String.class),
                                @ColumnResult(name = "country",type = String.class),
                                @ColumnResult(name = "state",type = String.class),
                                @ColumnResult(name = "zip",type = Integer.class),
                                @ColumnResult(name = "role",type = String.class),
                                @ColumnResult(name = "vendor_org_id",type = String.class),
                                @ColumnResult(name = "image_url",type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Docker.getVendorInfoByDockerName",
        query = "SELECT v.email ,v.name ,v.phone ,v.phone_type ,v.value ,v.company_name ,v.website ,v.address ,v.city ,v.country ,v.state ,"
                + "v.zip ,v.role ,v.vendor_org_id, v.image_url FROM vendor v LEFT JOIN docker d ON d.vendor_org_id = v.vendor_org_id WHERE d.name = ?1 AND "
                + "v.role = 'master-vendor'",
        resultSetMapping = "vendorinfomapping"

)



@NamedNativeQuery(
        name="Docker.getHostDockerObj",
        query="SELECT d.name,d.vdms_id,d.host,d.gateway,d.external_ip_address,d.system_type,d.internal_ip_address,d.public_ip_address,d.internet_required,d.primary_dns,d.secondary_dns,d.is_static,d.is_tagged,d.vlan_id,d.interface_out,d.macvlan_name,d.cidr,d.configuration_status,d.approval_status,v.email from docker as d LEFT JOIN vendor as v ON d.vendor_org_id=v.vendor_org_id WHERE v.role LIKE 'master-vendor' AND d.host=1",
        resultSetMapping = "networklistbyvendormapping"
)

//docker details with isp information
@SqlResultSetMapping(
        name = "dockerispinfomapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerPhonebookDTO.class,
                        columns = {
                                @ColumnResult(name = "name",type = String.class),
                                @ColumnResult(name = "vdms_id",type = String.class),
                                @ColumnResult(name= "system_type",type = String.class),
                                @ColumnResult(name= "internet_status",type = String.class),
                                @ColumnResult(name = "isp_id", type = String.class),
                                @ColumnResult(name = "account_number",type = String.class),
                                @ColumnResult(name = "company_name",type = String.class),
                                @ColumnResult(name = "email",type = String.class),
                                @ColumnResult(name = "phone",type = String.class),
                                @ColumnResult(name = "phone_type",type = String.class),
                                @ColumnResult(name = "value",type = String.class),
                                @ColumnResult(name = "vendor_name",type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Docker.getDockerIspInfoByDockerName",
        query = "SELECT do.name, do.vdms_id, do.system_type, do.internet_status, do.isp_id, "
                + "p.account_number, p.company_name, p.email, p.phone, p.phone_type, p.value, p.vendor_name "
                + "FROM docker do "
                + "LEFT JOIN phonebook p ON do.isp_id = p.id "
                + "WHERE do.name = ?1",
        resultSetMapping = "dockerispinfomapping"

)

@NamedNativeQuery(
        name = "Docker.getNoInternetNetworkTS",
        query = "SELECT do.name, do.vdms_id, do.system_type, do.internet_status, do.isp_id, "
                + "p.account_number, p.company_name, p.email, p.phone, p.phone_type, p.value, p.vendor_name "
                + "FROM docker do "
                + "LEFT JOIN phonebook p ON do.isp_id = p.id "
                + "WHERE do.internet_required = 1 AND do.internet_status IS NOT NULL AND do.internet_status NOT IN ('active-internet', '')",
        resultSetMapping = "dockerispinfomapping"

)



@SqlResultSetMapping(name = "dockerinterfacelistmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerInfoDto.class,
                        columns = {
                                @ColumnResult(name = "interface_out", type = String.class),
                                @ColumnResult(name = "interface_status", type = String.class)
                        }
                )
        }
)
@NamedNativeQuery (
        name = "Docker.getDockerInterfaceList",
        query = "SELECT interface_out, interface_status FROM docker WHERE network_origin = ?1 ",
        resultSetMapping = "dockerinterfacelistmapping"
)

@NamedNativeQuery (
        name = "Docker.getVdmsConfigInterfaceList",
        query = "SELECT interface_id AS interface_out, interface_status FROM vdms_configuration ",
        resultSetMapping = "dockerinterfacelistmapping"
)

//@NamedNativeQuery (
//		name = "Docker.getInterfaceList",
//		query = "SELECT interface_out , interface_status FROM docker UNION SELECT interface_id , interface_status FROM vdms_configuration ",
//		resultSetMapping = "dockerinterfacelistmapping"
//)

//NEW SETTINGS
@SqlResultSetMapping(
        name = "networklistmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerDTO.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "host", type = Boolean.class),
                                @ColumnResult(name = "gateway", type = String.class),
                                @ColumnResult(name = "external_ip_address", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "internal_ip_address", type = String.class),
                                @ColumnResult(name = "public_ip_address", type = String.class),
                                @ColumnResult(name = "internet_required", type = Boolean.class),
                                @ColumnResult(name = "primary_dns", type = String.class),
                                @ColumnResult(name = "secondary_dns", type = String.class),
                                @ColumnResult(name = "is_static", type = Boolean.class),
                                @ColumnResult(name = "is_tagged", type = Boolean.class),
                                @ColumnResult(name = "vlan_id", type = Integer.class),
                                @ColumnResult(name = "interface_out", type = String.class),
                                @ColumnResult(name = "macvlan_name", type = String.class),
                                @ColumnResult(name = "cidr", type = Integer.class),
                                @ColumnResult(name = "configuration_status", type = String.class),
                                @ColumnResult(name = "approval_status", type = String.class),
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "vendor_org_id", type = String.class),
                                @ColumnResult(name = "internet_status", type = String.class),
                                @ColumnResult(name = "network_origin", type = Integer.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Docker.getAllNetworks",
        query = "SELECT d.name,d.vdms_id,d.host,d.gateway,d.external_ip_address,d.system_type,d.internal_ip_address,d.public_ip_address,d.internet_required,d.primary_dns,d.secondary_dns,d.is_static,d.is_tagged,d.vlan_id,d.interface_out,d.macvlan_name,d.cidr,d.configuration_status,d.approval_status,NULL as email , d.vendor_org_id, d.internet_status, d.network_origin from docker as d ",
        resultSetMapping = "networklistmapping"
)

@NamedNativeQuery(
        name = "Docker.getAllNetworksByNetworkOrigin",
        query = "SELECT d.name,d.vdms_id,d.host,d.gateway,d.external_ip_address,d.system_type,d.internal_ip_address,d.public_ip_address,d.internet_required,d.primary_dns,d.secondary_dns,d.is_static,d.is_tagged,d.vlan_id,d.interface_out,d.macvlan_name,d.cidr,d.configuration_status,d.approval_status,NULL as email , d.vendor_org_id, d.internet_status, d.network_origin from docker as d where d.network_origin = ?1 ",
        resultSetMapping = "networklistmapping"
)

@NamedNativeQuery(
        name = "Docker.getNetworkById",
        query = "SELECT d.name,d.vdms_id,d.host,d.gateway,d.external_ip_address,d.system_type,d.internal_ip_address,d.public_ip_address,d.internet_required,d.primary_dns,d.secondary_dns,d.is_static,d.is_tagged,d.vlan_id,d.interface_out,d.macvlan_name,d.cidr,d.configuration_status,d.approval_status,NULL as email , d.vendor_org_id, d.internet_status, d.network_origin from docker as d WHERE d.name=?1",
        resultSetMapping = "networklistmapping"
)



@SqlResultSetMapping(
        name = "checkifhostpresentbynetworkoriginmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerDTO.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "host", type = Boolean.class),
                                @ColumnResult(name = "network_origin", type = Integer.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Docker.checkIfHostNetworkPresentByNetworkOrigin",
        query = "SELECT name, host, network_origin FROM docker WHERE host=true AND network_origin=?1",
        resultSetMapping = "checkifhostpresentbynetworkoriginmapping"
)

//@NamedNativeQuery(
//		name = "Docker.listAllHostNetworks",
//		query = "SELECT name, host, network_origin FROM docker WHERE host=true AND network_origin IN (0, 1)",
//		resultSetMapping = "checkifhostpresentmapping"
//)

@SqlResultSetMapping(
        name = "checkifhostnetworkspresentmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerDTO.class,
                        columns = {
                                @ColumnResult(name = "network_origin", type = Integer.class),
                                @ColumnResult(name = "host", type = Boolean.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Docker.checkIfHostNetworksPresent",
        query = "SELECT "
                + "network_origin, "
                + "CASE "
                + "WHEN MAX(CASE WHEN host = true THEN 1 ELSE 0 END) = 1 "
                + "THEN true "
                + "ELSE false "
                + "END AS host "
                + "FROM docker "
                + "WHERE network_origin IN (0, 1) "
                + "GROUP BY network_origin",
        resultSetMapping = "checkifhostnetworkspresentmapping"
)

//@NamedNativeQuery(
//		name = "Docker.listAllHostNetworks",
//		query = "SELECT "
//				+ "o.network_origin, "
//				+ "CASE "
//				+ "WHEN MAX(CASE WHEN d.host = true THEN 1 ELSE 0 END) = 1 "
//				+ "THEN true "
//				+ "ELSE false "
//				+ "END AS host "
//				+ "FROM ( "
//				+ "SELECT 0 AS network_origin "
//				+ "UNION ALL "
//				+ "SELECT 1 AS network_origin "
//				+ ") o "
//				+ "LEFT JOIN docker d "
//				+ "ON d.network_origin = o.network_origin "
//				+ "GROUP BY o.network_origin "
//				+ "ORDER BY o.network_origin",
//		resultSetMapping = "listallhostnetworksmapping"
//)


@SqlResultSetMapping(name = "dockernameslistmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerDTO.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "network_origin", type = Integer.class)
                        }
                )
        }
)

@NamedNativeQuery (
        name = "Docker.listDockerNameById",
        query = "SELECT DISTINCT d.name, d.network_origin FROM docker d, vdms v, user u WHERE u.email = ?1 AND v.id = ?2 ",
        resultSetMapping = "dockernameslistmapping"
)


/************Port Switching***********************/

@SqlResultSetMapping(
        name = "networknameandinternalipmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerInfoDto.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "internal_ip_address", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Docker.getNameAndInternalIP",
        query = "SELECT do.name, do.internal_ip_address, do.vdms_id FROM docker do",
        resultSetMapping = "networknameandinternalipmapping"
)

/************Port Switching***********************/

@SqlResultSetMapping(
        name = "listotherdockerinfomapping",
        classes = {
                @ConstructorResult(
                        targetClass = DockerInfoDto.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "external_ip_address", type = String.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "Docker.listOtherDockerInfo",
        query = "SELECT d.name, d.vdms_id, d.mac_address, d.external_ip_address "
                + " FROM docker as d "
                + " WHERE NOT d.name = ?1 AND d.configuration_status = 'Configured' ",
        resultSetMapping = "listotherdockerinfomapping"
)

public class Docker {
    @Id
    private String name;

    @MapsId
    @ManyToOne
    private Vdms vdms;

    @Column(length = 64)
    private String gateway;

    @Column(length = 1)
    private Boolean host;

    @Column(length = 64)
    private String external_ip_address;

    @Column(length = 32)
    private String mac_address;

    @Column(length = 128)
    private String system_type;

    @Column(length = 64)
    private String internet_status;

    @Column(length = 1)
    private Boolean internet_required;

    private BigInteger internet_timestamp;

    @Column(length = 1)
    private Boolean is_block;

    private BigInteger block_timestamp;

    @Column(length = 5)
    private Integer cidr;

    @Column(length = 64)
    private String primary_dns;

    @Column(length = 64)
    private String secondary_dns;

    @Column(length = 16)
    private Integer vlan_id;

    @Column(length = 64)
    private String interface_in;

    @Column(length = 64)
    private String interface_out;

    @Column(length = 1)
    private Boolean isStatic;

    @Column(length = 1)
    private Boolean isTagged;

    @Column(length = 128)
    private String macvlan_name;

    @Column(length = 64)
    private String public_ip_address;

    @Column(length = 64)
    private String internal_ip_address;

    @Column(length = 64)
    private String approval_status;

    @Column(length = 64)
    private String configuration_status;

    @Column(length = 64)
    private String interface_status;

    @Column(columnDefinition = "INTEGER DEFAULT 1", length = 1, nullable = false)
    private Integer network_origin;

    @ManyToOne
    private Vendor_Organisation vendor_org;

    @javax.persistence.Transient
    private Set<Device> device;

    @OneToOne
    private Phonebook isp;


    @OneToOne(cascade = CascadeType.ALL,mappedBy = "docker")
    private RemoteAccess remoteaccess;

    @OneToMany(mappedBy = "docker",cascade = CascadeType.ALL)
    private Set<Bacnet_Device> bacnet_device;

    @javax.persistence.Transient
    private Set<History> history;

    @OneToMany(mappedBy = "docker",cascade = CascadeType.ALL)
    private Set<KNXInterface> knx_interface;

    @OneToMany(mappedBy = "docker",cascade = CascadeType.ALL)
    private Set<SnmpDeviceConfiguration> snmp_device_configuration;

    public Docker() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vdms getVdms() {
        return vdms;
    }

    public void setVdms(Vdms vdms) {
        this.vdms = vdms;
    }

    public Set<Device> getDevice() {
        return device;
    }

    public void setDevice(Set<Device> device) {
        this.device = device;
        device.forEach((temp) -> {temp.setDocker(this);});
    }

    public Phonebook getIsp() {
        return isp;
    }

    public void setIsp(Phonebook isp) {
        this.isp = isp;
    }

    public RemoteAccess getRemoteaccess() {
        return remoteaccess;
    }

    public void setRemoteaccess(RemoteAccess remoteaccess) {
        this.remoteaccess = remoteaccess;
        remoteaccess.setDocker(this);
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public Boolean getHost() {
        return host;
    }

    public void setHost(Boolean host) {
        this.host = host;
    }

    public String getExternal_ip_address() {
        return external_ip_address;
    }

    public void setExternal_ip_address(String external_ip_address) {
        this.external_ip_address = external_ip_address;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getSystem_type() {
        return system_type;
    }

    public void setSystem_type(String system_type) {
        this.system_type = system_type;
    }

    public String getInternet_status() {
        return internet_status;
    }

    public void setInternet_status(String internet_status) {
        this.internet_status = internet_status;
    }

    public Boolean getInternet_required() {
        return internet_required;
    }

    public void setInternet_required(Boolean internet_required) {
        this.internet_required = internet_required;
    }

    public BigInteger getInternet_timestamp() {
        return internet_timestamp;
    }

    public void setInternet_timestamp(BigInteger internet_timestamp) {
        this.internet_timestamp = internet_timestamp;
    }

    public Boolean getIs_block() {
        return is_block;
    }

    public void setIs_block(Boolean is_block) {
        this.is_block = is_block;
    }

    public BigInteger getBlock_timestamp() {
        return block_timestamp;
    }

    public void setBlock_timestamp(BigInteger block_timestamp) {
        this.block_timestamp = block_timestamp;
    }

    public Integer getCidr() {
        return cidr;
    }

    public void setCidr(Integer cidr) {
        this.cidr = cidr;
    }

    public String getPrimary_dns() {
        return primary_dns;
    }

    public void setPrimary_dns(String primary_dns) {
        this.primary_dns = primary_dns;
    }

    public String getSecondary_dns() {
        return secondary_dns;
    }

    public void setSecondary_dns(String secondary_dns) {
        this.secondary_dns = secondary_dns;
    }

    public Integer getVlan_id() {
        return vlan_id;
    }

    public void setVlan_id(Integer vlan_id) {
        this.vlan_id = vlan_id;
    }

    public String getInterface_in() {
        return interface_in;
    }

    public void setInterface_in(String interface_in) {
        this.interface_in = interface_in;
    }

    public String getInterface_out() {
        return interface_out;
    }

    public void setInterface_out(String interface_out) {
        this.interface_out = interface_out;
    }

    public Boolean getIsStatic() {
        return isStatic;
    }

    public void setIsStatic(Boolean isStatic) {
        this.isStatic = isStatic;
    }

    public Boolean getIsTagged() {
        return isTagged;
    }

    public void setIsTagged(Boolean isTagged) {
        this.isTagged = isTagged;
    }

    public String getMacvlan_name() {
        return macvlan_name;
    }

    public void setMacvlan_name(String macvlan_name) {
        this.macvlan_name = macvlan_name;
    }

    public String getPublic_ip_address() {
        return public_ip_address;
    }

    public void setPublic_ip_address(String public_ip_address) {
        this.public_ip_address = public_ip_address;
    }

    public String getInternal_ip_address() {
        return internal_ip_address;
    }

    public void setInternal_ip_address(String internal_ip_address) {
        this.internal_ip_address = internal_ip_address;
    }

    public String getApproval_status() {
        return approval_status;
    }

    public void setApproval_status(String approval_status) {
        this.approval_status = approval_status;
    }

    public Vendor_Organisation getVendor_org() {
        return vendor_org;
    }

    public void setVendor_org(Vendor_Organisation vendor_org) {
        this.vendor_org = vendor_org;
    }

    public Set<Bacnet_Device> getBacnet_device() {
        return bacnet_device;
    }

    public void setBacnet_device(Set<Bacnet_Device> bacnet_device) {
        this.bacnet_device = bacnet_device;
    }

    public String getConfiguration_status() {
        return configuration_status;
    }

    public void setConfiguration_status(String configuration_status) {
        this.configuration_status = configuration_status;
    }

//	public Set<Lorawan_Gateway> getLorawan_gateway() {
//		return lorawan_gateway;
//	}
//
//	public void setLorawan_gateway(Set<Lorawan_Gateway> lorawan_gateway) {
//		this.lorawan_gateway = lorawan_gateway;
//	}

    public Set<History> getHistory() {
        return history;
    }

    public void setHistory(Set<History> history) {
        this.history = history;
    }

    public String getInterface_status() {
        return interface_status;
    }

    public void setInterface_status(String interface_status) {
        this.interface_status = interface_status;
    }

    public Set<KNXInterface> getKnx_interface() {
        return knx_interface;
    }

    public void setKnx_interface(Set<KNXInterface> knx_interface) {
        this.knx_interface = knx_interface;
    }

    public Set<SnmpDeviceConfiguration> getSnmp_device_configuration() {
        return snmp_device_configuration;
    }

    public void setSnmp_device_configuration(Set<SnmpDeviceConfiguration> snmp_device_configuration) {
        this.snmp_device_configuration = snmp_device_configuration;
    }

    public Integer getNetwork_origin() {
        return network_origin;
    }

    public void setNetwork_origin(Integer network_origin) {
        this.network_origin = network_origin;
    }
}
