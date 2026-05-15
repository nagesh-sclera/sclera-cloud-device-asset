package io.sclera.models;

import io.sclera.dto.MeasuringInstrumentAttributesDTO;

import javax.persistence.*;

@SqlResultSetMapping(
        name = "measuringinstrumentattributemapping",
        classes = {
                @ConstructorResult(
                        targetClass = MeasuringInstrumentAttributesDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "unit", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "protocol", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "primary_id", type = String.class),
                                @ColumnResult(name = "secondary_id", type = String.class),
                                @ColumnResult(name = "tertiary_id", type = String.class),
                                @ColumnResult(name = "measuring_instrument_id", type = String.class),
                                @ColumnResult(name = "attribute_index", type = Integer.class)
                        }
                )
        }

)

@NamedNativeQuery(
        name = "MeasuringInstrument_Attributes.getMeasuringInstrumentAttributeById",
        query = "SELECT ma.id,ma.name,ma.type,ma.unit,ma.value,ma.protocol,ma.category,ma.primary_id,ma.secondary_id,ma.tertiary_id,ma.measuring_instrument_id,ma.attribute_index "
                + " FROM measuring_instrument_attributes ma WHERE ma.id=?1",
        resultSetMapping = "measuringinstrumentattributemapping"
)

@NamedNativeQuery(
        name = "MeasuringInstrument_Attributes.getAllMeasuringInstrumentAttributes",
        query = "SELECT ma.id, ma.name, ma.type, ma.unit, ma.value, ma.protocol, ma.category, ma.primary_id, ma.secondary_id, ma.tertiary_id, ma.measuring_instrument_id, ma.attribute_index "
                + " FROM measuring_instrument_attributes ma ORDER BY ma.attribute_index",
        resultSetMapping = "measuringinstrumentattributemapping"
)

@NamedNativeQuery(
        name = "MeasuringInstrument_Attributes.getMeasuringInstrumentAttributesByMeasuringInstrumentId",
        query = "SELECT ma.id,ma.name,ma.type,ma.unit,ma.value,ma.protocol,ma.category,ma.primary_id,ma.secondary_id,ma.tertiary_id,ma.measuring_instrument_id,ma.attribute_index "
                + " FROM measuring_instrument_attributes ma "
                + " WHERE ma.measuring_instrument_id=?1 ORDER BY ma.attribute_index",
        resultSetMapping = "measuringinstrumentattributemapping"
)
@Entity
public class MeasuringInstrument_Attributes {
    @Id
    private String id;
    private String name;
    private String type;
    private String unit;
    private String value;
    private String protocol;
    private String category;
    private String primary_id;
    private String secondary_id;
    private String tertiary_id;
    @ManyToOne
    private MeasuringInstrument measuring_instrument;
    private Integer attribute_index;

}
