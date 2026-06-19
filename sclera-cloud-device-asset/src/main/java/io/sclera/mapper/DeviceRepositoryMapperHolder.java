package io.sclera.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Bridges the Spring-managed mappers into Spring Data repository default methods. */
@Component
public class DeviceRepositoryMapperHolder {
    public static DeviceDtoMapper MAPPER;
    public static DeviceOnboardDtoMapper ONBOARD_MAPPER;
    public static DeviceInfoDtoMapper INFO_MAPPER;
    public static DeviceByInstrumentDtoMapper INSTRUMENT_MAPPER;
    public static DeviceImagesDtoMapper IMAGES_MAPPER;

    @Autowired
    public DeviceRepositoryMapperHolder(DeviceDtoMapper mapper, DeviceOnboardDtoMapper onboardMapper, DeviceInfoDtoMapper infoMapper, DeviceByInstrumentDtoMapper instrumentMapper, DeviceImagesDtoMapper imagesMapper) {
        DeviceRepositoryMapperHolder.MAPPER = mapper;
        DeviceRepositoryMapperHolder.ONBOARD_MAPPER = onboardMapper;
        DeviceRepositoryMapperHolder.INFO_MAPPER = infoMapper;
        DeviceRepositoryMapperHolder.INSTRUMENT_MAPPER = instrumentMapper;
        DeviceRepositoryMapperHolder.IMAGES_MAPPER = imagesMapper;
    }
}
