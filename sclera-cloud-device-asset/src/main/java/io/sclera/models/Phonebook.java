package io.sclera.models;

import java.util.Set;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.PhonebookAddressDto;
import io.sclera.dto.touchscreen.ContactListDTO;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Phonebook.class)
@SqlResultSetMapping(name = "phonebookaddress", classes = { @ConstructorResult(targetClass = PhonebookAddressDto.class, columns = {
    @ColumnResult(name = "id", type = String.class), @ColumnResult(name = "account_number", type = String.class),
    @ColumnResult(name = "vendor_name", type = String.class), @ColumnResult(name = "email", type = String.class),
    @ColumnResult(name = "phone", type = String.class), @ColumnResult(name = "phone_type", type = String.class),
    @ColumnResult(name = "value", type = String.class), @ColumnResult(name = "company_name", type = String.class),
    @ColumnResult(name = "website", type = String.class), @ColumnResult(name = "address", type = String.class),
    @ColumnResult(name = "city", type = String.class), @ColumnResult(name = "country", type = String.class),
    @ColumnResult(name = "state", type = String.class), @ColumnResult(name = "street", type = String.class),
    @ColumnResult(name = "zip", type = Integer.class)}) })
@NamedNativeQuery(name = "Phonebook.getPhoneAddressById",
    query = "SELECT p.id, p.account_number, p.vendor_name, p.email, p.phone, p.phone_type, p.value, p.company_name, p.website, p.address, p.city, p.country, p.state, p.street, p.zip FROM phonebook as p WHERE p.id = ?1",
    resultSetMapping = "phonebookaddress")
@NamedNativeQuery(name = "Phonebook.getAllPhonePhoneAddress",
    query = "SELECT p.id, p.account_number, p.vendor_name, p.email, p.phone, p.phone_type, p.value, p.company_name, p.website, p.address, p.city, p.country, p.state, p.street, p.zip FROM phonebook as p",
    resultSetMapping = "phonebookaddress")
@SqlResultSetMapping(name = "phonebooklist", classes = { @ConstructorResult(targetClass = ContactListDTO.class, columns = {
    @ColumnResult(name = "id", type = String.class), @ColumnResult(name = "vendor_name", type = String.class),
    @ColumnResult(name = "company_name", type = String.class)}) })
@NamedNativeQuery(name = "Phonebook.getphonebooklist",
    query = "SELECT DISTINCT p.id , p.vendor_name , p.company_name FROM phonebook as p JOIN device d on d.local_vendor_id = p.id OR d.other_vendor_1_id = p.id OR d.other_vendor_2_id = p.id OR d.other_vendor_3_id = p.id ORDER BY p.vendor_name ASC",
    resultSetMapping = "phonebooklist")
public class Phonebook {
    @Id private String id;
    private String account_number;
    @Column(length = 128) private String vendor_name;
    private String email;
    @Column(length = 32) private String phone;
    @Column(length = 32) private String phone_type;
    @Column(length = 16) private String value;
    @Column(length = 128) private String company_name;
    private String website;
    private String address;
    @Column(length = 64) private String city;
    @Column(length = 64) private String country;
    @Column(length = 64) private String state;
    @Column(length = 8) private Integer zip;
    @Column(length = 64) private String street;
    // removed: relation to Bucket-D entity Docker (edge-only)
    @OneToMany(mappedBy = "global_vendor", cascade = CascadeType.ALL) private Set<Device> device_global_vendor;
    @OneToMany(mappedBy = "local_vendor", cascade = CascadeType.ALL) private Set<Device> device_local_vendor;
    @OneToMany(mappedBy = "other_vendor_1", cascade = CascadeType.ALL) private Set<Device> device_other_vendor_1;
    @OneToMany(mappedBy = "other_vendor_2", cascade = CascadeType.ALL) private Set<Device> device_other_vendor_2;
    @OneToMany(mappedBy = "other_vendor_3", cascade = CascadeType.ALL) private Set<Device> device_other_vendor_3;
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getAccount_number() { return account_number; } public void setAccount_number(String v) { this.account_number = v; }
    public String getVendor_name() { return vendor_name; } public void setVendor_name(String v) { this.vendor_name = v; }
    public String getEmail() { return email; } public void setEmail(String v) { this.email = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { this.phone = v; }
    public String getPhone_type() { return phone_type; } public void setPhone_type(String v) { this.phone_type = v; }
    public String getValue() { return value; } public void setValue(String v) { this.value = v; }
    public String getCompany_name() { return company_name; } public void setCompany_name(String v) { this.company_name = v; }
    public String getWebsite() { return website; } public void setWebsite(String v) { this.website = v; }
    public String getAddress() { return address; } public void setAddress(String v) { this.address = v; }
    public String getCity() { return city; } public void setCity(String v) { this.city = v; }
    public String getCountry() { return country; } public void setCountry(String v) { this.country = v; }
    public String getState() { return state; } public void setState(String v) { this.state = v; }
    public Integer getZip() { return zip; } public void setZip(Integer v) { this.zip = v; }
    public String getStreet() { return street; } public void setStreet(String v) { this.street = v; }
    public Set<Device> getDevice_global_vendor() { return device_global_vendor; } public void setDevice_global_vendor(Set<Device> d) { this.device_global_vendor = d; }
    public Set<Device> getDevice_local_vendor() { return device_local_vendor; } public void setDevice_local_vendor(Set<Device> d) { this.device_local_vendor = d; }
    public Set<Device> getDevice_other_vendor_1() { return device_other_vendor_1; } public void setDevice_other_vendor_1(Set<Device> d) { this.device_other_vendor_1 = d; }
    public Set<Device> getDevice_other_vendor_2() { return device_other_vendor_2; } public void setDevice_other_vendor_2(Set<Device> d) { this.device_other_vendor_2 = d; }
    public Set<Device> getDevice_other_vendor_3() { return device_other_vendor_3; } public void setDevice_other_vendor_3(Set<Device> d) { this.device_other_vendor_3 = d; }
}