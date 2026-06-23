package io.sclera.service;

import io.sclera.Repository.ConditionsRepository;
import io.sclera.dto.touchscreen.SensorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for ConditionsService.deleteSensorConditions / deleteAllSensorConditions: resolve a
 * sensor's conditions and delete them. Exercised with an empty condition set so the delete loop
 * (and its alert recompute) is not entered.
 */
@ExtendWith(MockitoExtension.class)
class ConditionsServiceDeleteTest {

    @Mock ConditionsRepository conditionsRepository;

    @InjectMocks ConditionsService service;

    @Test
    void deleteSensorConditions_resolvesConditionsForSensor() {
        SensorDTO sensor = mock(SensorDTO.class);
        when(sensor.getPrimary_id()).thenReturn("mi1");
        when(conditionsRepository.getConditions(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Set.of());

        service.deleteSensorConditions("u", "v", "d", "measuring_instrument", sensor,
                mock(HttpServletRequest.class));

        verify(conditionsRepository).getConditions(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deleteAllSensorConditions_iteratesEachSensor() {
        SensorDTO sensor = mock(SensorDTO.class);
        when(sensor.getPrimary_id()).thenReturn("mi1");
        when(conditionsRepository.getConditions(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Set.of());

        service.deleteAllSensorConditions("u", "v", "d", "measuring_instrument", Set.of(sensor),
                mock(HttpServletRequest.class));

        verify(conditionsRepository).getConditions(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
    }
}
