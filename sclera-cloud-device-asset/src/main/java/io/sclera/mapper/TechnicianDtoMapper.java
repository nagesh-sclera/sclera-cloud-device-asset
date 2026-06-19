package io.sclera.mapper;

import io.sclera.dto.TechnicianDTO;
import io.sclera.models.Technician;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link Technician} to {@link TechnicianDTO} for the single-technician read. With
 * {@code ignoreByDefault = true} only the explicitly listed targets are populated, so the output
 * matches the old {@code technicianMapping} @ConstructorResult of {@code Technician.getTechnicianById}
 * exactly (unselected fields stay null).
 *
 * <p>Projected columns (native SELECT): id, email, phone, country_code AS countryCode, name,
 * department, designation, time_zone AS timeZone, created_by AS createdBy, created_at AS createdAt,
 * vdms_id AS vdmsId. All entity fields are already same-typed Strings (createdAt is Long), so no
 * converters are needed. {@code vdmsId} has no scalar entity field — it is the FK of the
 * {@code @ManyToOne Vdms vdms} association, sourced via {@code vdms.id} (null-safe).
 */
@Mapper(componentModel = "spring")
public interface TechnicianDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id",          source = "id")
    @Mapping(target = "email",       source = "email")
    @Mapping(target = "phone",       source = "phone")
    @Mapping(target = "countryCode", source = "countryCode")
    @Mapping(target = "name",        source = "name")
    @Mapping(target = "department",  source = "department")
    @Mapping(target = "designation", source = "designation")
    @Mapping(target = "timeZone",    source = "timeZone")
    @Mapping(target = "createdBy",   source = "createdBy")
    @Mapping(target = "createdAt",   source = "createdAt")
    @Mapping(target = "vdmsId",      source = "vdms.id")
    TechnicianDTO toDto(Technician t);
}
