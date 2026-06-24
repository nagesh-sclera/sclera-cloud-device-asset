package io.sclera.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.ProfileDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Profile.class)

@SqlResultSetMapping(
        name = "userprofilemapping",
        classes = {
                @ConstructorResult(
                        targetClass = ProfileDTO.class,
                        columns = {
                                @ColumnResult(name = "profile_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "is_primary", type = Boolean.class),
                                @ColumnResult(name = "customer_org_id", type = String.class)
                        }
                )
        }
)


@SqlResultSetMapping(
        name = "vendorprofilemapping",
        classes = {
                @ConstructorResult(
                        targetClass = ProfileDTO.class,
                        columns = {
                                @ColumnResult(name = "profile_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "vendor_org_id", type = String.class),
                                @ColumnResult(name = "is_primary", type = Boolean.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Profile.getProfilesByCustomerOrganisationId",
        query = "SELECT id AS profile_id ,name ,is_primary ,customer_org_id FROM profile WHERE customer_org_id = ?1 ",
        resultSetMapping = "userprofilemapping"
)


@NamedNativeQuery(
        name = "Profile.getProfilesByVendorOrganisationId",
        query = "SELECT id AS profile_id ,name ,is_primary ,vendor_org_id FROM profile WHERE vendor_org_id = ?1 ",
        resultSetMapping = "vendorprofilemapping"
)


@Getter
@Setter
public class Profile {

    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 64)
    private String name;

    private Boolean is_primary;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private Set<ProfileUser> profile_users;

    @ManyToOne
    private Customer_Organisation customer_org;

    @Column(length = 64)
    private String vendor_org_id;


}
