package io.sclera.model;

import io.sclera.dto.ScleraFXDTO;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@Entity

@SqlResultSetMapping(
        name = "sclerafxmapping",
        classes = {
                @ConstructorResult(
                        targetClass = ScleraFXDTO.class,
                        columns = {
                                @ColumnResult(name = "os" , type = String.class),
                                @ColumnResult(name = "version" , type = String.class)
                        }
                )
        }
)





@NamedNativeQuery(
        name = "ScleraFX.getAllScleraFXVersions",
        query = "SELECT os ,version FROM sclerafx ",
        resultSetMapping = "sclerafxmapping"
)

public class ScleraFX {

    @Id
    @Column(length = 16)
    private String OS;

    @Column(length = 10 ,columnDefinition = "varchar(255) default '1.0.0'")
    private String version;




}
