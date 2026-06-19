package io.sclera.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Bridges the Spring-managed MeasuringInstrument mapper into Spring Data repository default methods. */
@Component
public class MeasuringInstrumentMapperHolder {
    public static MeasuringInstrumentDtoMapper MAPPER;

    @Autowired
    public MeasuringInstrumentMapperHolder(MeasuringInstrumentDtoMapper mapper) {
        MeasuringInstrumentMapperHolder.MAPPER = mapper;
    }
}
