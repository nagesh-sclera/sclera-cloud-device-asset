package io.sclera.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Bridges the Spring-managed technician-availability mapper into Spring Data repository default methods. */
@Component
public class TechnicianAvailabilityMapperHolder {
    public static TechnicianAvailabilityDtoMapper MAPPER;

    @Autowired
    public TechnicianAvailabilityMapperHolder(TechnicianAvailabilityDtoMapper mapper) {
        TechnicianAvailabilityMapperHolder.MAPPER = mapper;
    }
}
