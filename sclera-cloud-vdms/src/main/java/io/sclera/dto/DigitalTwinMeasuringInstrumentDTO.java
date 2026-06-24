package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DigitalTwinMeasuringInstrumentDTO {

    private String id;
    private String name;
    private String digital_twin_position;
    private String type;
    private String groupedMeasuringInstrumentId;
    private String parameter;
    private List<Map<String, Object>> parameterJson;
    private String description;
    private String attribute;
    private Map<String, Object> attributeJson;
    private String tags;
    private List<String> tagsJson;
    private String unit;
    private String category;
    private String sensor_type;
    private String calculation_type;
    private String scale_type;
    private List<MeasuringInstrumentAttributesDTO> measuring_instrument_attributes;

    public DigitalTwinMeasuringInstrumentDTO(String id, String name, String digital_twin_position, String type, String groupedMeasuringInstrumentId) {
        this.id = id;
        this.name = name;
        this.digital_twin_position = digital_twin_position;
        this.type = type;
        this.groupedMeasuringInstrumentId = groupedMeasuringInstrumentId;
    }
}
