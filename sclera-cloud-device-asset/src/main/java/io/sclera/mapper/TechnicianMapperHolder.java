package io.sclera.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Bridges the Spring-managed technician mapper into Spring Data repository default methods. */
@Component
public class TechnicianMapperHolder {
    public static TechnicianDtoMapper MAPPER;

    @Autowired
    public TechnicianMapperHolder(TechnicianDtoMapper mapper) {
        TechnicianMapperHolder.MAPPER = mapper;
    }
}
