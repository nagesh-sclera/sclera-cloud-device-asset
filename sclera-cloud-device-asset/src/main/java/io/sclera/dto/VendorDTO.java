package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VendorDTO {

    private String email;
    private String name;
    private String phone;
    private String phone_type;
    private String value;
    private String company_name;
    private String website;
    private String address;
    private String city;
    private String country;
    private String state;
    private Integer zip;
    private String role;
    private String organisation_id;
    private String org_status;
    private String image_url;
    //tempory
    private String docker_name;
    private Integer otp;






    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public Integer getZip() {
        return zip;
    }
    public void setZip(Integer zip) {
        this.zip = zip;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getOrganisation_id() {
        return organisation_id;
    }
    public void setOrganisation_id(String organisation_id) {
        this.organisation_id = organisation_id;
    }
    public String getDocker_name() {
        return docker_name;
    }
    public void setDocker_name(String docker_name) {
        this.docker_name = docker_name;
    }
    public Integer getOtp() {
        return otp;
    }
    public void setOtp(Integer otp) {
        this.otp = otp;
    }
    public String getOrg_status() {
        return org_status;
    }
    public void setOrg_status(String org_status) {
        this.org_status = org_status;
    }
    public String getImage_url() {
        return image_url;
    }
    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public VendorDTO() {
        super();
    }

    public VendorDTO(String email, String name, String phone, String phone_type, String value, String company_name,
                     String website, String address, String city, String country, String state, Integer zip, String role,
                     String organisation_id, String image_url ) {
        super();
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.phone_type = phone_type;
        this.value = value;
        this.company_name = company_name;
        this.website = website;
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.zip = zip;
        this.role = role;
        this.organisation_id = organisation_id;
        this.image_url = image_url;
    }


    @Override
    public String toString() {
        return "VendorDTO [email=" + email + ", name=" + name + ", phone=" + phone + ", phone_type=" + phone_type
                + ", value=" + value + ", company_name=" + company_name + ", website=" + website + ", address="
                + address + ", city=" + city + ", country=" + country + ", state=" + state + ", zip=" + zip + ", role="
                + role + ", organisation_id=" + organisation_id + ", org_status=" + org_status + ", image_url="
                + image_url + ", docker_name=" + docker_name + ", otp=" + otp + "]";
    }







}
