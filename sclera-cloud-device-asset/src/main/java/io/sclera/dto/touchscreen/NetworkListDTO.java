package io.sclera.dto.touchscreen;

public class NetworkListDTO {

	
	private String name;
	private String system_type;
	private String vendor_org_id;
	private String external_ip_address;
	
	
	public String getName() {
		return name;
	}
	public String getSystem_type() {
		return system_type;
	}
	public String getVendor_org_id() {
		return vendor_org_id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setSystem_type(String system_type) {
		this.system_type = system_type;
	}
	public void setVendor_org_id(String vendor_org_id) {
		this.vendor_org_id = vendor_org_id;
	}
	
	
	
	public String getExternal_ip_address() {
		return external_ip_address;
	}
	public void setExternal_ip_address(String external_ip_address) {
		this.external_ip_address = external_ip_address;
	}
	public NetworkListDTO(String name, String system_type, String vendor_org_id, String external_ip_address) {
		super();
		this.name = name;
		this.system_type = system_type;
		this.vendor_org_id = vendor_org_id;
		this.external_ip_address = external_ip_address;
	}
	
	
	
	
	
	
}
