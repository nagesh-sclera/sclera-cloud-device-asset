package io.sclera.service;

import io.sclera.Repository.DeviceConditionsRepository;
import io.sclera.client.AlertProfileClient;
import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.ScheduledJobDTO;
import io.sclera.dto.ShareConditionsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceConditionsServiceTest {

    @Mock DeviceConditionsRepository deviceConditionsRepository;
    @Mock AlertProfileClient alertProfileClient;
    @Mock DeviceService deviceService;
    @Mock JobSchedulerService jobSchedulerService;

    @InjectMocks DeviceConditionsService service;

    // ---- helpers ----------------------------------------------------------

    private DeviceConditionsDTO cond(String id, String deviceId) {
        DeviceConditionsDTO c = new DeviceConditionsDTO();
        c.setId(id);
        c.setDevice_id(deviceId);
        return c;
    }

    /** A condition with the fields the upsert change-detection block dereferences set,
     *  so the .equals() comparisons don't NPE. */
    private DeviceConditionsDTO fullCond(String id, String deviceId, String alertCondition) {
        DeviceConditionsDTO c = cond(id, deviceId);
        c.setAlert_condition(alertCondition);
        c.setStart_time("08:00");
        c.setEnd_time("18:00");
        c.setSchedule(1);
        c.setMax_alert_count(5);
        c.setAlert_count_enabled(1);
        return c;
    }

    private DeviceDTO mockDevice(String id) {
        DeviceDTO d = mock(DeviceDTO.class);
        when(d.getId()).thenReturn(id);
        return d;
    }

    // ---- pass-through methods --------------------------------------------

    @Test
    void getAlertCount_delegatesToRepository() {
        when(deviceConditionsRepository.getAlertCount("dev1")).thenReturn(5);
        assertThat(service.getAlertCount("dev1")).isEqualTo(5);
    }

    @Test
    void updateAlertProfileId_delegatesToRepository() {
        service.updateAlertProfileId("ap1");
        verify(deviceConditionsRepository).updateAlertProfileId("ap1");
    }

    @Test
    void updateLastAlertedDetails_delegatesWithArgs() {
        BigInteger ts = BigInteger.valueOf(123L);
        service.updateLastAlertedDetails("c1", ts, true, 2);
        verify(deviceConditionsRepository).updateLastAlertedDetails("c1", ts, true, 2);
    }

    @Test
    void getDeviceConditionsForAiCall_returnsRepoResult() {
        Set<DeviceConditionsDTO> set = new HashSet<>(Set.of(cond("c1", "d1")));
        when(deviceConditionsRepository.getDeviceConditionsForAiCall("d1")).thenReturn(set);
        assertThat(service.getDeviceConditionsForAiCall("u", "v", "dock", "d1")).isSameAs(set);
    }

    @Test
    void getDeviceConditionsByIdForAiCall_returnsRepoResult() {
        DeviceConditionsDTO c = cond("c1", "d1");
        when(deviceConditionsRepository.getDeviceConditionsByIdForAiCall("c1")).thenReturn(c);
        assertThat(service.getDeviceConditionsByIdForAiCall("u", "v", "c1")).isSameAs(c);
    }

    // ---- guard branch -----------------------------------------------------

    @Test
    void updateAlertCountByConditionId_valid_updates() {
        service.updateAlertCountByConditionId("c1", 3);
        verify(deviceConditionsRepository).updateAlertCountByConditionId("c1", 3);
    }

    @Test
    void updateAlertCountByConditionId_invalid_doesNotUpdate() {
        service.updateAlertCountByConditionId(null, 3);
        service.updateAlertCountByConditionId("c1", 0);
        verify(deviceConditionsRepository, never()).updateAlertCountByConditionId(anyString(), anyInt());
    }

    // ---- enrichment branch ------------------------------------------------

    @Test
    void getDeviceConditions_enrichesWithAlertProfileWhenProfileIdSet() {
        DeviceConditionsDTO c = cond("c1", "d1");
        c.setAlert_profile_id("ap1");
        when(deviceConditionsRepository.getDeviceConditions("d1")).thenReturn(new HashSet<>(Set.of(c)));
        AlertProfileDTO profile = mock(AlertProfileDTO.class);
        when(alertProfileClient.getAlertProfileDetailsById(null, null, "ap1")).thenReturn(profile);

        service.getDeviceConditions("u", "v", "dock", "d1");

        verify(alertProfileClient).getAlertProfileDetailsById(null, null, "ap1");
        assertThat(c.getAlert_profile()).isSameAs(profile);
    }

    @Test
    void getDeviceConditions_skipsEnrichmentWhenProfileIdNull() {
        DeviceConditionsDTO c = cond("c1", "d1"); // alert_profile_id is null
        when(deviceConditionsRepository.getDeviceConditions("d1")).thenReturn(new HashSet<>(Set.of(c)));

        service.getDeviceConditions("u", "v", "dock", "d1");

        verify(alertProfileClient, never()).getAlertProfileDetailsById(any(), any(), any());
    }

    @Test
    void getDeviceConditionsById_enrichesWhenProfileIdSet() {
        DeviceConditionsDTO c = cond("c1", "d1");
        c.setAlert_profile_id("ap1");
        when(deviceConditionsRepository.getDeviceConditionsById("c1")).thenReturn(c);
        when(alertProfileClient.getAlertProfileDetailsById(null, null, "ap1")).thenReturn(mock(AlertProfileDTO.class));

        service.getDeviceConditionsById("u", "v", "c1");

        verify(alertProfileClient).getAlertProfileDetailsById(null, null, "ap1");
    }

    // ---- delete methods ---------------------------------------------------

    @Test
    void deleteAllDeviceConditions_deletesEachByid() {
        Set<DeviceConditionsDTO> set = new LinkedHashSet<>(List.of(cond("c1", "d1"), cond("c2", "d1")));
        when(deviceConditionsRepository.getDeviceConditions("d1")).thenReturn(set);

        service.deleteAllDeviceConditions("u", "v", "d1");

        verify(deviceConditionsRepository).deleteById("c1");
        verify(deviceConditionsRepository).deleteById("c2");
    }

    @Test
    void deleteDeviceConditions_deletesEachByid() {
        Set<DeviceConditionsDTO> set = new LinkedHashSet<>(List.of(cond("c1", "d1"), cond("c2", "d1")));

        service.deleteDeviceConditions("u", "v", set);

        verify(deviceConditionsRepository).deleteById("c1");
        verify(deviceConditionsRepository).deleteById("c2");
    }

    // ---- resetDeviceConditions -------------------------------------------

    @Test
    void resetDeviceConditions_resetsAndUpdatesStatusWhenDevicePresent() {
        DeviceConditionsDTO c = cond("c1", "d1");
        DeviceDTO dev = mockDevice("d1");
        when(dev.getStatus()).thenReturn(2);
        when(deviceService.getDeviceDetails("d1")).thenReturn(dev);

        service.resetDeviceConditions("u", "v", "dock", new HashSet<>(Set.of(c)));

        verify(deviceConditionsRepository).resetDeviceConditions("c1", 0, false);
        verify(deviceService).getDeviceConditionStatus("d1", 2);
    }

    @Test
    void resetDeviceConditions_skipsStatusWhenDeviceNull() {
        DeviceConditionsDTO c = cond("c1", "d1");
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);

        service.resetDeviceConditions("u", "v", "dock", new HashSet<>(Set.of(c)));

        verify(deviceConditionsRepository).resetDeviceConditions("c1", 0, false);
        verify(deviceService, never()).getDeviceConditionStatus(anyString(), any());
    }

    // ---- upsertDeviceConditions (branchy) --------------------------------

    @Test
    void upsertDeviceConditions_newConditionInserts() {
        DeviceConditionsDTO incoming = fullCond(null, "d1", "temp>5"); // id null -> insert
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);

        service.upsertDeviceConditions("u", "v", "dock", new HashSet<>(Set.of(incoming)));

        // new id is generated; device_id and last_alerted=false are fixed
        verify(deviceConditionsRepository).addDeviceConditions(
                anyString(), eq("temp>5"), eq("d1"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(false), any());
    }

    @Test
    void upsertDeviceConditions_existingChange_resetsCountAndUpdates() {
        DeviceConditionsDTO incoming = fullCond("c1", "d1", "temp>10"); // differs from existing
        DeviceConditionsDTO existing = fullCond("c1", "d1", "temp>5");
        existing.setAlert_count(7);
        existing.setLast_alerted(true);
        when(deviceConditionsRepository.existsById("c1")).thenReturn(true);
        when(deviceConditionsRepository.getDeviceConditionsById("c1")).thenReturn(existing);
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);

        service.upsertDeviceConditions("u", "v", "dock", new HashSet<>(Set.of(incoming)));

        // alert_condition changed -> alert_count reset to 0, last_alerted false
        verify(deviceConditionsRepository).updateDeviceConditions(
                eq("c1"), eq("temp>10"), eq("d1"), any(), any(), any(), eq("08:00"), eq("18:00"),
                eq(0), eq(5), eq(1), eq(1), any(), any(), eq(false), any());
    }

    @Test
    void upsertDeviceConditions_triggerTimeChanged_deletesAlertJob() {
        DeviceConditionsDTO incoming = fullCond("c1", "d1", "temp>5");
        incoming.setTrigger_time(20);
        DeviceConditionsDTO existing = fullCond("c1", "d1", "temp>5");
        existing.setTrigger_time(10); // differs -> triggers job deletion path
        when(deviceConditionsRepository.existsById("c1")).thenReturn(true);
        when(deviceConditionsRepository.getDeviceConditionsById("c1")).thenReturn(existing);
        when(jobSchedulerService.getScheduledJobByConditionId("c1")).thenReturn(mock(ScheduledJobDTO.class));
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);

        service.upsertDeviceConditions("u", "v", "dock", new HashSet<>(Set.of(incoming)));

        verify(deviceService).deleteDeviceAlertJob("c1");
    }

    // ---- upsertDeviceConditionsForAiCall ---------------------------------

    @Test
    void upsertDeviceConditionsForAiCall_newCondition_inserts() {
        DeviceConditionsDTO incoming = fullCond(null, "d1", "temp>5");
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);
        when(deviceConditionsRepository.getDeviceConditionIdByDeviceId("d1")).thenReturn(null);

        service.upsertDeviceConditionsForAiCall("u", "v", "dock", new HashSet<>(Set.of(incoming)));

        verify(deviceConditionsRepository).addDeviceConditions(
                anyString(), eq("temp>5"), eq("d1"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(false), any());
    }

    @Test
    void upsertDeviceConditionsForAiCall_alertCountAtThreshold_deletesThenReAdds() {
        DeviceConditionsDTO incoming = fullCond("c1", "d1", "temp>5");
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);
        when(deviceConditionsRepository.getDeviceConditionIdByDeviceId("d1")).thenReturn("existingId");
        when(deviceConditionsRepository.getAlertCount("d1")).thenReturn(3);
        when(deviceConditionsRepository.getDeviceConditionsForAiCall("d1"))
                .thenReturn(new HashSet<>(Set.of(cond("old1", "d1"))));

        service.upsertDeviceConditionsForAiCall("u", "v", "dock", new HashSet<>(Set.of(incoming)));

        verify(deviceConditionsRepository).deleteById("old1");
        verify(deviceConditionsRepository).addDeviceConditions(
                anyString(), eq("temp>5"), eq("d1"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(false), any());
    }

    // ---- shareDeviceConditions -------------------------------------------

    @Test
    void shareDeviceConditions_add_upsertsMappedConditionOntoTargetDevice() {
        DeviceConditionsDTO template = fullCond(null, "ignored", "temp>5");
        ShareConditionsDTO share = new ShareConditionsDTO();
        share.setDevices(List.of(mockDevice("dev1")));
        share.setCondition_method("add");
        share.setDeviceConditions(List.of(template));
        when(deviceService.getDeviceDetails("dev1")).thenReturn(null);

        service.shareDeviceConditions("u", "v", "dock", share);

        // mapped condition has device_id=dev1, id null -> insert via upsert
        verify(deviceConditionsRepository).addDeviceConditions(
                anyString(), eq("temp>5"), eq("dev1"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(false), any());
    }

    @Test
    void shareDeviceConditions_replace_deletesExistingThenUpserts() {
        DeviceConditionsDTO existingOnDevice = cond("oldC", "dev1");
        when(deviceConditionsRepository.getDeviceConditions("dev1"))
                .thenReturn(new HashSet<>(Set.of(existingOnDevice)));
        DeviceConditionsDTO template = fullCond(null, "ignored", "x>1");
        ShareConditionsDTO share = new ShareConditionsDTO();
        share.setDevices(List.of(mockDevice("dev1")));
        share.setCondition_method("replace");
        share.setDeviceConditions(List.of(template));
        when(deviceService.getDeviceDetails("dev1")).thenReturn(null);

        service.shareDeviceConditions("u", "v", "dock", share);

        verify(deviceConditionsRepository).deleteById("oldC");
        verify(deviceConditionsRepository).addDeviceConditions(
                anyString(), eq("x>1"), eq("dev1"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(false), any());
    }

    @Test
    void shareDeviceConditions_swallowsDownstreamException() {
        ShareConditionsDTO share = new ShareConditionsDTO();
        share.setDevices(List.of(mockDevice("dev1")));
        share.setCondition_method("replace");
        share.setDeviceConditions(List.of());
        when(deviceConditionsRepository.getDeviceConditions("dev1")).thenThrow(new RuntimeException("boom"));

        assertThatCode(() -> service.shareDeviceConditions("u", "v", "dock", share))
                .doesNotThrowAnyException();
    }
}
