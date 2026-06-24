package io.sclera.service;

import io.sclera.Repository.ApplicationUserRepository;
import io.sclera.Repository.DeviceInstalledAppsRepository;
import io.sclera.Repository.DeviceSpecificationRepository;
import io.sclera.Repository.ManagedSoftwareRepository;
import io.sclera.client.APICallClient;
import io.sclera.dto.InventoryApplicationDTO;
import io.sclera.models.ManagedSoftware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for ManagedSoftwareService inventory-integration paths: the field-list resource read,
 * getInventoryApplications guard, deleteManagedSoftware (skip + error branches), and the
 * update/clear sync loops plus their per-application single-transaction helpers (match + no-match).
 *
 * DEFERRED: tagInventoryDetails / unTagInventoryDetails (heavier multi-collaborator JSON ingestion)
 * and the getInventoryApplications happy-path JSON.parseArray filtering.
 */
@ExtendWith(MockitoExtension.class)
class ManagedSoftwareServiceSyncTest {

    @Mock ManagedSoftwareRepository managedSoftwareRepository;
    @Mock DeviceInstalledAppsService deviceInstalledAppsService;
    @Mock DeviceInstalledAppsRepository deviceInstalledAppsRepository;
    @Mock DeviceSpecificationRepository deviceSpecificationRepository;
    @Mock ApplicationUserRepository applicationUserRepository;
    @Mock APICallClient apiCallService;
    @Mock JdbcTemplate jdbcTemplate;

    @InjectMocks ManagedSoftwareService service;

    // ---- field-list resource read ----

    @Test
    void getManagedSoftwareFieldsList_readsAndReturnsJsonArray() {
        String json = service.getManagedSoftwareFieldsList("u", "v");
        assertThat(json).isNotNull().contains("column");
    }

    // ---- getInventoryApplications guard ----

    @Test
    void getInventoryApplications_nullFromApi_returnsEmpty() {
        when(apiCallService.getAllInventoryApplications("v")).thenReturn(null);
        assertThat(service.getInventoryApplications("u", "v", "dock")).isEmpty();
    }

    // ---- deleteManagedSoftware ----

    @Test
    void deleteManagedSoftware_noApplicationId_skipsUsersAndDeletes() {
        when(deviceInstalledAppsRepository.clearManagedSoftwareIdAndRiskStatus("ms1")).thenReturn(2);
        when(managedSoftwareRepository.getApplicationIdByManagedSoftwareId("ms1")).thenReturn(null);

        service.deleteManagedSoftware("u", "v", "dock", "ms1");

        verify(managedSoftwareRepository).deleteById("ms1");
        verify(apiCallService, never()).getApplicationUsersFromInventory(any(), any());
    }

    @Test
    void deleteManagedSoftware_nullUsersFromInventory_throws() {
        when(deviceInstalledAppsRepository.clearManagedSoftwareIdAndRiskStatus("ms1")).thenReturn(1);
        when(managedSoftwareRepository.getApplicationIdByManagedSoftwareId("ms1")).thenReturn("app1");
        when(apiCallService.getApplicationUsersFromInventory("v", "app1")).thenReturn(null);

        assertThatThrownBy(() -> service.deleteManagedSoftware("u", "v", "dock", "ms1"))
                .isInstanceOf(RuntimeException.class);
        verify(managedSoftwareRepository, never()).deleteById(any());
    }

    // ---- update sync loop + helper ----

    @Test
    void updateManagedSoftwareDetailsSync_nullOrEmpty_returnsEmpty() {
        assertThat(service.updateManagedSoftwareDetailsSync(null)).isEmpty();
        assertThat(service.updateManagedSoftwareDetailsSync(List.of())).isEmpty();
    }

    @Test
    void updateManagedSoftwareDetailsSync_noMatch_recordsFailure() {
        InventoryApplicationDTO dto = mock(InventoryApplicationDTO.class);
        when(dto.getId()).thenReturn("app1");
        when(managedSoftwareRepository.findByApplicationId("app1")).thenReturn(null);

        assertThat(service.updateManagedSoftwareDetailsSync(List.of(dto))).containsExactly("app1");
    }

    @Test
    void updateSingleManagedSoftwareTransaction_match_savesAndReturnsTrue() {
        InventoryApplicationDTO dto = mock(InventoryApplicationDTO.class);
        ManagedSoftware ms = mock(ManagedSoftware.class);
        when(managedSoftwareRepository.findByApplicationId(any())).thenReturn(ms);

        assertThat(service.updateSingleManagedSoftwareTransaction(dto)).isTrue();
        verify(managedSoftwareRepository).save(ms);
    }

    @Test
    void updateSingleManagedSoftwareTransaction_noMatch_returnsFalse() {
        InventoryApplicationDTO dto = mock(InventoryApplicationDTO.class);
        when(managedSoftwareRepository.findByApplicationId(any())).thenReturn(null);
        assertThat(service.updateSingleManagedSoftwareTransaction(dto)).isFalse();
    }

    // ---- clear sync loop + helper ----

    @Test
    void clearManagedSoftwareDetailsSync_nullOrEmpty_returnsEmpty() {
        assertThat(service.clearManagedSoftwareDetailsSync(null)).isEmpty();
        assertThat(service.clearManagedSoftwareDetailsSync(List.of())).isEmpty();
    }

    @Test
    void clearManagedSoftwareDetailsSync_noMatch_recordsFailure() {
        InventoryApplicationDTO dto = mock(InventoryApplicationDTO.class);
        when(dto.getId()).thenReturn("app2");
        when(managedSoftwareRepository.findByApplicationId("app2")).thenReturn(null);

        assertThat(service.clearManagedSoftwareDetailsSync(List.of(dto))).containsExactly("app2");
    }

    @Test
    void clearSingleManagedSoftwareTransaction_match_clearsAndReturnsTrue() {
        InventoryApplicationDTO dto = mock(InventoryApplicationDTO.class);
        ManagedSoftware ms = mock(ManagedSoftware.class);
        when(ms.getId()).thenReturn("ms9");
        when(managedSoftwareRepository.findByApplicationId(any())).thenReturn(ms);

        assertThat(service.clearSingleManagedSoftwareTransaction(dto)).isTrue();
        verify(managedSoftwareRepository).save(ms);
        verify(deviceInstalledAppsRepository).clearRiskStatusByManagedSoftwareId("ms9");
        verify(applicationUserRepository).deleteByManagedSoftwareId("ms9");
    }

    @Test
    void clearSingleManagedSoftwareTransaction_noMatch_returnsFalse() {
        InventoryApplicationDTO dto = mock(InventoryApplicationDTO.class);
        when(managedSoftwareRepository.findByApplicationId(any())).thenReturn(null);
        assertThat(service.clearSingleManagedSoftwareTransaction(dto)).isFalse();
    }
}
