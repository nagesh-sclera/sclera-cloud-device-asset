package io.sclera.service;

import io.sclera.Repository.MeasuringInstrumentAttributesRepository;
import io.sclera.Repository.MeasuringInstrumentRepository;
import io.sclera.dto.AnalyticSensorDTO;
import io.sclera.dto.CategorySensorDTO;
import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import io.sclera.dto.MeasuringInstrumentDTO;
import io.sclera.dto.SensorAlertDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import io.sclera.models.MeasuringInstrument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean main methods of MeasuringInstrumentService: repository
 * delegates, pure parsing, alert-count aggregation, key branches, and attribute
 * enrichment. The heavy multi-collaborator methods (upsertInstrument, updateInstrumentValueById,
 * processData/getMeasuringInstrumentsAttributes, Bacnet/formula value updates) are deferred.
 */
@ExtendWith(MockitoExtension.class)
class MeasuringInstrumentServiceTest {

    @Mock MeasuringInstrumentRepository measuingInstrumentRepository;
    @Mock MeasuringInstrumentAttributesRepository measuringInstrumentAttributesRepository;
    @Mock DeviceService deviceService;

    @InjectMocks MeasuringInstrumentService service;

    // ---- pure parsing -----------------------------------------------------

    @Test
    void getNoOfParametersFromString_returnsHighestParameterNumber() {
        assertThat(service.getNoOfParametersFromString("a parameter_1 b parameter_5 c parameter_3")).isEqualTo(5);
        assertThat(service.getNoOfParametersFromString("nothing here")).isEqualTo(0);
    }

    // ---- simple repository delegates -------------------------------------

    @Test
    void getInstrumentCountByDeviceId_delegates() {
        when(measuingInstrumentRepository.getInstrumentCountByDeviceId("d1")).thenReturn(4);
        assertThat(service.getInstrumentCountByDeviceId("d1")).isEqualTo(4);
    }

    @Test
    void getUniqueSensorCategoryByFloor_delegates() {
        Set<String> set = Set.of("temp", "humidity");
        when(measuingInstrumentRepository.getUniqueSensorCategoryByFloor("f1")).thenReturn(set);
        assertThat(service.getUniqueSensorCategoryByFloor("f1")).isSameAs(set);
    }

    @Test
    void getSensorCategoryByFloorCount_delegates() {
        when(measuingInstrumentRepository.getSensorCategoryByFloorCount("f1", "temp")).thenReturn(6);
        assertThat(service.getSensorCategoryByFloorCount("f1", "temp")).isEqualTo(6);
    }

    @Test
    void getSensorCategoryByFloorPagination_delegates() {
        List<CategorySensorDTO> list = List.of(mock(CategorySensorDTO.class));
        when(measuingInstrumentRepository.getSensorCategoryByFloorPagination("f1", "temp", 10, 0)).thenReturn(list);
        assertThat(service.getSensorCategoryByFloorPagination("f1", "temp", 10, 0)).isSameAs(list);
    }

    @Test
    void updateMeasuringInstrumentSensorAlert_delegates() {
        service.updateMeasuringInstrumentSensorAlert("mi1", true);
        verify(measuingInstrumentRepository).updateMeasuringInstrumentSensorAlert("mi1", true);
    }

    @Test
    void getDeviceIdByMeasuringInstrumentSensorId_delegates() {
        when(measuingInstrumentRepository.getDeviceIdByMeasuringInstrumentSensorId("mi1")).thenReturn("d1");
        assertThat(service.getDeviceIdByMeasuringInstrumentSensorId("mi1")).isEqualTo("d1");
    }

    @Test
    void getMeasuringInstrumentSensorCurrentValue_delegates() {
        when(measuingInstrumentRepository.getMeasuringInstrumentSensorCurrentValue("mi1")).thenReturn("23.5");
        assertThat(service.getMeasuringInstrumentSensorCurrentValue("mi1")).isEqualTo("23.5");
    }

    @Test
    void getSensorByDeviceId_delegates() {
        Set<SensorDTO> set = Set.of(mock(SensorDTO.class));
        when(measuingInstrumentRepository.getSensorByDeviceId("d1")).thenReturn(set);
        assertThat(service.getSensorByDeviceId("d1")).isSameAs(set);
    }

    @Test
    void getSensorByLocationId_delegates() {
        Set<SensorDTO> set = Set.of(mock(SensorDTO.class));
        when(measuingInstrumentRepository.getSensorByLocationId("l1")).thenReturn(set);
        assertThat(service.getSensorByLocationId("l1")).isSameAs(set);
    }

