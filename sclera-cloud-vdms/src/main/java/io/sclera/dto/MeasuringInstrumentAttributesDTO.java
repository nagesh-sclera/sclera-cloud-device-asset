package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MeasuringInstrumentAttributesDTO {
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
    private Integer attribute_index;
    private String measuring_instruments_type;
    private String grouped_measuring_instruments_id;

    public MeasuringInstrumentAttributesDTO(String name, String type, String unit, String value, String protocol,
                                            String category, String primary_id, String secondary_id, String tertiary_id, Integer attribute_index) {
        this.name = name;
        this.type = type;
        this.unit = unit;
        this.value = value;
        this.protocol = protocol;
        this.category = category;
        this.primary_id = primary_id;
        this.secondary_id = secondary_id;
        this.tertiary_id = tertiary_id;
        this.attribute_index = attribute_index;
    }
}
