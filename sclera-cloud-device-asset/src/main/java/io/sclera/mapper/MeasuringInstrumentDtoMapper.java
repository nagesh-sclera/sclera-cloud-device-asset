package io.sclera.mapper;

import io.sclera.dto.MeasuringInstrumentDTO;
import io.sclera.models.MeasuringInstrument;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link MeasuringInstrument} to {@link MeasuringInstrumentDTO} for the single-instrument read
 * ({@code getInstrumentByInstrumentId}). With {@code ignoreByDefault = true} only the columns the old
 * {@code instrumentmapping} @ConstructorResult projected are populated, so the output stays
 * byte-identical to the native query (unselected fields remain null).
 */
@Mapper(componentModel = "spring")
public interface MeasuringInstrumentDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id",               source = "id")
    @Mapping(target = "type",             source = "type")
    @Mapping(target = "name",             source = "name")
    @Mapping(target = "calculation_type", source = "calculation_type")
    @Mapping(target = "category",         source = "category")
    @Mapping(target = "value",            source = "value")
    @Mapping(target = "unit",             source = "unit")
    // device_id column is the FK backing the @ManyToOne Device relation
    @Mapping(target = "device_id",        source = "device.id")
    @Mapping(target = "sensor_type",      source = "sensor_type")
    MeasuringInstrumentDTO toDto(MeasuringInstrument mi);
}
