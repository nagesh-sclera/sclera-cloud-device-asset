package io.sclera.model;


import io.sclera.dto.CategoryDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;



@SqlResultSetMapping(
        name = "procedureMapping",
        classes = {
                @ConstructorResult(
                        targetClass = CategoryDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "iconUrl", type = String.class),
                                @ColumnResult(name = "displayName", type = String.class),
                                @ColumnResult(name = "creationTimestamp", type = BigInteger.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Procedures.getAllProcedure",
        query = "SELECT id, name, icon_url AS iconUrl, display_name AS displayName, creation_timestamp AS creationTimestamp " +
                "FROM procedures " +
                "WHERE (?1 = 'all' OR CONCAT_WS('',display_name) LIKE CONCAT('%',?1,'%')) " +
                "ORDER BY " +
                "CASE " +
                "   WHEN ?2 = 'creation_timestamp' THEN creation_timestamp " +
                "   ELSE NULL " +
                "END DESC, " +
                "CASE " +
                "   WHEN ?2 = 'name' THEN name " +
                "   ELSE NULL " +
                "END ASC " +
                "LIMIT ?3 OFFSET ?4",
        resultSetMapping = "procedureMapping"
)


@Entity
@Getter
@Setter
public class Procedures {

    @Id
    private String id;
    @Column(length = 128)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String iconUrl;
    @Column(length = 128)
    private String display_name;
    private BigInteger creation_timestamp;

}
