package io.sclera.mapper;

import io.sclera.dto.DeviceDTO;
import io.sclera.models.Device;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link Device} to {@link DeviceDTO} for the device-images read ({@code getAllDeviceImages}).
 * With {@code ignoreByDefault = true} only the explicitly listed targets are populated, so the output
 * matches the old {@code deviceImageMapping} @ConstructorResult exactly (4 columns; unselected fields
 * stay null). Single-table query — all sources are {@link Device} scalar fields, no type mismatch.
 */
@Mapper(componentModel = "spring")
public interface DeviceImagesDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    // --- device scalar columns (entity fields are snake_case; types match the DTO directly) ---
    @Mapping(target = "ai_call",              source = "ai_call")
    @Mapping(target = "asset_image_url",      source = "asset_image_url")
    @Mapping(target = "asset_ocr_image_url",  source = "asset_ocr_image_url")
    @Mapping(target = "asset_tag_images_url", source = "asset_tag_images_url")
    DeviceDTO toDto(Device device);
}
