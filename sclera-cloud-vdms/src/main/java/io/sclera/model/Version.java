package io.sclera.model;

import io.sclera.dto.VersionDTO;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@SqlResultSetMapping(
        name = "versionMapping",
        classes = {
                @ConstructorResult(
                        targetClass = VersionDTO.class,
                        columns = {
                                @ColumnResult(name = "version", type = String.class),
                                @ColumnResult(name = "link", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Version.getVdmsVersion",
        query = "SELECT version ,link FROM version ",
        resultSetMapping = "versionMapping"
)


@Getter
@Setter
public class Version {

    @Id
    private String version;

    private String link;


}
