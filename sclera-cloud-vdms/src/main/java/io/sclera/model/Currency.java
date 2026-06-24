package io.sclera.model;

import io.sclera.dto.CurrencyDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity


@SqlResultSetMapping(
        name = "currencyMapping",
        classes = {
                @ConstructorResult(
                        targetClass = CurrencyDTO.class,
                        columns = {
                                @ColumnResult(name = "currencyId", type = String.class),
                                @ColumnResult(name = "code", type = String.class),
                                @ColumnResult(name = "country", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Currency.getCurrency",
        query = "SELECT id AS currencyId,code,country FROM currency WHERE (?1 = 'all' OR CONCAT_WS('',code,country) " +
                "LIKE CONCAT('%',?1,'%')) LIMIT ?2 OFFSET ?3",
        resultSetMapping = "currencyMapping"
)


@Getter
@Setter
public class Currency {

    @Id
    private String id;
    private String code;
    private String country;

}
