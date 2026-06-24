package io.sclera.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.UserDTO;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "email", scope = User.class)

@SqlResultSetMapping(
        name = "usermapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "company_name", type = String.class),
                                @ColumnResult(name = "is_enterprise", type = Integer.class),
                                @ColumnResult(name = "creation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "phone", type = String.class),
                                @ColumnResult(name = "phone_type", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "website", type = String.class),
                                @ColumnResult(name = "organisation_id", type = String.class),
                                @ColumnResult(name = "role", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "address", type = String.class),
                                @ColumnResult(name = "city", type = String.class),
                                @ColumnResult(name = "country", type = String.class),
                                @ColumnResult(name = "state", type = String.class),
                                @ColumnResult(name = "zip", type = String.class),
                                @ColumnResult(name = "time_zone", type = String.class)
                        }
                )
        }
)


@SqlResultSetMapping(
        name = "userdatamapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "company_name", type = String.class),
                                @ColumnResult(name = "is_enterprise", type = Integer.class),
                                @ColumnResult(name = "creation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "phone", type = String.class),
                                @ColumnResult(name = "phone_type", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "website", type = String.class),
                                @ColumnResult(name = "organisation_id", type = String.class),
                                @ColumnResult(name = "role", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "address", type = String.class),
                                @ColumnResult(name = "city", type = String.class),
                                @ColumnResult(name = "country", type = String.class),
                                @ColumnResult(name = "state", type = String.class),
                                @ColumnResult(name = "zip", type = String.class),
                                @ColumnResult(name = "activation_status", type = String.class),
                                @ColumnResult(name = "last_updated", type = BigInteger.class)
                        }
                )
        }
)


