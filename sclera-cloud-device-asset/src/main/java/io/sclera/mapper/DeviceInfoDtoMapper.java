package io.sclera.mapper;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.projection.DeviceInfoRow;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigInteger;

/**
 * Maps {@link DeviceInfoRow} to {@link DeviceDTO} for the call-status single-device read
 * ({@code getDeviceInfoFromDb}). With {@code ignoreByDefault = true} only the explicitly listed
 * targets are populated, so the output matches the old {@code devicedetailesforcallstatusmapping}
 * @ConstructorResult exactly (25 columns; unselected fields stay null).
 */
@Mapper(componentModel = "spring")
public interface DeviceInfoDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    // --- device scalar columns (entity fields are snake_case) ---
    @Mapping(target = "id",               source = "device.id")
    @Mapping(target = "alarm",            source = "device.alarm",         qualifiedByName = "intToStr")
    @Mapping(target = "user_data_name",   source = "device.user_data_name")
    @Mapping(target = "display_name",     source = "device.display_name")
    @Mapping(target = "ip_address",       source = "device.ip_address")
    @Mapping(target = "mac_address",      source = "device.mac_address")
    @Mapping(target = "last_seen_on",     source = "device.last_seen_on",  qualifiedByName = "bigIntToStr")
    @Mapping(target = "model",            source = "device.model")
    @Mapping(target = "user_data_model",  source = "device.user_data_model")
    @Mapping(target = "user_data_vendor", source = "device.user_data_vendor")
    @Mapping(target = "vendor",           source = "device.vendor")
    @Mapping(target = "warranty",         source = "device.warranty")
    @Mapping(target = "serial_number",    source = "device.serial_number")
    @Mapping(target = "parent",           source = "device.parent")
    @Mapping(target = "description",      source = "device.description")
    @Mapping(target = "category",         source = "device.category")
    @Mapping(target = "sub_category",     source = "device.sub_category")
    // --- docker association FK + LEFT-JOINed location scalars (from the projection record) ---
    @Mapping(target = "docker_name",      source = "dockerName")
    @Mapping(target = "docker_vdms_id",   source = "vdmsId")
    @Mapping(target = "building_id",      source = "buildingId")
    @Mapping(target = "building",         source = "building")
    @Mapping(target = "floor_id",         source = "floorId")
    @Mapping(target = "floor",            source = "floor")
    @Mapping(target = "location_id",      source = "locationId")
    @Mapping(target = "location",         source = "location")
    DeviceDTO toDto(DeviceInfoRow row);

    /** bigint column rendered as String, null-safe — matches the native @ColumnResult(String) cast. */
    @Named("bigIntToStr")
    default String bigIntToStr(BigInteger v) { return v == null ? null : v.toString(); }

    /** integer column rendered as String, null-safe — matches the native @ColumnResult(String) cast. */
    @Named("intToStr")
    default String intToStr(Integer v) { return v == null ? null : v.toString(); }
}
