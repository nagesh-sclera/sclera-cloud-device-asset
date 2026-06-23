package io.sclera.service;

import io.sclera.dto.MeasuringInstrumentDetailsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for ConditionsService alert-dispatch methods: the live measuring-instrument variant
 * (resolves details and stamps the alert message) and the per-protocol dispatch stubs, which
 * currently no-op inside a try/catch and must not throw.
 */
@ExtendWith(MockitoExtension.class)
class ConditionsServiceAlertInfoTest {

    @Mock MeasuringInstrumentService measuringInstrumentService;

    @InjectMocks ConditionsService service;

    @Test
    void sendMeasuringInstrumentAlertInfo_stampsAlertMessage() {
        MeasuringInstrumentDetailsDTO details = mock(MeasuringInstrumentDetailsDTO.class);
        when(measuringInstrumentService.getMeasuringInstrumentSensorDetailsById("mi1")).thenReturn(details);

        service.sendMeasuringInstrumentAlertInfo("mi1", "Over threshold");

        verify(details).setAlert_message("Over threshold");
    }

    @Test
    void perProtocolAlertDispatchStubs_doNotThrow() {
        assertThatCode(() -> {
            service.sendBacnetAlertInfo("d", "o", "msg");
            service.sendLorawanAlertInfo("s", "attr", "msg");
            service.sendDisruptiveAlertInfo("s", "msg");
            service.sendMyDevicesAlertInfo("s", "attr", "msg");
            service.sendMonnitAlertInfo("s", "msg");
            service.sendPelicanAlertInfo("s", "attr", "msg");
            service.sendKNXAlertInfo("dev", "grp", "msg");
        }).doesNotThrowAnyException();
    }
}
