package io.sclera.mapper;

import io.sclera.dto.TechnicianAvailabilityDTO;
import io.sclera.models.TechnicianAvailability;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link TechnicianAvailability} to {@link TechnicianAvailabilityDTO} for the single-record read
 * ({@code TechnicianAvailability.getTechnicianAvailabilityById}). With {@code ignoreByDefault = true}
 * only the explicitly listed targets are populated, so the output is byte-identical to the old
 * {@code technicianAvailabilityMapping} @ConstructorResult (unselected fields stay null).
 *
 * <p>Native SELECT projects, in column order: {@code id, start_date, end_date, start_time, end_time,
 * is_all_day, frequency, condition, technician_id} (9 columns). The {@code @ConstructorResult} binds
 * these <em>positionally</em> to the DTO constructor's first 9 parameters
 * {@code (id, startDate, endDate, startTime, endTime, isAllDay, frequency, technicianId, condition)} —
 * so column #8 {@code condition} lands in the {@code technicianId} slot and column #9
 * {@code technician_id} lands in the {@code condition} slot. This swap is preserved below to keep the
 * output identical. The 10th constructor param {@code sync} is not projected and stays null.
 *
 * <p>All projected entity fields are already same-typed (String/Long/Boolean) so no converters are
 * needed. {@code technician_id} has no scalar entity field — it is the FK of the
 * {@code @ManyToOne Technician technician} association, sourced null-safely via {@code technician.id}.
 */
@Mapper(componentModel = "spring")
public interface TechnicianAvailabilityDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id",           source = "id")            // col 1: id
    @Mapping(target = "startDate",    source = "startDate")     // col 2: start_date
    @Mapping(target = "endDate",      source = "endDate")       // col 3: end_date
    @Mapping(target = "startTime",    source = "startTime")     // col 4: start_time
    @Mapping(target = "endTime",      source = "endTime")       // col 5: end_time
    @Mapping(target = "isAllDay",     source = "isAllDay")      // col 6: is_all_day
    @Mapping(target = "frequency",    source = "frequency")     // col 7: frequency
    @Mapping(target = "technicianId", source = "condition")     // col 8 (condition) -> param #8 technicianId
    @Mapping(target = "condition",    source = "technician.id") // col 9 (technician_id) -> param #9 condition
    TechnicianAvailabilityDTO toDto(TechnicianAvailability t);
}
