package io.sclera.service;

import io.sclera.Repository.ConditionsRepository;
import io.sclera.Repository.ScheduledJobRepository;
import io.sclera.client.AlertProfileClient;
import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.ConditionsAdvanceExportExcelDto;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.ScheduledJobDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean, self-contained methods of ConditionsService: sensor alert-job
 * scheduling/replacement/deletion and the advance-export mapping. The large upsert/alert
 * evaluation and per-protocol sensor-condition methods are deferred (heavy integration code).
 */
@ExtendWith(MockitoExtension.class)
class ConditionsServiceTest {

    @Mock ConditionsRepository conditionsRepository;
    @Mock JobSchedulerService jobSchedulerService;
    @Mock ScheduledJobRepository scheduledJobRepository;
    @Mock AlertProfileClient alertProfileClient;

    @InjectMocks ConditionsService service;

    private ConditionsDTO condition(String id) {
        ConditionsDTO c = mock(ConditionsDTO.class);
        lenient().when(c.getId()).thenReturn(id);
        lenient().when(c.getAlert_time()).thenReturn(30);
        return c;
    }

    // ---- delegate ---------------------------------------------------------

    @Test
    void getConditionByConditionId_delegates() {
        ConditionsDTO c = mock(ConditionsDTO.class);
        when(conditionsRepository.getConditionByConditionId("c1")).thenReturn(c);
        assertThat(service.getConditionByConditionId("c1")).isSameAs(c);
    }

    // ---- scheduleSensorAlertJob branches ---------------------------------

    @Test
    void scheduleSensorAlertJob_jobCreated_addsScheduledJob() {
        when(jobSchedulerService.createScheduledJob(any(ScheduledJobDTO.class))).thenReturn("job1");

        service.scheduleSensorAlertJob("create", condition("c1"), "sensor");

        verify(jobSchedulerService).addScheduledJob(any());
    }

    @Test
    void scheduleSensorAlertJob_noJobId_doesNotAdd() {
        when(jobSchedulerService.createScheduledJob(any(ScheduledJobDTO.class))).thenReturn(null);

        service.scheduleSensorAlertJob("create", condition("c1"), "sensor");

        verify(jobSchedulerService, never()).addScheduledJob(any());
    }

    // ---- replaceSensorAlertJob -------------------------------------------

    @Test
    void replaceSensorAlertJob_jobCreated_deletesOldAndAdds() {
        when(jobSchedulerService.createScheduledJob(any(ScheduledJobDTO.class))).thenReturn("newJob");

        service.replaceSensorAlertJob("create", condition("c1"), "sensor", "oldKey");

        verify(jobSchedulerService).deleteScheduledJob(java.util.Set.of("oldKey"));
        verify(jobSchedulerService).addScheduledJob(any());
    }

    @Test
    void replaceSensorAlertJob_noJobId_doesNotDeleteOrAdd() {
        when(jobSchedulerService.createScheduledJob(any(ScheduledJobDTO.class))).thenReturn(null);

        service.replaceSensorAlertJob("create", condition("c1"), "sensor", "oldKey");

        verify(jobSchedulerService, never()).deleteScheduledJob(any());
        verify(jobSchedulerService, never()).addScheduledJob(any());
    }

    // ---- deleteSensorAlertJob branches -----------------------------------

    @Test
    void deleteSensorAlertJob_jobPresent_deletesRecord() {
        ScheduledJobDTO existing = mock(ScheduledJobDTO.class);
        when(existing.getId()).thenReturn("sj1");
        when(jobSchedulerService.getScheduledJobByConditionId("c1")).thenReturn(existing);
        when(jobSchedulerService.createScheduledJob(any(ScheduledJobDTO.class))).thenReturn("delJob");

        service.deleteSensorAlertJob("c1");

        verify(scheduledJobRepository).deleteByConditionId("c1");
    }

    @Test
    void deleteSensorAlertJob_noScheduledJob_doesNothing() {
        when(jobSchedulerService.getScheduledJobByConditionId("c1")).thenReturn(null);

        service.deleteSensorAlertJob("c1");

        verify(scheduledJobRepository, never()).deleteByConditionId(any());
    }

    // ---- getConditionsForAdvanceExcelExport branches ---------------------

    @Test
    void getConditionsForAdvanceExcelExport_emptyRows_returnsEmpty() {
        when(conditionsRepository.getConditionsForAdvanceExcelExport("d1")).thenReturn(List.of());
        assertThat(service.getConditionsForAdvanceExcelExport("u", "v", "d1")).isEmpty();
    }

    @Test
    void getConditionsForAdvanceExcelExport_withRows_mapsToDtos() {
        Map<String, Object> row = new HashMap<>();
        row.put("condition_id", "c1");
        row.put("condition_name", "Temp High");
        when(conditionsRepository.getConditionsForAdvanceExcelExport("d1")).thenReturn(List.of(row));

        List<ConditionsAdvanceExportExcelDto> result = service.getConditionsForAdvanceExcelExport("u", "v", "d1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConditionId()).isEqualTo("c1");
        assertThat(result.get(0).getConditionName()).isEqualTo("Temp High");
    }

    @Test
    void getConditionsForAdvanceExcelExport_enrichesAlertProfileFromClient() {
        // db-per-service: alert_profile name/ioc now come from the Dapr AlertProfileClient, not a JOIN.
        Map<String, Object> row = new HashMap<>();
        row.put("condition_id", "c1");
        row.put("alert_profile_id", "ap1");
        when(conditionsRepository.getConditionsForAdvanceExcelExport("d1")).thenReturn(List.of(row));
        AlertProfileDTO ap = new AlertProfileDTO();
        ap.setName("Critical");
        ap.setIoc(2);
        when(alertProfileClient.getAlertProfileById("ap1")).thenReturn(ap);

        List<ConditionsAdvanceExportExcelDto> result = service.getConditionsForAdvanceExcelExport("u", "v", "d1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAlertProfileId()).isEqualTo("ap1");
        assertThat(result.get(0).getAlertProfileName()).isEqualTo("Critical");
        assertThat(result.get(0).getIoc()).isEqualTo(2);
    }
}
