package io.sclera.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.AddressDTO;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Address.class)

@SqlResultSetMapping(
        name = "addressMapping",
        classes = {
                @ConstructorResult(
                        targetClass = AddressDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "address", type = String.class),
                                @ColumnResult(name = "city", type = String.class),
                                @ColumnResult(name = "state", type = String.class),
                                @ColumnResult(name = "zip", type = String.class),
                                @ColumnResult(name = "country", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Address.getAddressDetailsById",
        query = "SELECT a.id , a.address ,a.city ,a.state ,a.zip ,a.country FROM address a WHERE a.id = ?1 ",
        resultSetMapping = "addressMapping"
)


@Getter
@Setter
public class Address {

    @Id
    private String id;

    @Column(length = 128)
    private String address;

    @Column(length = 64)
    private String city;

    @Column(length = 64)
    private String country;

    @Column(length = 64)
    private String state;

    @Column(length = 8)
    private String zip;

    @OneToOne(mappedBy = "address")
    private User user;

    @OneToOne(mappedBy = "address")
    private Vdms vdms;

    public void setUser(User user) {
        this.user = user;
        user.setAddress(this);
    }


}
