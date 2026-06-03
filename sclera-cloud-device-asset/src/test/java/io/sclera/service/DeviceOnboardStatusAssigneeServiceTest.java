package io.sclera.service;

import io.sclera.Repository.DeviceOnboardStatusAssigneeRepository;
import io.sclera.dto.DeviceOnboardStatusAssigneeDTO;
import io.sclera.dto.DeviceOnboardStatusDTO;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for DeviceOnboardStatusAssigneeService: add loop, delegate reads, and the
 * update branching (null skip, empty deletes, non-empty delete-then-add).
 */
@ExtendWith(MockitoExtension.class)
class DeviceOnboardStatusAssigneeServiceTest {

    @Mock DeviceOnboardStatusAssigneeRepository repository;

    @InjectMocks DeviceOnboardStatusAssigneeService service;

    @Test
    void addDeviceOnboardStatusAssignees_setsIdsAndPersists() {
        DeviceOnboardStatusAssigneeDTO a = mock(DeviceOnboardStatusAssigneeDTO.class);
        service.addDeviceOnboardStatusAssignees("os1", Set.of(a));
        verify(a).setId(any());
        verify(a).setDevice_onboard_status_id("os1");
        verify(repository).addDeviceOnboardStatusAssignees(any(), any(), eq("secondary"), any());
    }

    @Test
    void deleteByStatusId_delegates() {
        service.deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId("os1");
        verify(repository).deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId("os1");
    }

    @Test
    void getDeviceOnboardStatusAssignees_delegates() {
        Set<DeviceOnboardStatusAssigneeDTO> set = Set.of(mock(DeviceOnboardStatusAssigneeDTO.class));
        when(repository.getDeviceOnboardStatusAssignees("os1")).thenReturn(set);
        assertThat(service.getDeviceOnboardStatusAssignees("os1")).isSameAs(set);
    }

    @Test
    void getDeviceOnboardStatusAssigneesEmail_delegates() {
        Set<String> emails = Set.of("a@x.com");
        when(repository.getDeviceOnboardStatusAssigneesEmail()).thenReturn(emails);
        assertThat(service.getDeviceOnboardStatusAssigneesEmail()).isSameAs(emails);
    }

    @Test
    void update_nullAssignees_doesNothing() {
        DeviceOnboardStatusDTO dto = mock(DeviceOnboardStatusDTO.class);
        when(dto.getDevice_onboard_status_assignees()).thenReturn(null);

        service.updateDeviceOnboardStautsAssignee(dto);

        verify(repository, never()).deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId(any());
    }

    @Test
    void update_emptyAssignees_deletesOnly() {
        DeviceOnboardStatusDTO dto = mock(DeviceOnboardStatusDTO.class);
        when(dto.getId()).thenReturn("os1");
        when(dto.getDevice_onboard_status_assignees()).thenReturn(Set.of());

        service.updateDeviceOnboardStautsAssignee(dto);

        verify(repository).deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId("os1");
        verify(repository, never()).addDeviceOnboardStatusAssignees(any(), any(), any(), any());
    }

    @Test
    void update_nonEmptyAssignees_deletesThenAdds() {
        DeviceOnboardStatusDTO dto = mock(DeviceOnboardStatusDTO.class);
        when(dto.getId()).thenReturn("os1");
        when(dto.getDevice_onboard_status_assignees())
                .thenReturn(Set.of(mock(DeviceOnboardStatusAssigneeDTO.class)));

        service.updateDeviceOnboardStautsAssignee(dto);

        verify(repository).deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId("os1");
        verify(repository).addDeviceOnboardStatusAssignees(any(), any(), eq("secondary"), any());
    }
}