    @Test
    void getMeasuringInstruments_delegatesToFindAll() {
        List<MeasuringInstrument> list = List.of(mock(MeasuringInstrument.class));
        when(measuingInstrumentRepository.findAll()).thenReturn(list);
        assertThat(service.getMeasuringInstruments()).isSameAs(list);
    }

    @Test
    void deleteMeasuringIntrumentLocationsByLocationId_delegates() {
        service.deleteMeasuringIntrumentLocationsByLocationId("l1");
        verify(measuingInstrumentRepository).deleteMeasuringIntrumentLocationsByLocationId("l1");
    }

    @Test
    void checkMeasuringInstrumentsExists_delegates() {
        when(measuingInstrumentRepository.checkMeasuringInstrumentsExists("mi1", "l1")).thenReturn(1);
        assertThat(service.checkMeasuringInstrumentsExists("mi1", "l1")).isEqualTo(1);
    }

    @Test
    void updateInstrumentValueAndAttributeById_delegates() {
        BigInteger ts = BigInteger.valueOf(99L);
        service.updateInstrumentValueAndAttributeById("mi1", "10", ts, "{}");
        verify(measuingInstrumentRepository).updateInstrumentValueAndAttributeById("mi1", "10", ts, "{}");
    }

    @Test
    void deleteDigitalTwinPositions_delegates() {
        service.deleteDigitalTwinPositions("d1");
        verify(measuingInstrumentRepository).deleteDigitalTwinPositions("d1");
    }

    @Test
    void getMeasuringInstrumentCountByType_delegates() {
        when(measuingInstrumentRepository.getMeasuringInstrumentCountByType("flow")).thenReturn(3);
        assertThat(service.getMeasuringInstrumentCountByType("flow")).isEqualTo(3);
    }

    @Test
    void getMeasuringInstrumentIdsByProtocolAndPrimaryIds_delegates() {
        Set<String> set = Set.of("mi1");
        when(measuingInstrumentRepository.getMeasuringInstrumentIdsByProtocolAndPrimaryIds("modbus", Set.of("p1")))
                .thenReturn(set);
        assertThat(service.getMeasuringInstrumentIdsByProtocolAndPrimaryIds("modbus", Set.of("p1"))).isSameAs(set);
    }

    @Test
    void getMeasuringInstrumentAlertDetails_delegates() {
        SensorAlertDTO a = mock(SensorAlertDTO.class);
        when(measuingInstrumentRepository.getMeasuringInstrumentAlertDetails("mi1")).thenReturn(a);
        assertThat(service.getMeasuringInstrumentAlertDetails("mi1")).isSameAs(a);
    }

