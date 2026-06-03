package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.DeviceInstalledAppsRepository;
import io.sclera.Repository.DeviceSpecificationRepository;
import io.sclera.Repository.ManagedSoftwareRepository;
import io.sclera.client.APICallClient;
import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean main methods of ManagedSoftwareService: delegates,
 * insert/update upsert, license/count map aggregation, risk-compliance branches, and the
 * status-recompute path of getAllManagedSoftwares. The inventory-sync, JSON-file and
 * jdbc-heavy methods are deferred.
 */
@ExtendWith(MockitoExtension.class)
class ManagedSoftwareServiceTest {

    @Mock ManagedSoftwareRepository managedSoftwareRepository;
    @Mock DeviceInstalledAppsRepository deviceInstalledAppsRepository;
    @Mock DeviceSpecificationRepository deviceSpecificationRepository;
    @Mock APICallClient apiCallService;

    @InjectMocks ManagedSoftwareService service;

    // ---- delegates --------------------------------------------------------

    @Test
    void getManagedSoftwareById_delegates() {
        ManagedSoftwareDTO dto = mock(ManagedSoftwareDTO.class);
        when(managedSoftwareRepository.getManagedSoftwareById("ms1")).thenReturn(dto);
        assertThat(service.getManagedSoftwareById("ms1")).isSameAs(dto);
    }

    @Test
    void getManagedSoftwareUsers_delegates() {
        List<ManagedSoftwareUsersDTO> users = List.of(mock(ManagedSoftwareUsersDTO.class));
        when(managedSoftwareRepository.getManagedSoftwareUsers("ms1")).thenReturn(users);
        assertThat(service.getManagedSoftwareUsers("u", "v", "dock", "ms1")).isSameAs(users);
    }

    @Test
    void getManagedSoftwareUsersList_delegates() {
        List<String> emails = List.of("a@b.com");
        when(deviceSpecificationRepository.findDistinctEmail()).thenReturn(emails);
        assertThat(service.getManagedSoftwareUsersList("u", "v")).isSameAs(emails);
    }

    @Test
    void getManagedSoftwareOSTypesList_delegates() {
        List<String> os = List.of("Windows", "macOS");
        when(deviceSpecificationRepository.findDistinctOsType()).thenReturn(os);
        assertThat(service.getManagedSoftwareOSTypesList("u", "v")).isSameAs(os);
    }

    // ---- insertManagedSoftware branches ----------------------------------

