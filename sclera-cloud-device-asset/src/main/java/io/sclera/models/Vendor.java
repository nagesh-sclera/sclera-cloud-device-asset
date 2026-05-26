package io.sclera.models;

// Minimal compatibility stub for the extracted service — only columns referenced by native queries (loose coupling, scalar FKs).

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vendor")
public class Vendor {

    @Id
    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String name;

    @Column(length = 255)
    private String phone;

    @Column(length = 255)
    private String phone_type;

    @Column(length = 256)
    private String value;

    @Column(length = 255)
    private String company_name;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String address;

    @Column(length = 64)
    private String city;

    @Column(length = 64)
    private String country;

    @Column(length = 64)
    private String state;

    @Column(length = 255)
    private Integer zip;

    @Column(length = 64)
    private String street;

    @Column(length = 255)
    private String role;

    @Column(length = 255)
    private String image_url;

    /** Scalar FK — replaces @ManyToOne Vendor_Organisation (loose coupling). */
    @Column(length = 255)
    private String vendor_org_id;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPhone_type() { return phone_type; }
    public void setPhone_type(String phone_type) { this.phone_type = phone_type; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getCompany_name() { return company_name; }
    public void setCompany_name(String company_name) { this.company_name = company_name; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Integer getZip() { return zip; }
    public void setZip(Integer zip) { this.zip = zip; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getImage_url() { return image_url; }
    public void setImage_url(String image_url) { this.image_url = image_url; }

    public String getVendor_org_id() { return vendor_org_id; }
    public void setVendor_org_id(String vendor_org_id) { this.vendor_org_id = vendor_org_id; }
}
