package io.sclera.service;

import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Coverage for ConditionsService.updateShareConditionSensorIds - the pure rebind that copies a
 * condition onto a target sensor's protocol identifiers (and, in "replace" mode, resets the
 * non-target identifiers to "null").
 */
class ConditionsServiceShareTest {

    private final ConditionsService service = new ConditionsService();

    @Test
    void measuringInstrument_bindsPrimaryIdToInstrument() {
        SensorDTO sensor = mock(SensorDTO.class);
        when(sensor.getPrimary_id()).thenReturn("mi1");

        ConditionsDTO result = service.updateShareConditionSensorIds(
                "add", "measuring_instrument", mock(ConditionsDTO.class), sensor);

        assertThat(result.getMeasuring_instrument_id()).isEqualTo("mi1");
    }

    @Test
    void bacnet_bindsObjectAndDeviceIds() {
        SensorDTO sensor = mock(SensorDTO.class);
        when(sensor.getPrimary_id()).thenReturn("obj1");
        when(sensor.getSecondary_id()).thenReturn("dev1");

        ConditionsDTO result = service.updateShareConditionSensorIds(
                "add", "bacnet", mock(ConditionsDTO.class), sensor);

        assertThat(result.getBacnet_object_id()).isEqualTo("obj1");
        assertThat(result.getBacnet_device_id()).isEqualTo("dev1");
    }

    @Test
    void replaceMode_resetsNonTargetIdentifiersToNullString() {
        SensorDTO sensor = mock(SensorDTO.class);
        when(sensor.getPrimary_id()).thenReturn("obj1");
        when(sensor.getSecondary_id()).thenReturn("dev1");

        ConditionsDTO result = service.updateShareConditionSensorIds(
                "replace", "bacnet", mock(ConditionsDTO.class), sensor);

        // bacnet ids come from the sensor; the other protocols are blanked to the "null" sentinel
        assertThat(result.getBacnet_object_id()).isEqualTo("obj1");
        assertThat(result.getLorawan_sensor_id()).isEqualTo("null");
    }
}
