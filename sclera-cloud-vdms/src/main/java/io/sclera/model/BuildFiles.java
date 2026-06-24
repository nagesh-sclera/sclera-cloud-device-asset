package io.sclera.model;

import io.sclera.dto.BuildFilesDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@SqlResultSetMapping(
        name = "buildFilesMapping",
        classes = {
                @ConstructorResult(
                        targetClass = BuildFilesDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "version", type = String.class),
                                @ColumnResult(name = "link", type = String.class),
                                @ColumnResult(name = "type", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "BuildFiles.getBuildFiles",
        query = "SELECT id,version ,link ,type FROM build_files WHERE type = ?1 ",
        resultSetMapping = "buildFilesMapping"
)


@Getter
@Setter
public class BuildFiles {

    @Id
    private String id;

    @Column(length = 16)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String link;

    @Column(length = 64)
    private String type;

}