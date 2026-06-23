package io.sclera.service;

import io.sclera.Repository.MeasuringInstrumentAttributesRepository;
import io.sclera.Repository.MeasuringInstrumentRepository;
import io.sclera.client.IOCClient;
import io.sclera.client.RabbitmqClient;
import io.sclera.client.SocketClient;
import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import io.sclera.dto.MeasuringInstrumentDTO;
import io.sclera.utils.InstrumentFormula;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for MeasuringInstrumentService value-update paths: getDaintreeMeasuringIntruments
 * enrichment, updateInstrumentValueById (persist + downstream notifications), and the
 * formula-driven recompute that skips unchanged values.
 */
@ExtendWith(MockitoExtension.class)
class MeasuringInstrumentServiceValueTest {

    @Mock MeasuringInstrumentRepository measuingInstrumentRepository;
    @Mock MeasuringInstrumentAttributesRepository measuringInstrumentAttributesRepository;
    @Mock InstrumentFormula instrumentFormula;
    @Mock ConditionsService conditionsService;
    @Mock IOCClient iocService;
    @Mock SocketClient socketService;
    @Mock RabbitmqClient rabbitmqService;

    @InjectMocks MeasuringInstrumentService service;

    @Test
    void getDaintreeMeasuringIntruments_enrichesAttributes() {
        MeasuringInstrumentDTO mi = mock(MeasuringInstrumentDTO.class);
        when(mi.getId()).thenReturn("mi1");
        when(measuingInstrumentRepository.getDaintreeMeasuringInstruments()).thenReturn(List.of(mi));
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(mock(MeasuringInstrumentAttributesDTO.class));
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId("mi1"))
                .thenReturn(attrs);

        assertThat(service.getDaintreeMeasuringIntruments()).containsExactly(mi);
        verify(mi).setMeasuring_instrument_attributes(attrs);
    }

    @Test
    void updateInstrumentValueById_persistsAndPublishes() {
        BigInteger ts = BigInteger.valueOf(123L);

        service.updateInstrumentValueById("mi1", "42", ts, "temperature");

        verify(measuingInstrumentRepository).updateInstrumentValueById("mi1", "42", ts);
        verify(socketService).socketMeasuringInstrumentSensorValueUpdate("mi1");
        verify(rabbitmqService).rabbitmqMeasuringInstrumentData(eq("mi1"), eq("42"), any(), eq("temperature"));
    }

    @Test
    void updateMultipleInstrumentValueByFormula_unchangedValue_skipsUpdate() {
        MeasuringInstrumentDTO mi = mock(MeasuringInstrumentDTO.class);
        when(mi.getId()).thenReturn("mi1");
        when(mi.getValue()).thenReturn("10");
        when(measuingInstrumentRepository.getInstrumentByDeviceId("d1")).thenReturn(Set.of(mi));
        when(measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId("mi1"))
                .thenReturn(List.of());
        // formula yields the same value -> no write
        when(instrumentFormula.getValuebyMeasuringParameter(any(), any(), any(), any(), any())).thenReturn("10");

        service.updateMultipleInstrumentValueByFormula("d1");

        verify(measuingInstrumentRepository, never()).updateInstrumentValueById(any(), any(), any());
    }
}
