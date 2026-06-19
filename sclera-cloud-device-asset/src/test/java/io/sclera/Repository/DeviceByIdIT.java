package io.sclera.Repository;

import io.sclera.dto.DeviceDTO;
import io.sclera.it.PostgresJpaIT;
import io.sclera.mapper.DeviceDtoMapper;
import io.sclera.mapper.DeviceDtoMapperImpl;
import io.sclera.mapper.DeviceOnboardDtoMapper;
import io.sclera.mapper.DeviceOnboardDtoMapperImpl;
import io.sclera.mapper.DeviceInfoDtoMapper;
import io.sclera.mapper.DeviceInfoDtoMapperImpl;
import io.sclera.mapper.DeviceByInstrumentDtoMapper;
import io.sclera.mapper.DeviceByInstrumentDtoMapperImpl;
import io.sclera.mapper.DeviceImagesDtoMapper;
import io.sclera.mapper.DeviceImagesDtoMapperImpl;
import io.sclera.mapper.DeviceRepositoryMapperHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runtime gate for the refactored {@code DeviceRepository.getDeviceByDeviceId(String)} — executes the
 * new {@code findDeviceDetailRows} JPQL projection against a real PostgreSQL (via {@link PostgresJpaIT})
 * and verifies the row is assembled into a {@link DeviceDTO} by the MapStruct mapper.
 *
 * Mirrors the {@code DeviceRepositoryIT} harness: extends {@link PostgresJpaIT}, schema applied once
 * via {@code @Sql} BEFORE_TEST_CLASS, per-method seed/cleanup, and an injected {@link DeviceRepository}.
 *
 * The default method {@code getDeviceByDeviceId} resolves the mapper through the static
 * {@link DeviceRepositoryMapperHolder#MAPPER}, which is populated by Spring on bean construction.
 * {@code JpaTestConfig} does not component-scan {@code io.sclera.mapper}, so this test registers the
 * mapper and holder explicitly via {@link MapperBeans}.
 */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-by-id-it.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-by-id-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Import(DeviceByIdIT.MapperBeans.class)
@Transactional
class DeviceByIdIT extends PostgresJpaIT {

    @Configuration
    static class MapperBeans {
        @Bean
        DeviceDtoMapper deviceDtoMapper() {
            return new DeviceDtoMapperImpl();
        }

        @Bean
        DeviceOnboardDtoMapper deviceOnboardDtoMapper() {
            return new DeviceOnboardDtoMapperImpl();
        }

        @Bean
        DeviceInfoDtoMapper deviceInfoDtoMapper() {
            return new DeviceInfoDtoMapperImpl();
        }

        @Bean
        DeviceByInstrumentDtoMapper deviceByInstrumentDtoMapper() {
            return new DeviceByInstrumentDtoMapperImpl();
        }

        @Bean
        DeviceImagesDtoMapper deviceImagesDtoMapper() {
            return new DeviceImagesDtoMapperImpl();
        }

        @Bean
        DeviceRepositoryMapperHolder deviceRepositoryMapperHolder(DeviceDtoMapper mapper,
                                                                  DeviceOnboardDtoMapper onboardMapper,
                                                                  DeviceInfoDtoMapper infoMapper,
                                                                  DeviceByInstrumentDtoMapper instrumentMapper,
                                                                  DeviceImagesDtoMapper imagesMapper) {
            return new DeviceRepositoryMapperHolder(mapper, onboardMapper, infoMapper, instrumentMapper, imagesMapper);
        }
    }

    @Autowired
    DeviceRepository deviceRepository;

    @Test
    void getDeviceByDeviceId_withRelations_mapsJoinedScalars() {
        DeviceDTO dto = deviceRepository.getDeviceByDeviceId("dev-1");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("dev-1");
        assertThat(dto.getLocation()).isEqualTo("Room 101");
        assertThat(dto.getFloor()).isEqualTo("Floor 1");
        assertThat(dto.getBuilding()).isEqualTo("Tower A");
        assertThat(dto.getLocation_id()).isEqualTo("loc-1");
        assertThat(dto.getSnmp_count()).isEqualTo(3);
        assertThat(dto.getSystem_type()).isEqualTo("hvac");
        assertThat(dto.getInventory_tracking_id()).isEqualTo("trk-7");
    }

    @Test
    void getDeviceByDeviceId_bareDevice_joinedScalarsNull() {
        DeviceDTO dto = deviceRepository.getDeviceByDeviceId("dev-2");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("dev-2");
        assertThat(dto.getLocation()).isNull();
        assertThat(dto.getBuilding()).isNull();
        assertThat(dto.getLocal_vendor_id()).isNull();
        assertThat(dto.getInventory_tracking_id()).isNull();
    }

    @Test
    void getDeviceByDeviceId_unknownId_returnsNull() {
        assertThat(deviceRepository.getDeviceByDeviceId("nope")).isNull();
    }
}
