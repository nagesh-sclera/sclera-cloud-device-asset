package io.sclera.models;

import java.math.BigInteger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.sclera.dto.Product_NotesDTO;
import io.sclera.dto.Product_PortsDTO;
import io.sclera.models.compositeclass.ServiceIds;

@Entity
@IdClass(ServiceIds.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class , property = "id" , scope = Service.class)

@SqlResultSetMapping(
		name = "servicemapping",
		classes = {
				@ConstructorResult(
						targetClass = Product_PortsDTO.class,
						columns = {
								@ColumnResult(name = "id",type = String.class),
								@ColumnResult(name = "port",type = String.class),
								@ColumnResult(name = "status",type = Integer.class),
								@ColumnResult(name = "timestamp",type = BigInteger.class),
								@ColumnResult(name = "device_id",type = String.class),
								@ColumnResult(name = "is_global",type = Integer.class)
						}
						)
		}
		)





@NamedNativeQuery(
		name = "Service.getPortsByDeviceId",
		query = "SELECT id ,port,status ,timestamp ,device_id ,is_global FROM service WHERE device_id = ?1",
		resultSetMapping = "servicemapping"	

		)

@SqlResultSetMapping(
		name = "portsstatusmapping",
		classes = {
				@ConstructorResult(
						targetClass = Product_PortsDTO.class,
						columns = {
								@ColumnResult(name = "id",type = String.class),
								@ColumnResult(name = "port",type = String.class),
								@ColumnResult(name = "ip_address",type = String.class),
								@ColumnResult(name = "device_id",type = String.class),
						}
						)
		}
		)





@NamedNativeQuery(
		name = "Service.listDockerDevicesByPorts",
		query = "SELECT s.id, s.port, d.ip_address, s.device_id FROM service s JOIN device d ON s.device_id = d.id "
				+ "WHERE d.docker_name = ?1 ",
				resultSetMapping = "portsstatusmapping"	
		)


public class Service {

	@Id
	private String id;

	@Column(length = 32)
	private String port;

	@Column(length = 8)
	private Integer status;

	private BigInteger timestamp;

	@Column(columnDefinition = "integer default 0")
	private Integer is_global;

	@MapsId
	@ManyToOne
	private Device device;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public BigInteger getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getIs_global() {
		return is_global;
	}

	public void setIs_global(Integer is_global) {
		this.is_global = is_global;
	}


}
