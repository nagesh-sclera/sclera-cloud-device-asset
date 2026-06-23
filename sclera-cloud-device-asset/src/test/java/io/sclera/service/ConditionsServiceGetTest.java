package io.sclera.service;

import io.sclera.Repository.ConditionsRepository;
import io.sclera.client.AlertProfileClient;
import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for ConditionsService sensor-condition reads: getConditions / getConditionsTS delegate
 * to getSensorConditions, which maps the protocol group to the repository lookup and enriches each
 * condition that references an alert profile. Covers the unrecognised-group, plain, and
 * alert-profile-enrichment paths.
 */
@ExtendWith(MockitoExtension.class)
class ConditionsServiceGetTest {

    @Mock ConditionsRepository conditionsRepository;
    @Mock AlertProfileClient alertProfileClient;

    @InjectMocks ConditionsService service;

    @Test
    void getConditions_unrecognisedGroup_returnsNull() {
        assertThat(service.getConditions("u", "v", "d", "no_such_group", mock(SensorDTO.class))).isNull();
    }

    @Test
    void getConditionsTS_unrecognisedGroup_returnsNull() {
        assertThat(service.getConditionsTS("no_such_group", mock(SensorDTO.class))).isNull();
    }

    @Test
    void getSensorConditions_measuringInstrument_returnsConditions() {
        SensorDTO sensor = mock(SensorDTO.class);
        when(sensor.getPrimary_id()).thenReturn("mi1");
        ConditionsDTO cond = mock(ConditionsDTO.class);
        when(cond.getAlert_profile_id()).thenReturn(null); // no enrichment
        when(conditionsRepository.getConditions(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Set.of(cond));

        assertThat(service.getSensorConditions("measuring_instrument", sensor)).hasSize(1);
    }

    @Test
    void getSensorConditions_enrichesAlertProfileWhenPresent() {
        SensorDTO sensor = mock(SensorDTO.class);
        when(sensor.getPrimary_id()).thenReturn("d1");
        ConditionsDTO cond = mock(ConditionsDTO.class);
        when(cond.getAlert_profile_id()).thenReturn("ap1");
        when(conditionsRepository.getConditions(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Set.of(cond));
        AlertProfileDTO profile = mock(AlertProfileDTO.class);
        when(alertProfileClient.getAlertProfileDetailsById(any(), any(), eq("ap1"))).thenReturn(profile);

        service.getSensorConditions("disruptive", sensor);

        verify(cond).setAlert_profile(profile);
    }
}
