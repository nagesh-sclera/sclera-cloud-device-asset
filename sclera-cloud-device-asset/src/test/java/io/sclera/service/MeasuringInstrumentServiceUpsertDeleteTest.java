package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.Repository.MeasuringInstrumentAttributesRepository;
import io.sclera.Repository.MeasuringInstrumentRepository;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import io.sclera.dto.MeasuringInstrumentDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for MeasuringInstrumentService delete/untag/tag and attribute-processing paths: per-device
 * delete (success + failure audit), instrument enrichment with attributes/locations, the Siemens
 * fetch, location tag/untag loops, and the processData / getMeasuringInstrumentsAttributes builders.
 *
 * DEFERRED: the big upsertInstrument orchestration and the JdbcTemplate/DataSource-backed value-update
 * and multi-digital-twin paths.
 */
@ExtendWith(MockitoExtension.class)
class MeasuringInstrumentServiceUpsertDeleteTest {

    @Mock MeasuringInstrumentRepository measuingInstrumentRepository;
    @Mock MeasuringInstrumentAttributesRepository measuringInstrumentAttributesRepository;
    @Mock UserActionLogService userActionLogService;
    @Mock DeviceService deviceService;
    @Mock LocationService locationService;

    @InjectMocks MeasuringInstrumentService service;

    private HttpServletRequest req() {
        HttpServletRequest r = mock(HttpServletRequest.class);
        when(r.getRequestURI()).thenReturn("/api/sensors");
        return r;
    }

    // ---- simple delegate ----

    @Test
    void getIntegrationSensorByLocationId_delegates() {
        Set<SensorDTO> sensors = Set.of(mock(SensorDTO.class));
        when(measuingInstrumentRepository.getIntegrationSensorByLocationId("l1")).thenReturn(sensors);
        assertThat(service.getIntegrationSensorByLocationId("l1")).isSameAs(sensors);
    }

    // ---- delete ----

    @Test
    void deleteInstrumentsByDeviceId_deletesEachAndRefreshesDevice() {
        MeasuringInstrumentDTO inst = mock(MeasuringInstrumentDTO.class);
        when(inst.getId()).thenReturn("i1");
        when(inst.getDevice_id()).thenReturn("d1");
        when(measuingInstrumentRepository.getInstrumentByDeviceId("d1")).thenReturn(Set.of(inst));

        service.deleteInstrumentsByDeviceId("u", "v", "d1", req());

        verify(measuingInstrumentRepository).deleteById("i1");
        verify(deviceService).updateDeviceMeasureCountByDeviceId("d1");
        verify(deviceService).updateDeviceMeasuringInstrumentStatusByDeviceId("d1");
        verify(userActionLogService).addUserAction(eq("u"), eq("sensors"), eq("DELETE"),
                any(), eq("success"), eq("sensors_configuration"), eq("i1"));
    }

    @Test
    void deleteInstrumentById_deleteFails_logsFailed() {
        MeasuringInstrumentDTO inst = mock(MeasuringInstrumentDTO.class);
        when(inst.getId()).thenReturn("i1");
        when(inst.getDevice_id()).thenReturn("d1");
        doThrow(new RuntimeException("boom")).when(measuingInstrumentRepository).deleteById("i1");

        service.deleteInstrumentById("u", "v", Set.of(inst), req());

        verify(userActionLogService).addUserAction(eq("u"), eq("sensors"), eq("DELETE"),
                any(), eq("failed"), eq("sensors_configuration"), eq("i1"));
        verify(deviceService, never()).updateDeviceMeasureCountByDeviceId(any());
    }

    // ---- enrichment ----

    @Test
    void getInstrumentsByDeviceId_enrichesAttributesAndLocations() {
        MeasuringInstrumentDTO inst = mock(MeasuringInstrumentDTO.class);
        when(inst.getId()).thenReturn("i1");
        when(measuingInstrumentRepository.getInstrumentByDeviceId("d1")).thenReturn(Set.of(inst));
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(mock(MeasuringInstrumentAttributesDTO.class));
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId("i1"))
                .thenReturn(attrs);
        Set<LocationDTO> locs = Set.of(mock(LocationDTO.class));
        when(locationService.getTaggedMeasuringInstrumentLocations("", "", "i1")).thenReturn(locs);

        Set<MeasuringInstrumentDTO> result = service.getInstrumentsByDeviceId("u", "v", "d1");

