package io.sclera.model;

import io.sclera.dto.DigitalTwinMeasuringInstrumentDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@SqlResultSetMapping(
        name = "digitalTwinMeasuringInstrumentsListMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DigitalTwinMeasuringInstrumentDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "digital_twin_position", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "groupedMeasuringInstrumentId", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "DigitalTwinMeasuringInstrument.getDigitalTwinMeasuringInstrumentsByDigitalTwinTemplateId",
        query = "SELECT id, name, digital_twin_position, measuring_instruments_type AS type, grouped_measuring_instrument_id AS groupedMeasuringInstrumentId " +
                "FROM digital_twin_measuring_instrument " +
                "WHERE digital_twin_template_id = ?1",
        resultSetMapping = "digitalTwinMeasuringInstrumentsListMapping"
)

@Entity
@Getter
@Setter
public class DigitalTwinMeasuringInstrument {

    @Id
    private String id;
    @Column(length = 128)
    private String name;
    private String measuringInstrumentsType;
    private String digital_twin_position;
    private String groupedMeasuringInstrumentId;
    @ManyToOne
    private DigitalTwinTemplate digitalTwinTemplate;
}