@SqlResultSetMapping(
        name = "userlistmapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "organisation_id", type = String.class),
                                @ColumnResult(name = "role", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class)
                        }
                )
        }
)
@SqlResultSetMapping(
        name = "userMappingWithoutOrg",
        classes = {
                @ConstructorResult(
                        targetClass = UserDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "creation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "phone", type = String.class),
                                @ColumnResult(name = "phone_type", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "website", type = String.class),
                                @ColumnResult(name = "organisation_id", type = String.class),
                                @ColumnResult(name = "role", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "address", type = String.class),
                                @ColumnResult(name = "city", type = String.class),
                                @ColumnResult(name = "country", type = String.class),
                                @ColumnResult(name = "state", type = String.class),
                                @ColumnResult(name = "zip", type = String.class),
                                @ColumnResult(name = "timeZone", type = String.class),
                                @ColumnResult(name = "language", type = String.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "usersyncmapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "company_name", type = String.class),
                                @ColumnResult(name = "is_enterprise", type = Integer.class),
                                @ColumnResult(name = "creation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "phone", type = String.class),
                                @ColumnResult(name = "phone_type", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "website", type = String.class),
                                @ColumnResult(name = "organisation_id", type = String.class),
                                @ColumnResult(name = "role", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "address", type = String.class),
                                @ColumnResult(name = "city", type = String.class),
                                @ColumnResult(name = "country", type = String.class),
                                @ColumnResult(name = "state", type = String.class),
                                @ColumnResult(name = "zip", type = String.class),
                                @ColumnResult(name = "timeZone", type = String.class),
                                @ColumnResult(name = "language", type = String.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "userImageMapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "User.getAllUserInfoByOrganisationId",
        query = "SELECT u.email ,co.company_name ,co.is_enterprise ,u.creation_timestamp ,u.name , u.phone ,u.phone_type ,u.value ,u.website ,"
                + "u.customer_org_id AS organisation_id , u.role ,u.image_url ,a.address ,a.city ,a.country ,a.state ,a.zip,u.time_zone"
                + " FROM user u " +
                " LEFT JOIN customer_organisation co ON co.id = u.customer_org_id " +
                " LEFT JOIN address a ON a.id = u.address_id " +
                " WHERE ((u.customer_org_id = ?1) " +
                " AND (?2 = 'all' OR CONCAT_WS('',u.name,u.email,u.role) LIKE CONCAT('%',?2,'%'))) " +
                " LIMIT ?3 OFFSET ?4",
        resultSetMapping = "usermapping"
)


@NamedNativeQuery(
        name = "User.getAllUserListByOrganisationId",
        query = "SELECT u.email ,u.customer_org_id AS organisation_id ,u.role ,u.image_url " +
                "FROM user u " +
                "WHERE (u.customer_org_id = ?1 " +
                "AND (?2 = 'all' " +
                "OR CONCAT_WS('',u.name,u.email,u.role) LIKE CONCAT('%',?2,'%'))) " +
                "LIMIT ?3 OFFSET ?4 ",
        resultSetMapping = "userlistmapping"
)

@NamedNativeQuery(
        name = "User.getAllUserListBySuperAdminEmail",
        query = "SELECT u.email ,u.customer_org_id AS organisation_id ,u.role ,u.image_url " +
                "FROM user u " +
                "WHERE (?1 = 'all' " +
                "OR CONCAT_WS('',u.name,u.email,u.role) LIKE CONCAT('%',?1,'%')) " +
                "LIMIT ?2 OFFSET ?3 ",
        resultSetMapping = "userlistmapping"
)


@NamedNativeQuery(
        name = "User.getAllUserListByAdminEmail",
        query = "SELECT email ,customer_org_id AS organisation_id ,role ,image_url FROM user " +
                "WHERE ((role != 'super-admin' AND role != 'admin') " +
                "AND (?1 = 'all' OR CONCAT_WS('',name,email,role) LIKE CONCAT('%',?1,'%'))) " +
                "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "userlistmapping"
)

@NamedNativeQuery(
        name = "User.getMasterUserInfoByVdmsId",
        query = "SELECT u.email ,co.company_name,co.is_enterprise ,u.creation_timestamp ,u.name , u.phone ,u.phone_type ,u.value ,u.website ,"
                + "u.customer_org_id AS organisation_id ,u.role ,u.image_url ,a.address ,a.city ,a.country ,a.state ,a.zip,u.time_zone "
                + "FROM user u " +
                " LEFT JOIN address a ON a.id = u.address_id LEFT JOIN vdms v ON u.customer_org_id = v.customer_org_id" +
                " LEFT JOIN customer_organisation co ON co.id = u.customer_org_id " +
                " WHERE v.id = ?1 AND u.role = 'master-user' ",
        resultSetMapping = "usermapping"
)

@NamedNativeQuery(
        name = "User.getUserDetailsByOrganisationIdAndUserEmailAndVdmsId",
        query = "SELECT u.email ,co.company_name,co.is_enterprise ,u.creation_timestamp ,u.name , u.phone ,u.phone_type ,u.value ,u.website ,"
                + "u.customer_org_id AS organisation_id ,u.role ,u.image_url , v.activation_status ,v.last_updated ,a.address ,a.city ,"
                + "a.country ,a.state ,a.zip FROM user u "
                + "LEFT JOIN address a ON a.id = u.address_id " +
                " LEFT JOIN customer_organisation co ON co.id = u.customer_org_id "
                + "LEFT JOIN vdms v ON v.customer_org_id = u.customer_org_id "
                + "WHERE u.customer_org_id = ?1 AND u.email = ?2 AND v.id = ?3 ",
        resultSetMapping = "userdatamapping"
)


@NamedNativeQuery(
        name = "User.getUserDetailsByUserEmail",
        query = "SELECT u.email ,co.company_name,co.is_enterprise ,u.creation_timestamp ,u.name , u.phone ,u.phone_type ,u.value ,u.website ,"
                + "u.customer_org_id AS organisation_id ,u.role ,u.image_url ,a.address ,a.city ,a.country ,a.state ,a.zip,u.time_zone"
                + " FROM user u " +
                " LEFT JOIN address a ON a.id = u.address_id " +
                " LEFT JOIN customer_organisation co ON u.customer_org_id = co.id " +
                " WHERE u.email = ?1 ",
        resultSetMapping = "usermapping"
)

@NamedNativeQuery(
        name = "User.getMasterUserDetailsByVdmsId",
        query = "SELECT u.email ,co.company_name,co.is_enterprise ,u.creation_timestamp ,u.name , u.phone ,u.phone_type ,u.value ,u.website ,"
                + "u.customer_org_id AS organisation_id ,u.role ,u.image_url ,a.address ,a.city ,a.country ,a.state ,a.zip,u.time_zone"
                + " FROM user u LEFT JOIN address a ON a.id = u.address_id " +
                " LEFT JOIN vdms v ON u.customer_org_id = v.customer_org_id " +
                " LEFT JOIN customer_organisation co ON u.customer_org_id = co.id " +
                " WHERE u.role = 'master-user' AND v.id = ?1 ",
        resultSetMapping = "usermapping"
)

@NamedNativeQuery(
        name = "User.getMasterUserInfoByOrganisationId",
        query = "SELECT u.email ,co.company_name,co.is_enterprise ,u.creation_timestamp ,u.name , u.phone ,u.phone_type ,u.value ,u.website ,"
                + "u.customer_org_id AS organisation_id ,u.role ,u.image_url ,a.address ,a.city ,a.country ,a.state ,a.zip ,u.time_zone"
                + " FROM user u " +
                " LEFT JOIN address a ON a.id = u.address_id " +
                " LEFT JOIN customer_organisation co ON u.customer_org_id = co.id " +
                "WHERE u.customer_org_id = ?1 AND u.role = 'master-user' ",
        resultSetMapping = "usermapping"
)

@NamedNativeQuery(
        name = "User.getAllUserListByOrgAdmin",
        query = "SELECT u.email ,u.customer_org_id AS organisation_id ,u.role ,u.image_url " +
                "FROM user u " +
                "WHERE (u.customer_org_id = ?1 " +
                "AND (role != 'master-user' AND role != 'org-admin') " +
                "AND (?2 = 'all' " +
                "OR CONCAT_WS('',u.name,u.email,u.role) LIKE CONCAT('%',?2,'%'))) " +
                "LIMIT ?3 OFFSET ?4 ",
        resultSetMapping = "userlistmapping"
)

@NamedNativeQuery(
        name = "User.getAllUserInfoByOrganisationIdAndVdmsId",
        query = "SELECT u.email ,co.company_name,co.is_enterprise ,u.creation_timestamp ,u.name , u.phone ,u.phone_type ,u.value ,u.website ,"
                + "u.customer_org_id AS organisation_id ,u.role ,u.image_url ,a.address ,a.city ,a.country ,a.state ,a.zip ,u.time_zone"
                + " FROM user u " +
                " LEFT JOIN customer_organisation co ON co.id = u.customer_org_id " +
                " LEFT JOIN address a ON a.id = u.address_id " +
                " LEFT JOIN vdms_visibility vv ON u.email = vv.email AND vv.vdms_id= ?2 Where u.customer_org_id = ?1 AND (u.role = 'master-user' OR u.role = 'org-admin' OR vv.email IS NOT NULL) ORDER BY u.email DESC",
        resultSetMapping = "usermapping"
)

@NamedNativeQuery(
        name = "User.getAllUserInfoByOrgId",
        query = "SELECT u.email ,co.company_name ,co.is_enterprise ,u.creation_timestamp ,u.name , u.phone ,u.phone_type ,u.value ,u.website ,"
                + "u.customer_org_id AS organisation_id , u.role ,u.image_url ,a.address ,a.city ,a.country ,a.state ,a.zip,u.time_zone AS timeZone ,u.language"
                + " FROM user u " +
                " LEFT JOIN customer_organisation co ON co.id = u.customer_org_id " +
                " LEFT JOIN address a ON a.id = u.address_id WHERE u.customer_org_id = ?1 ",
        resultSetMapping = "usersyncmapping"
)
@NamedNativeQuery(
        name = "User.getAllUserInfoByOrgIds",
        query = "SELECT u.email ,co.company_name ,co.is_enterprise ,u.creation_timestamp ,u.name , u.phone ,u.phone_type ,u.value ,u.website ,"
                + "u.customer_org_id AS organisation_id , u.role ,u.image_url ,a.address ,a.city ,a.country ,a.state ,a.zip,u.time_zone AS timeZone,u.language"
                + " FROM user u " +
                " LEFT JOIN customer_organisation co ON co.id = u.customer_org_id " +
                " LEFT JOIN address a ON a.id = u.address_id WHERE u.customer_org_id IN (?1) ",
        resultSetMapping = "usersyncmapping"
)
@NamedNativeQuery(
        name = "User.getAllUserInfoWithoutOrg",
        query = "SELECT u.email ,u.creation_timestamp ,u.name , u.phone ,u.phone_type ,u.value ,u.website ,"
                + "u.customer_org_id AS organisation_id , u.role ,u.image_url ,a.address ,a.city ,a.country ,a.state ,a.zip,u.time_zone AS timeZone,u.language"
                + " FROM user u "
                + " LEFT JOIN address a ON a.id = u.address_id WHERE u.customer_org_id IS NULL ",
        resultSetMapping = "userMappingWithoutOrg"
)
@NamedNativeQuery(
        name = "User.getUrlByEmails",
        query = "SELECT email,image_url FROM user ",
        resultSetMapping = "userImageMapping"
)

@SqlResultSetMapping(
        name = "timeusermapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "creation_timestamp", type = BigInteger.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "User.getCreationTimeStamps",
        query = "SELECT u.email,u.creation_timestamp FROM user AS u",
        resultSetMapping = "timeusermapping"
)

public class User {

    @Id
    @Column(length = 64)
    private String email;
    private String id;
    @Column(length = 128)
    private String company_name;

    private BigInteger creation_timestamp;

    @Column(length = 64)
    private String name;

    @Column(length = 18)
    private String phone;

    @Column(length = 16)
    private String phone_type;

    @Column(length = 8)
    private String value;

    @Column(length = 64)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String image_url;

    @OneToOne(cascade = CascadeType.ALL)
    private Address address;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "customer_org_id")
    private Customer_Organisation customer_org;

    private String role;

    @Column(columnDefinition = "varchar(255) default 'Asia/Kolkata'")
    private String timeZone;

    @Column(columnDefinition = "varchar(8) default 'EN'")
    private String language;
}
