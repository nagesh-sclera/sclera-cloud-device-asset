package io.sclera.model;

import io.sclera.dto.CustomerOrganisationDto;
import io.sclera.dto.IocDto;
import jakarta.persistence.*;
import java.util.Set;


@Entity

@SqlResultSetMapping(
		name = "customerOrganisationDataMapping",
		classes = {
				@ConstructorResult(
						targetClass = CustomerOrganisationDto.class,
						columns = {
								@ColumnResult(name = "id", type = String.class),
								@ColumnResult(name = "company_name", type = String.class),
						}
				)
		}
)
@NamedNativeQuery(
		name = "Customer_Organisation.getAllOrgIdAndCompanyName",
		query = "SELECT id ,company_name FROM `customer_organisation` " ,
		resultSetMapping = "customerOrganisationDataMapping"
)

public class Customer_Organisation {

	@Id
	private String id;

	@Column(length = 128 ,unique = true)
	private String company_name;

	@Column(columnDefinition = "integer default 1")
	private Integer is_enterprise;

	@OneToMany(mappedBy = "customer_org" ,cascade = CascadeType.ALL)
	private Set<User> users;

	@OneToMany(mappedBy = "customer_org" , cascade = CascadeType.ALL)
	private Set<Vdms> vdms;

	@OneToMany(mappedBy = "customer_org" , cascade = CascadeType.ALL)
	private Set<ProxyProfile> proxy_profiles;

	@OneToMany(mappedBy = "customer_org" , cascade = CascadeType.ALL)
	private Set<Ioc> ioc;

	@OneToMany(mappedBy = "customer_org" ,cascade = CascadeType.ALL)
	private Set<ClientBarCode> clientBarCodes;

	@OneToMany(mappedBy = "customer_org" ,cascade = CascadeType.ALL)
	private Set<ClientNfc> clientNfcs;

	@OneToMany(mappedBy = "customer_org" ,cascade = CascadeType.ALL)
	private Set<ClientQrCode> clientQrCodes;

	@OneToMany(mappedBy = "customer_org" ,cascade = CascadeType.ALL)
	private Set<QrCode> qrCodes;

	public Set<ProxyProfile> getProxy_profiles() {
		return proxy_profiles;
	}

	public void setProxy_profiles(Set<ProxyProfile> proxy_profiles) {
		this.proxy_profiles = proxy_profiles;
		proxy_profiles.forEach((temp) -> {temp.setCustomer_org(this);});
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
		users.forEach((temp) -> {temp.setCustomer_org(this);});
	}

	public Set<Vdms> getVdms() {
		return vdms;
	}

	public void setVdms(Set<Vdms> vdms) {
		this.vdms = vdms;
		vdms.forEach((temp) -> {temp.setCustomer_org(this);});
	}

	public String getCompany_name() {
		return company_name;
	}

	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}
}
