package io.sclera.service;

import io.sclera.Repository.DeviceTypesRepository;
import io.sclera.dto.DeviceTypesDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean methods of DeviceTypeService: upsertDeviceType (null/empty,
 * up-to-date skip, and actual upsert branches) and getAllDeviceTypes (success + error fallback).
 * The DataSource/PreparedStatement batch method and the executor-backed sync methods are deferred.
 */
@ExtendWith(MockitoExtension.class)
class DeviceTypeServiceTest {

    @Mock DeviceTypesRepository deviceTypesRepository;

    @InjectMocks DeviceTypeService service;

    private DeviceTypesDTO dt(String id, long ts) {
        DeviceTypesDTO d = new DeviceTypesDTO();
        d.setId(id);
        d.setName("name-" + id);
        d.setUpdatedTimestamp(BigInteger.valueOf(ts));
        return d;
    }

    @Test
    void upsertDeviceType_null_doesNothing() {
        service.upsertDeviceType(null);
        verify(deviceTypesRepository, never()).upsert(any(), any(), any());
    }

    @Test
    void upsertDeviceType_empty_doesNothing() {
        service.upsertDeviceType(List.of());
        verify(deviceTypesRepository, never()).upsert(any(), any(), any());
    }

    @Test
    void upsertDeviceType_newType_upserts() {
        when(deviceTypesRepository.getAllDeviceTypes()).thenReturn(List.of());

        service.upsertDeviceType(List.of(dt("t1", 100L)));

        verify(deviceTypesRepository).upsert("t1", "name-t1", BigInteger.valueOf(100L));
    }

    @Test
    void upsertDeviceType_upToDate_skipsUpsert() {
        when(deviceTypesRepository.getAllDeviceTypes()).thenReturn(List.of(dt("t1", 200L)));

        // incoming timestamp older than existing -> skip
        service.upsertDeviceType(List.of(dt("t1", 100L)));

        verify(deviceTypesRepository, never()).upsert(any(), any(), any());
    }

    @Test
    void getAllDeviceTypes_delegates() {
        List<DeviceTypesDTO> all = List.of(dt("t1", 1L));
        when(deviceTypesRepository.getAllDeviceTypes()).thenReturn(all);
        assertThat(service.getAllDeviceTypes()).isSameAs(all);
    }

    @Test
    void getAllDeviceTypes_onError_returnsEmpty() {
        when(deviceTypesRepository.getAllDeviceTypes()).thenThrow(new RuntimeException("db down"));
        assertThat(service.getAllDeviceTypes()).isEmpty();
    }
}
