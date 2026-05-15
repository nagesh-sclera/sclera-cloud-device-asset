package io.sclera.dto.touchscreen;

public class ContactListDTO {

	
	private String id;
	private String vendor_name;
	private String company_name;
	
	
	public String getId() {
		return id;
	}
	public String getVendor_name() {
		return vendor_name;
	}
	public String getCompany_name() {
		return company_name;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setVendor_name(String vendor_name) {
		this.vendor_name = vendor_name;
	}
	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}
	public ContactListDTO(String id, String vendor_name, String company_name) {
		super();
		this.id = id;
		this.vendor_name = vendor_name;
		this.company_name = company_name;
	}
	
	
	
	
	
}
