package io.sclera.service;

import io.sclera.Repository.MeasuringInstrumentAttributesRepository;
import io.sclera.Repository.MeasuringInstrumentRepository;
import io.sclera.dto.AnalyticSensorDTO;
import io.sclera.dto.CategorySensorDTO;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import io.sclera.dto.MeasuringInstrumentDTO;
import io.sclera.dto.MeasuringInstrumentDetailsDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import io.sclera.models.MeasuringInstrument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Additional delegate/enrichment coverage for MeasuringInstrumentService beyond the main test:
 * floor/location category lookups, sensor-detail and pagination enrichment with attributes,
 * template lookups, and the batched value save.
 */
@ExtendWith(MockitoExtension.class)
class MeasuringInstrumentServiceMoreTest {

    @Mock MeasuringInstrumentRepository measuingInstrumentRepository;
    @Mock MeasuringInstrumentAttributesRepository measuringInstrumentAttributesRepository;
    @Mock DeviceService deviceService;

    @InjectMocks MeasuringInstrumentService service;

    @Test
    void getSensorCategoryByFloor_delegates() {
        Set<CategorySensorDTO> set = Set.of(mock(CategorySensorDTO.class));
        when(measuingInstrumentRepository.getSensorCategoryByFloor("f1", "temp")).thenReturn(set);
        assertThat(service.getSensorCategoryByFloor("f1", "temp")).isSameAs(set);
    }

    @Test
    void getSensorCategoryByLocationPagination_delegates() {
        List<CategorySensorDTO> list = List.of(mock(CategorySensorDTO.class));
        when(measuingInstrumentRepository.getSensorCategoryByLocationPagination("l1", "temp", 10, 0)).thenReturn(list);
        assertThat(service.getSensorCategoryByLocationPagination("l1", "temp", 10, 0)).isSameAs(list);
    }

    @Test
    void getSensorCategoryByLocationCount_delegates() {
        when(measuingInstrumentRepository.getSensorCategoryByLocationCount("l1", "temp")).thenReturn(7);
        assertThat(service.getSensorCategoryByLocationCount("l1", "temp")).isEqualTo(7);
    }

    @Test
    void updateMeasuringinstrumentSensorUserDataValue_delegates() {
        service.updateMeasuringinstrumentSensorUserDataValue("mi1", "42");
        verify(measuingInstrumentRepository).updateMeasuringinstrumentSensorUserDataValue("mi1", "42");
    }

    @Test
    void getMeasuringInstrumentSensorsByDeviceId_delegates() {
        List<SensorDTO> list = List.of(mock(SensorDTO.class));
        when(measuingInstrumentRepository.getmeasuringInstrumentsByDeviceId("d1")).thenReturn(list);
        assertThat(service.getMeasuringInstrumentSensorsByDeviceId("d1")).isSameAs(list);
    }

    @Test
    void listMeasuringIntrumentDevicesAlertMessagesByDeviceIds_delegates() {
        List<ConditionsDTO> list = List.of(mock(ConditionsDTO.class));
        when(measuingInstrumentRepository.listMeasuringIntrumentDevicesAlertMessagesByDevice(List.of("d1")))
                .thenReturn(list);
        assertThat(service.listMeasuringIntrumentDevicesAlertMessagesByDeviceIds(List.of("d1"))).isSameAs(list);
    }

    @Test
    void getMeasuringInstrumentsByTemplateId_delegates() {
        AnalyticSensorDTO a = mock(AnalyticSensorDTO.class);
        when(measuingInstrumentRepository.getMeasuringInstrumentsByTemplateId("mi1", "key", "ra1")).thenReturn(a);
        assertThat(service.getMeasuringInstrumentsByTemplateId("mi1", "key", "ra1")).isSameAs(a);
    }

    @Test
    void getMeasuringInstrumentSensorDetailsById_enrichesWithAttributes() {
        MeasuringInstrumentDetailsDTO details = mock(MeasuringInstrumentDetailsDTO.class);
        when(details.getId()).thenReturn("mi1");
        when(measuingInstrumentRepository.getMeasuringInstrumentSensorDetailsById("mi1")).thenReturn(details);
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(mock(MeasuringInstrumentAttributesDTO.class));
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId("mi1"))
                .thenReturn(attrs);

        assertThat(service.getMeasuringInstrumentSensorDetailsById("mi1")).isSameAs(details);
        verify(details).setMeasuring_instrument_attributes(attrs);
    }

    @Test
    void getAllMeasuringInstrumentDeviceByPagination_computesOffsetAndEnriches() {
        MeasuringInstrumentDTO mi = mock(MeasuringInstrumentDTO.class);
        when(mi.getId()).thenReturn("mi1");
        // pageno=2, pagesize=10 -> offset 10
        when(measuingInstrumentRepository.getAllMeasuringInstrumentDeviceByPagination("key", 10, 10))
                .thenReturn(List.of(mi));
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(mock(MeasuringInstrumentAttributesDTO.class));
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId("mi1"))
                .thenReturn(attrs);

        List<MeasuringInstrumentDTO> result = service.getAllMeasuringInstrumentDeviceByPagination("u", "v", "key", 2, 10);

        assertThat(result).containsExactly(mi);
        verify(mi).setMeasuring_instrument_attributes(attrs);
    }

    @Test
    void updateAllInstrumentValue_savesInBatches() {
        List<MeasuringInstrument> instruments = List.of(mock(MeasuringInstrument.class), mock(MeasuringInstrument.class));
        service.updateAllInstrumentValue(instruments);
        // one batch (< 500) -> a single saveAll of the leading sublist
        verify(measuingInstrumentRepository).saveAll(instruments.subList(0, 1));
    }

    @Test
    void updateAllInstrumentValue_nullList_noSave() {
        service.updateAllInstrumentValue(null);
        verify(measuingInstrumentRepository, org.mockito.Mockito.never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }
}
