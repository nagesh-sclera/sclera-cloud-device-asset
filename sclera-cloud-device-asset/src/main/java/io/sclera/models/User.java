package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.touchscreen.settings.UserDTO;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@SqlResultSetMapping(
        name = "usermapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "phone", type = String.class),
                                @ColumnResult(name = "phone_type", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "created_by", type = String.class),
                                @ColumnResult(name = "company_name", type = String.class),
                                @ColumnResult(name = "website", type = String.class),
                                @ColumnResult(name = "address_id", type = String.class),
                                @ColumnResult(name = "organisation_id", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "creation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "language", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "User.getAllUsers",
        query = "SELECT u.email, u.creation_timestamp, u.name, u.phone, u.phone_type, u.value,u.created_by,  u.company_name,u.website, u.address_id, u.customer_org_id as organisation_id, u.image_url, u.language "
                + " FROM user u WHERE (?3 = 'null' or CONCAT_WS('' , u.name, u.email ) LIKE CONCAT('%' ,?3, '%')) LIMIT ?1  OFFSET ?2",
        resultSetMapping = "usermapping"
)

@NamedNativeQuery(
        name = "User.getAllOrganisationUsersByPagination",
        query = "SELECT u.email, u.creation_timestamp, u.name, u.phone, u.phone_type, u.value,u.created_by,  u.company_name,u.website, u.address_id, u.customer_org_id as organisation_id, u.image_url, u.language "
                + " FROM user u"
                + " LEFT JOIN customer_organisation co on co.id = u.customer_org_id"
                + " WHERE u.customer_org_id = ?4 AND (?3 = 'null' or CONCAT_WS('' , u.name, u.email ) LIKE CONCAT('%' ,?3, '%')) LIMIT ?1  OFFSET ?2",
        resultSetMapping = "usermapping"
)

@NamedNativeQuery(
        name = "User.getAllOtherUsersByPagination",
        query = "SELECT u.email, u.creation_timestamp, u.name, u.phone, u.phone_type, u.value,u.created_by,  u.company_name,u.website, u.address_id, u.customer_org_id as organisation_id, u.image_url, u.language "
                + " FROM user u"
                + " WHERE u.customer_org_id IS NULL AND (?3 = 'null' or CONCAT_WS('' , u.name, u.email ) LIKE CONCAT('%' ,?3, '%')) LIMIT ?1  OFFSET ?2",
        resultSetMapping = "usermapping"
)

@NamedNativeQuery(
        name = "User.getUserByEmail",
        query = "SELECT u.email, u.creation_timestamp, u.name, u.phone, u.phone_type, u.value,u.created_by,  u.company_name,u.website, u.address_id, u.customer_org_id as organisation_id, u.image_url, u.language "
                + " FROM user u WHERE u.email = ?1",
        resultSetMapping = "usermapping"
)

@NamedNativeQuery(
        name = "User.getAllUsersByOrganisationId",
        query = "SELECT u.email, u.creation_timestamp, u.name, u.phone, u.phone_type, u.value,u.created_by,  u.company_name,u.website, u.address_id, u.customer_org_id as organisation_id, u.image_url, u.language "
                + " FROM user u"
                + " LEFT JOIN customer_organisation co on co.id = u.customer_org_id"
                + " WHERE u.customer_org_id = ?1",
        resultSetMapping = "usermapping"
)

@NamedNativeQuery(
        name = "User.getUsers",
        query = "SELECT u.email, u.creation_timestamp, u.name, u.phone, u.phone_type, u.value,u.created_by,  u.company_name,u.website, u.address_id, u.customer_org_id as organisation_id, u.image_url, u.language "
                + " FROM user u ",
        resultSetMapping = "usermapping"
)

//get user data
@SqlResultSetMapping(
        name = "userNameMapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserDTO.class,
                        columns ={
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "User.getAssigneeDetails",
        query = "SELECT email, name, image_url FROM `user` WHERE email IN ?1",
        resultSetMapping = "userNameMapping"
)


@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "email", scope = User.class)
public class User {
    @Id
    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String company_name;

    private BigInteger creation_timestamp;

    @Column(length = 255)
    private String name;

    @Column(length = 255)
    private String phone;

    @Column(length = 255)
    private String phone_type;

    @Column(length = 255)
    private String value;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String created_by;

    @Column(columnDefinition = "TEXT")
    private String image_url;


    @Column(columnDefinition = "varchar(16) default 'EN'")
    private String language;


    @OneToOne(cascade = CascadeType.ALL)
    private Address address;

    // removed: relation to Bucket-C entity Customer_Organisation (CP-2)
    // removed: relation to Bucket-C entity ProfileUser (CP-2)
    // removed: relation to Bucket-C entity UserSettings (CP-2)
    // removed: relation to Bucket-C entity Ticket (AP-C3)

    @Column(length = 255)
    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Device> devices;

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }





    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public BigInteger getCreation_timestamp() {
        return creation_timestamp;
    }

    public void setCreation_timestamp(BigInteger creation_timestamp) {
        this.creation_timestamp = creation_timestamp;
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }





    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


}