    @Test
    void insertManagedSoftware_existingName_returnsExistingId() {
        when(managedSoftwareRepository.getManagedSoftwareIdByName("Acme")).thenReturn(Optional.of("existing-id"));

        assertThat(service.insertManagedSoftware("Acme", "AcmeCorp")).isEqualTo("existing-id");
        verify(managedSoftwareRepository, never()).upsertManagedSoftware(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void insertManagedSoftware_newName_generatesIdAndUpserts() {
        when(managedSoftwareRepository.getManagedSoftwareIdByName("Acme")).thenReturn(Optional.empty());

        String id = service.insertManagedSoftware("Acme", "AcmeCorp");

        assertThat(id).isNotNull().isNotEqualTo("existing-id");
        verify(managedSoftwareRepository).upsertManagedSoftware(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateManagedSoftware_upsertsAndReturnsRefetched() {
        ManagedSoftwareDTO in = mock(ManagedSoftwareDTO.class);
        when(in.getId()).thenReturn("ms1");
        ManagedSoftwareDTO refetched = mock(ManagedSoftwareDTO.class);
        when(managedSoftwareRepository.getManagedSoftwareById("ms1")).thenReturn(refetched);

        assertThat(service.updateManagedSoftware("u", "v", in)).isSameAs(refetched);
        verify(managedSoftwareRepository).upsertManagedSoftware(
                eq("ms1"), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ---- license / count maps --------------------------------------------

    @Test
    void getManagedSoftwareLicense_buildsMapFromInventoryAndCompliance() {
        JSONObject licenseJson = new JSONObject();
        licenseJson.put("licensePurchased", 10);
        licenseJson.put("licenseUsed", 5);
        when(apiCallService.getLicenseDetailsFromInventory("v", "app1")).thenReturn(licenseJson);
        when(deviceInstalledAppsRepository.getCompliantRiskStatusCount("ms1")).thenReturn(3);

        Map<String, Integer> result = service.getManagedSoftwareLicense("u", "v", "dock", "ms1", "app1");

        assertThat(result).containsEntry("licensePurchased", 10)
                .containsEntry("licenseUsed", 5)
                .containsEntry("compliantLicense", 3);
    }

    @Test
    void getManagedSoftwareLicense_nullInventory_throws() {
        when(apiCallService.getLicenseDetailsFromInventory("v", "app1")).thenReturn(null);
        assertThatThrownBy(() -> service.getManagedSoftwareLicense("u", "v", "dock", "ms1", "app1"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getManagedSoftwareCount_aggregatesAllCounts() {
        when(managedSoftwareRepository.getAllStatusCounts()).thenReturn(20);
        when(managedSoftwareRepository.getActiveStatusCounts()).thenReturn(12);
        when(managedSoftwareRepository.getExpiredStatusCounts()).thenReturn(5);
        when(managedSoftwareRepository.getOthersStatusCounts()).thenReturn(3);
        when(managedSoftwareRepository.getMonthlySubscribedCount()).thenReturn(7);
        when(managedSoftwareRepository.getYearlySubscribedCount()).thenReturn(9);

        Map<String, Integer> counts = service.getManagedSoftwareCount("u", "v", "dock");

        assertThat(counts).containsEntry("all", 20).containsEntry("active", 12)
                .containsEntry("expired", 5).containsEntry("others", 3)
                .containsEntry("monthlySubscribed", 7).containsEntry("yearlySubscribed", 9);
    }

    // ---- risk and compliance ---------------------------------------------

    @Test
    void getAllRiskAndCompliances_noRiskyDevices_returnsEmpty() {
        when(deviceInstalledAppsRepository.getRiskyDeviceSpecIdsByManagedSoftwareId("ms1")).thenReturn(Set.of());
        assertThat(service.getAllRiskAndCompliances("u", "v", "dock", "ms1")).isEmpty();
    }

    @Test
    void riskAndComplianceAction_rowsAffected_noThrow() {
        JSONObject data = new JSONObject();
        data.put("deviceSpecificationId", "ds1");
        when(deviceInstalledAppsRepository.updateRiskStatusForDevices(Set.of("ds1"), "ms1", 2)).thenReturn(1);

        assertThatCode(() -> service.riskAndComplianceAction("u", "v", "dock", "ms1", data))
                .doesNotThrowAnyException();
    }

    @Test
    void riskAndComplianceAction_noRowsAffected_throws() {
        JSONObject data = new JSONObject();
        data.put("deviceSpecificationId", "ds1");
        when(deviceInstalledAppsRepository.updateRiskStatusForDevices(Set.of("ds1"), "ms1", 2)).thenReturn(0);

        assertThatThrownBy(() -> service.riskAndComplianceAction("u", "v", "dock", "ms1", data))
                .isInstanceOf(RuntimeException.class);
    }

    // ---- getAllManagedSoftwares status recompute -------------------------

    @Test
    void getAllManagedSoftwares_recomputesStatusWhenChanged() {
        ManagedSoftwareDTO dto = mock(ManagedSoftwareDTO.class);
        when(dto.getApplicationId()).thenReturn("app1");
        when(dto.getId()).thenReturn("ms1");
        when(dto.getSubscriptionStartDate()).thenReturn(BigInteger.ZERO);
        when(dto.getSubscriptionEndDate()).thenReturn(BigInteger.valueOf(Long.MAX_VALUE)); // now is within -> active
        when(dto.getStatus()).thenReturn("expired"); // differs from computed "active"
        // pageNo=1, pageSize=10 -> offset 0
        when(managedSoftwareRepository.getAllManagedSoftwares("cond", "key", 0, 10)).thenReturn(List.of(dto));

        service.getAllManagedSoftwares("u", "v", "dock", "cond", "key", 1, 10);

        verify(managedSoftwareRepository).updateStatusById("ms1", "active");
        verify(dto).setStatus("active");
    }
}
