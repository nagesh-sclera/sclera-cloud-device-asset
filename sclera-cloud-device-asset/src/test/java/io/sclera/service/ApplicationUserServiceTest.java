package io.sclera.service;

import io.sclera.Repository.ApplicationUserRepository;
import io.sclera.Repository.DeviceInstalledAppsRepository;
import io.sclera.Repository.DeviceSpecificationRepository;
import io.sclera.Repository.ManagedSoftwareRepository;
import io.sclera.dto.InventoryApplicationUserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for ApplicationUserService sync orchestration: null/empty short-circuit, and the
 * failed-id collection for both upsert and delete sync (success vs failing inner transaction).
 * The inner transactional methods run against mocked repositories.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationUserServiceTest {

    @Mock ManagedSoftwareRepository managedSoftwareRepository;
    @Mock ApplicationUserRepository applicationUserRepository;
    @Mock DeviceInstalledAppsRepository deviceInstalledAppsRepository;
    @Mock DeviceSpecificationRepository deviceSpecificationRepository;

    @InjectMocks ApplicationUserService service;

    private InventoryApplicationUserDTO user(String id, Integer status) {
        InventoryApplicationUserDTO u = mock(InventoryApplicationUserDTO.class);
        lenient().when(u.getId()).thenReturn(id);
        lenient().when(u.getApplicationId()).thenReturn("app1");
        lenient().when(u.getStatus()).thenReturn(status);
        return u;
    }

    // ---- upsertApplicationUsersSync --------------------------------------

    @Test
    void upsertSync_null_returnsEmpty() {
        assertThat(service.upsertApplicationUsersSync(null)).isEmpty();
    }

    @Test
    void upsertSync_empty_returnsEmpty() {
        assertThat(service.upsertApplicationUsersSync(List.of())).isEmpty();
    }

    @Test
    void upsertSync_upsertReturnsZero_collectsFailedId() {
        when(applicationUserRepository.upsertApplicationUsers(any(), any(), any(), any())).thenReturn(0);

        Set<String> failed = service.upsertApplicationUsersSync(List.of(user("u1", 0)));

        assertThat(failed).containsExactly("u1");
    }

    @Test
    void upsertSync_happyPath_noFailures() {
        when(applicationUserRepository.upsertApplicationUsers(any(), any(), any(), any())).thenReturn(1);
        when(managedSoftwareRepository.findIdByApplicationId("app1")).thenReturn("ms1");
        when(applicationUserRepository.updateManagedSoftwareIdByUserId("u1", "ms1")).thenReturn(1);
        when(deviceInstalledAppsRepository.getDeviceSpecIdsByManagedSoftwareId("ms1")).thenReturn(Set.of());
        when(deviceSpecificationRepository.findByIdIn(Set.of())).thenReturn(List.of());

        Set<String> failed = service.upsertApplicationUsersSync(List.of(user("u1", 0)));

        assertThat(failed).isEmpty();
    }

    // ---- deleteApplicationUsersSync --------------------------------------

    @Test
    void deleteSync_null_returnsEmpty() {
        assertThat(service.deleteApplicationUsersSync(null)).isEmpty();
    }

    @Test
    void deleteSync_noManagedSoftware_collectsFailedId() {
        when(managedSoftwareRepository.findIdByApplicationId("app1")).thenReturn(null);

        Set<String> failed = service.deleteApplicationUsersSync(List.of(user("u1", 1)));

        assertThat(failed).containsExactly("u1");
    }

    @Test
    void deleteSync_happyPath_noFailures() {
        when(managedSoftwareRepository.findIdByApplicationId("app1")).thenReturn("ms1");
        when(applicationUserRepository.deleteApplicationUsersById("u1")).thenReturn(1);
        when(deviceInstalledAppsRepository.getDeviceSpecIdsByManagedSoftwareId("ms1")).thenReturn(Set.of());
        when(deviceSpecificationRepository.findByIdIn(Set.of())).thenReturn(List.of());

        Set<String> failed = service.deleteApplicationUsersSync(List.of(user("u1", 1)));

        assertThat(failed).isEmpty();
    }
}
