package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DockerPhonebookDTO {
	
	//docker info
	private String name;
	private String vdms_id;
	private String gateway;
	private Boolean host;
    private String external_ip_address;
    private String system_type; 
    private String internal_ip_address;
    private String public_ip_address;
    private Boolean internet_required;
    private String internet_status;	
    private String isp_id;
    private String cidr;
    private Integer vlan_id;
    private String primary_dns;
    private String secondary_dns;
    
    //phonebook info
    private String account_number;
	private String company_name;
	private String email;
	private String phone;
	private String phone_type;
	private String value;
	private String vendor_name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVdms_id() {
		return vdms_id;
	}
	public void setVdms_id(String vdms_id) {
		this.vdms_id = vdms_id;
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
	public String getSystem_type() {
		return system_type;
	}
	public void setSystem_type(String system_type) {
		this.system_type = system_type;
	}
	public String getInternal_ip_address() {
		return internal_ip_address;
	}
	public void setInternal_ip_address(String internal_ip_address) {
		this.internal_ip_address = internal_ip_address;
	}
	public String getPublic_ip_address() {
		return public_ip_address;
	}
	public void setPublic_ip_address(String public_ip_address) {
		this.public_ip_address = public_ip_address;
	}
	public Boolean getInternet_required() {
		return internet_required;
	}
	public void setInternet_required(Boolean internet_required) {
		this.internet_required = internet_required;
	}
	public String getInternet_status() {
		return internet_status;
	}
	public void setInternet_status(String internet_status) {
		this.internet_status = internet_status;
	}
	public String getIsp_id() {
		return isp_id;
	}
	public void setIsp_id(String isp_id) {
		this.isp_id = isp_id;
	}
	public String getCidr() {
		return cidr;
	}
	public void setCidr(String cidr) {
		this.cidr = cidr;
	}
	public Integer getVlan_id() {
		return vlan_id;
	}
	public void setVlan_id(Integer vlan_id) {
		this.vlan_id = vlan_id;
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
	public String getAccount_number() {
		return account_number;
	}
	public void setAccount_number(String account_number) {
		this.account_number = account_number;
	}
	public String getCompany_name() {
		return company_name;
	}
	public void setCompany_name(String company_name) {
		this.company_name = company_name;
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
	public String getVendor_name() {
		return vendor_name;
	}
	public void setVendor_name(String vendor_name) {
		this.vendor_name = vendor_name;
	}
	public DockerPhonebookDTO() {
		super();
	}
	public DockerPhonebookDTO(String name, String vdms_id, String system_type, String internet_status, String isp_id, 
			String account_number, String company_name, String email, String phone, String phone_type, String value, String vendor_name) {
		super();
		this.name = name;
		this.vdms_id = vdms_id;
		this.system_type = system_type;
		this.internet_status = internet_status;
		this.isp_id = isp_id;
		this.account_number = account_number;
		this.company_name = company_name;
		this.email = email;
		this.phone = phone;
		this.phone_type = phone_type;
		this.value = value;
		this.vendor_name = vendor_name;
	}
}
