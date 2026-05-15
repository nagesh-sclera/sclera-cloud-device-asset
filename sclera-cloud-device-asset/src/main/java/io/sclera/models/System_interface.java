package io.sclera.models;

import java.math.BigInteger;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;

import io.sclera.dto.DockerInfoDto;
import io.sclera.dto.VlanDTO;


@SqlResultSetMappings({
    @SqlResultSetMapping(name = "systeminterfacelistmapping",
        classes = {
            @ConstructorResult(
                targetClass = DockerInfoDto.class,
                columns = {
                    @ColumnResult(name = "interface_out", type = String.class),
                    @ColumnResult(name = "interface_status", type = String.class)
                }
            )
        }
    ),
    @SqlResultSetMapping(name = "systeminterfacepidmapping",
        classes = {
            @ConstructorResult(
                targetClass = VlanDTO.class,
                columns = {
                    @ColumnResult(name = "pid", type = String.class),
                    @ColumnResult(name = "timestamp", type = BigInteger.class)
                }
            )
        }
    )
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "System_interface.getInterfaceStatusList", query = "SELECT interface_name as interface_out, status as interface_status FROM system_interface", resultSetMapping = "systeminterfacelistmapping"),
    @NamedNativeQuery(name = "System_interface.getVlanDiscoverPidByInterfaceName", query = "SELECT pid, timestamp FROM system_interface WHERE interface_name = ?1", resultSetMapping = "systeminterfacepidmapping"),
    @NamedNativeQuery(name = "System_interface.upsertInterfaceStatus", query = "INSERT INTO system_interface(interface_name, status) VALUE (?1, ?2) ON DUPLICATE KEY UPDATE status = ?2", resultClass = System_interface.class),
    @NamedNativeQuery(name = "System_interface.getInterfaceStatus", query = "SELECT status FROM system_interface WHERE interface_name = ?1", resultClass = System_interface.class),
    @NamedNativeQuery(name = "System_interface.updateVlanDiscoverPidByInterfaceName", query = "UPDATE system_interface SET pid = ?1, timestamp = ?2 WHERE interface_name = ?3", resultClass = System_interface.class),
    @NamedNativeQuery(name = "System_interface.deleteAllInterface", query = "DELETE from system_interface", resultClass = System_interface.class)
})
@Entity
public class System_interface {
	
	@Id
	private String interface_name;
	private String status;
	private String pid;
	private BigInteger timestamp;
	
	
	public String getInterface_name() {
		return interface_name;
	}
	public void setInterface_name(String interface_name) {
		this.interface_name = interface_name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public BigInteger getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
	

}
