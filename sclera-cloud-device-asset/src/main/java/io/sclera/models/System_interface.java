package io.sclera.models;

import java.math.BigInteger;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;

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
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (interface_name) DO UPDATE SET (VALUES->EXCLUDED); VALUE->VALUES
    @NamedNativeQuery(name = "System_interface.upsertInterfaceStatus", query = "INSERT INTO system_interface(interface_name, status) VALUES (?1, ?2) ON CONFLICT (interface_name) DO UPDATE SET status = EXCLUDED.status", resultClass = System_interface.class),
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