        assertThat(result).hasSize(1);
        verify(inst).setMeasuring_instrument_attributes(attrs);
        verify(inst).setLocations(locs);
    }

    @Test
    void getSiemensMeasuringInstruments_enrichesAttributes() {
        MeasuringInstrumentDTO inst = mock(MeasuringInstrumentDTO.class);
        when(inst.getId()).thenReturn("i1");
        when(measuingInstrumentRepository.getSiemensMeasuringInstruments()).thenReturn(List.of(inst));
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(mock(MeasuringInstrumentAttributesDTO.class));
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId("i1"))
                .thenReturn(attrs);

        assertThat(service.getSiemensMeasuringInstruments()).hasSize(1);
        verify(inst).setMeasuring_instrument_attributes(attrs);
    }

    // ---- location tag / untag ----

    @Test
    void untagLocationsFromMeasuringInstruments_success() {
        MeasuringInstrumentDTO inst = mock(MeasuringInstrumentDTO.class);
        when(inst.getId()).thenReturn("i1");
        when(inst.getLocation_id()).thenReturn("l1");

        service.untagLocationsFromMeasuringInstruments("u", "v", List.of(inst), req());

        verify(measuingInstrumentRepository).untagLocationsFromMeasuringInstruments("i1", "l1");
        verify(userActionLogService).addUserAction(eq("u"), eq("sensors"), eq("UPDATE"),
                any(), eq("success"), eq("sensors_info"), eq("i1"));
    }

    @Test
    void untagLocationsFromMeasuringInstruments_failure_logsFailed() {
        MeasuringInstrumentDTO inst = mock(MeasuringInstrumentDTO.class);
        when(inst.getId()).thenReturn("i1");
        when(inst.getLocation_id()).thenReturn("l1");
        doThrow(new RuntimeException("x"))
                .when(measuingInstrumentRepository).untagLocationsFromMeasuringInstruments("i1", "l1");

        service.untagLocationsFromMeasuringInstruments("u", "v", List.of(inst), req());

        verify(userActionLogService).addUserAction(eq("u"), eq("sensors"), eq("UPDATE"),
                any(), eq("failed"), eq("sensors_info"), eq("i1"));
    }

    @Test
    void upsertMeasuringInstrumentLocations_notExisting_tagsAndLogs() {
        MeasuringInstrumentDTO inst = mock(MeasuringInstrumentDTO.class);
        when(inst.getId()).thenReturn("i1");
        when(inst.getLocation_id()).thenReturn("l1");
        when(measuingInstrumentRepository.checkMeasuringInstrumentsExists("i1", "l1")).thenReturn(0);

        service.upsertMeasuringInstrumentLocations("u", "v", Set.of(inst), req());

        verify(measuingInstrumentRepository).upsertMeasuringInstrumentLocations("i1", "l1");
        verify(userActionLogService).addUserAction(eq("u"), eq("sensors"), eq("ADD"),
                any(), eq("success"), eq("sensors_info"), eq("i1"));
    }

    @Test
    void upsertMeasuringInstrumentLocations_alreadyExists_skips() {
        MeasuringInstrumentDTO inst = mock(MeasuringInstrumentDTO.class);
        when(inst.getId()).thenReturn("i1");
        when(inst.getLocation_id()).thenReturn("l1");
        when(measuingInstrumentRepository.checkMeasuringInstrumentsExists("i1", "l1")).thenReturn(1);

        service.upsertMeasuringInstrumentLocations("u", "v", Set.of(inst), req());

        verify(measuingInstrumentRepository, never()).upsertMeasuringInstrumentLocations(any(), any());
    }

    // ---- processData / getMeasuringInstrumentsAttributes ----

    @Test
    void processData_sensorType_emitsEntry() {
        MeasuringInstrumentAttributesDTO a = mock(MeasuringInstrumentAttributesDTO.class);
        when(a.getType()).thenReturn("sensor");
        when(a.getSecondary_id()).thenReturn("");
        when(a.getTertiary_id()).thenReturn("");

        JSONArray result = service.processData(List.of(a), BigInteger.valueOf(123L));
        assertThat(result).hasSize(1);
    }

    @Test
    void processData_nonSensorType_skips() {
        MeasuringInstrumentAttributesDTO a = mock(MeasuringInstrumentAttributesDTO.class);
        when(a.getType()).thenReturn("attribute");

        assertThat(service.processData(List.of(a), BigInteger.ONE)).isEmpty();
    }

    @Test
    void getMeasuringInstrumentsAttributes_noAttributes_addsFallbackEntry() {
        MeasuringInstrumentDTO dto = mock(MeasuringInstrumentDTO.class);
        when(dto.getId()).thenReturn("i1");
        when(measuingInstrumentRepository.getMeasuringInstrumentSensorById("i1")).thenReturn(dto);
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId("i1"))
                .thenReturn(List.of());

        JSONArray result = service.getMeasuringInstrumentsAttributes("i1");

        assertThat(result).hasSize(1);
        assertThat(result.getJSONObject(0).getString("protocol")).isEqualTo("measuring_instrument");
    }
}
