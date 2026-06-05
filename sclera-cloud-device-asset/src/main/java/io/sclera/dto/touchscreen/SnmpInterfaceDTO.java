package io.sclera.dto.touchscreen;

import io.sclera.dto.InterfaceDTO;

import java.util.List;

/**
 * Associates a device identifier with its list of SNMP-discovered network
 * interfaces for the touchscreen, used to present the interfaces reported by an
 * SNMP-managed device.
 */
public class SnmpInterfaceDTO {

	private String id;
	private List<InterfaceDTO> interfaces;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<InterfaceDTO> getInterfaces() {
		return interfaces;
	}
	public void setInterfaces(List<InterfaceDTO> interfaces) {
		this.interfaces = interfaces;
	}
	public SnmpInterfaceDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SnmpInterfaceDTO(String id, List<InterfaceDTO> interfaces) {
		super();
		this.id = id;
		this.interfaces = interfaces;
	}
	@Override
	public String toString() {
		return "SnmpInterfaceDTO [id=" + id + ", interfaces=" + interfaces + "]";
	}

	
}
