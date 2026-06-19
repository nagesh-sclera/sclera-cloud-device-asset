package io.sclera.mapper;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.projection.DeviceByInstrumentRow;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link DeviceByInstrumentRow} to {@link DeviceDTO} for the IAQ measuring-instrument lookup
 * ({@code getDeviceByMeasuringInstrumentId}). With {@code ignoreByDefault = true} only the
 * explicitly listed targets are populated, so the output matches the old {@code deviceforiaqMapping}
 * @ConstructorResult exactly (7 columns; unselected fields stay null).
 */
@Mapper(componentModel = "spring")
public interface DeviceByInstrumentDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    // --- device scalar columns (entity fields are snake_case) ---
    @Mapping(target = "id",             source = "device.id")
    @Mapping(target = "asset_group",    source = "device.asset_group")
    @Mapping(target = "user_data_name", source = "device.user_data_name")
    @Mapping(target = "display_name",   source = "device.display_name")
    @Mapping(target = "onboard_status", source = "device.onboard_status")
    // --- association-FK scalars (from the projection record) ---
    @Mapping(target = "docker_name",    source = "dockerName")
    @Mapping(target = "location_id",    source = "locationId")
    DeviceDTO toDto(DeviceByInstrumentRow row);
}