    @Test
    void getMeasuringInstrumentAttributeById_delegates() {
        MeasuringInstrumentAttributesDTO a = mock(MeasuringInstrumentAttributesDTO.class);
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributeById("a1")).thenReturn(a);
        assertThat(service.getMeasuringInstrumentAttributeById("a1")).isSameAs(a);
    }

    @Test
    void getMeasuringInstrumentAttributesByMeasuringInstrumentId_delegates() {
        List<MeasuringInstrumentAttributesDTO> list = List.of(mock(MeasuringInstrumentAttributesDTO.class));
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId("mi1"))
                .thenReturn(list);
        assertThat(service.getMeasuringInstrumentAttributesByMeasuringInstrumentId("mi1")).isSameAs(list);
    }

    // ---- aggregation / offset / branches ---------------------------------

    @Test
    void getMeasuringInstrumentsAlertsCount_buildsMapFromBothCounts() {
        when(measuingInstrumentRepository.getMeasuringInstrumentAlertSensorCount(true)).thenReturn(5);
        when(measuingInstrumentRepository.getMeasuringInstrumentAlertSensorCount(false)).thenReturn(3);

        Map<String, Integer> counts = service.getMeasuringInstrumentsAlertsCount();

        assertThat(counts).containsEntry("measuring_instrument_with_alert_count", 5)
                .containsEntry("measuring_instrument_without_alert_count", 3);
    }

    @Test
    void getMeasuringInstrumentAlertStatusByDeviceId_trueWhenCountPositive() {
        when(measuingInstrumentRepository.getMeasuringInstrumentAlertCountDeviceId("d1", true)).thenReturn(2);
        assertThat(service.getMeasuringInstrumentAlertStatusByDeviceId("d1")).isTrue();
    }

    @Test
    void getMeasuringInstrumentAlertStatusByDeviceId_falseWhenCountZero() {
        when(measuingInstrumentRepository.getMeasuringInstrumentAlertCountDeviceId("d1", true)).thenReturn(0);
        assertThat(service.getMeasuringInstrumentAlertStatusByDeviceId("d1")).isFalse();
    }

    @Test
    void getAnalyticsMeasuringInstruments_computesOffsetAndDelegates() {
        Set<AnalyticSensorDTO> set = Set.of(mock(AnalyticSensorDTO.class));
        // pageno=3, pagesize=10 -> offset = 20
        // db-per-service: repo no longer takes report_template_id (report_attributes JOIN removed); service still accepts it for API compatibility.
        when(measuingInstrumentRepository.getAnalyticsMeasuringInstruments("cat", "key", 10, 20))
                .thenReturn(set);
        assertThat(service.getAnalyticsMeasuringInstruments("cat", "key", 3, 10, "rt1")).isSameAs(set);
    }

    @Test
    void updateDeviceMeasureCountByDeviceIds_callsDeviceServicePerDevice() {
        service.updateDeviceMeasureCountByDeviceIds(new java.util.LinkedHashSet<>(List.of("d1", "d2")));
        verify(deviceService).updateDeviceMeasureCountByDeviceId("d1");
        verify(deviceService).updateDeviceMeasureCountByDeviceId("d2");
    }

    @Test
    void updateMeasuringInstrumentDeviceId_refreshesNewDeviceOnly_whenExistingNotRetained() {
        service.updateMeasuringInstrumentDeviceId("d1", "old1", Set.of());

        verify(measuingInstrumentRepository).updateMeasuringInstrumentDeviceId("d1", "old1");
        verify(deviceService).updateDeviceMeasureCountByDeviceId("d1");
        verify(deviceService).updateDeviceMeasuringInstrumentStatusByDeviceId("d1");
        verify(deviceService, never()).updateDeviceMeasureCountByDeviceId("old1");
    }

    @Test
    void updateMeasuringInstrumentDeviceId_alsoRefreshesExisting_whenRetained() {
        service.updateMeasuringInstrumentDeviceId("d1", "old1", Set.of("old1"));

        verify(deviceService).updateDeviceMeasureCountByDeviceId("d1");
        verify(deviceService).updateDeviceMeasureCountByDeviceId("old1");
        verify(deviceService).updateDeviceMeasuringInstrumentStatusByDeviceId("old1");
    }

    // ---- attribute upsert id-generation branch ---------------------------

    @Test
    void upsertMeasuringInstrumentAttribute_generatesIdWhenNull() {
        MeasuringInstrumentAttributesDTO a = new MeasuringInstrumentAttributesDTO();
        a.setId(null);

        service.upsertMeasuringInstrumentAttribute(a);

        assertThat(a.getId()).isNotNull();
        verify(measuringInstrumentAttributesRepository).upsertMeasuringInstrumentAttribute(
                anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void upsertMeasuringInstrumentAttribute_usesProvidedId() {
        MeasuringInstrumentAttributesDTO a = new MeasuringInstrumentAttributesDTO();
        a.setId("a1");

        service.upsertMeasuringInstrumentAttribute(a);

        verify(measuringInstrumentAttributesRepository).upsertMeasuringInstrumentAttribute(
                eq("a1"), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ---- enrichment ------------------------------------------------------

    @Test
    void getMeasuringInstrumentSensorById_enrichesWithAttributes() {
        MeasuringInstrumentDTO mi = mock(MeasuringInstrumentDTO.class);
        when(mi.getId()).thenReturn("mi1");
        when(measuingInstrumentRepository.getMeasuringInstrumentSensorById("mi1")).thenReturn(mi);
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(mock(MeasuringInstrumentAttributesDTO.class));
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId("mi1"))
                .thenReturn(attrs);

        MeasuringInstrumentDTO result = service.getMeasuringInstrumentSensorById("u", "v", "mi1");

        assertThat(result).isSameAs(mi);
        verify(mi).setMeasuring_instrument_attributes(attrs);
    }

    @Test
    void getMeasuringInstrumentsByDeviceId_enrichesEachWithAttributes() {
        MeasuringInstrumentDTO mi = mock(MeasuringInstrumentDTO.class);
        when(mi.getId()).thenReturn("mi1");
        when(measuingInstrumentRepository.getMeasuringInstrumentsByDeviceId("d1")).thenReturn(List.of(mi));
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(mock(MeasuringInstrumentAttributesDTO.class));
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId("mi1"))
                .thenReturn(attrs);

        List<MeasuringInstrumentDTO> result = service.getMeasuringInstrumentsByDeviceId("d1");

        assertThat(result).containsExactly(mi);
        verify(mi).setMeasuring_instrument_attributes(attrs);
    }
}
