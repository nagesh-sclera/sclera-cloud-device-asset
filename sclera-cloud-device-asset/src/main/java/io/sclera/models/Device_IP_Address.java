package io.sclera.models;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

// removed: unused import io.sclera.dto.HistoryDTO (AP-C6 history)
import io.sclera.dto.touchscreen.DeviceIPAddressDTO;

@Entity


@SqlResultSetMapping(
		name = "deviceipaddressmapping",
		classes = {
				@ConstructorResult(
						targetClass = DeviceIPAddressDTO.class,
						columns = {
								@ColumnResult(name = "ip_address",type = String.class),
								@ColumnResult(name = "ip_conflict_status",type = Integer.class)
						})
		})


@NamedNativeQueries({
    @NamedNativeQuery(name = "Device_IP_Address.getIPAddressByDeviceId", query = "SELECT ip_address, ip_conflict_status FROM device_ip_address WHERE device_id = ?1", resultSetMapping = "deviceipaddressmapping"),
    @NamedNativeQuery(name = "Device_IP_Address.deleteIPAddressByDeviceId", query = "DELETE FROM device_ip_address WHERE device_id = ?1", resultClass = Device_IP_Address.class),
    @NamedNativeQuery(name = "Device_IP_Address.insertIPAddressByDeviceId", query = "INSERT INTO device_ip_address (id, ip_address, ip_conflict_status, device_id) VALUES (?1, ?2, ?3, ?4)", resultClass = Device_IP_Address.class)
})




public class Device_IP_Address {

	@Id
	private String id;
	
	@Column(length = 64)
	private String ip_address;
	
	@Column(length = 8, columnDefinition = "integer default 0")
	private Integer ip_conflict_status;
	
	@ManyToOne
	private Device device;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}

	public Integer getIp_conflict_status() {
		return ip_conflict_status;
	}

	public void setIp_conflict_status(Integer ip_conflict_status) {
		this.ip_conflict_status = ip_conflict_status;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}
}
