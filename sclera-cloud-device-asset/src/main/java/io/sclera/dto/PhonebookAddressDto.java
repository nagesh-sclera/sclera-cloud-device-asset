package io.sclera.dto;



public class PhonebookAddressDto {
   
	
	private String id;
	private String account_number;
	private String vendor_name;
	private String email;
	private String phone;
	private String phone_type;
	private String value;
	private String company_name;
	private String website;
	private String address;
	private String city;
	private String country;
	private String state;
	private String street;
	private Integer zip;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAccount_number() {
		return account_number;
	}
	public void setAccount_number(String account_number) {
		this.account_number = account_number;
	}
	public String getVendor_name() {
		return vendor_name;
	}
	public void setVendor_name(String vendor_name) {
		this.vendor_name = vendor_name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPhone_type() {
		return phone_type;
	}
	public void setPhone_type(String phone_type) {
		this.phone_type = phone_type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getCompany_name() {
		return company_name;
	}
	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public Integer getZip() {
		return zip;
	}
	public void setZip(Integer zip) {
		this.zip = zip;
	}
	
	public PhonebookAddressDto() {}
	
	public PhonebookAddressDto(String id, String account_number, String vendor_name, String email, String phone,
			String phone_type, String value, String company_name, String website, String address, String city,
			String country, String state, String street, Integer zip) {
		super();
		this.id = id;
		this.account_number = account_number;
		this.vendor_name = vendor_name;
		this.email = email;
		this.phone = phone;
		this.phone_type = phone_type;
		this.value = value;
		this.company_name = company_name;
		this.website = website;
		this.address = address;
		this.city = city;
		this.country = country;
		this.state = state;
		this.street = street;
		this.zip = zip;
	}
	

	

	
	
	
	
	
	
	
	

	
}