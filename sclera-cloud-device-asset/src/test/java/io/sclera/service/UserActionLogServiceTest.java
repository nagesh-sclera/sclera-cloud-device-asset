package io.sclera.service;

import io.sclera.client.VdmsClient;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.DeviceAuditEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for UserActionLogService: audit-event publishing, vdmsId resolution caching,
 * publish-failure tolerance, and batch fan-out.
 */
@ExtendWith(MockitoExtension.class)
class UserActionLogServiceTest {

    @Mock VdmsClient vdmsClient;

    @InjectMocks UserActionLogService service;

    private PublishResult ok() {
        return new PublishResult(true, "e1", null);
    }

    @Test
    void addUserAction_publishesAuditEvent() {
        when(vdmsClient.getVdmsId()).thenReturn(Map.of("vdmsId", "v1"));
        when(vdmsClient.publishEvent(eq("device.audit"), any())).thenReturn(ok());

        service.addUserAction("user", "type", "CREATE", "msg", "SUCCESS", "sub", "rec1");

        verify(vdmsClient).publishEvent(eq("device.audit"), any(DeviceAuditEvent.class));
    }

    @Test
    void addUserAction_publishFailure_doesNotThrow() {
        when(vdmsClient.getVdmsId()).thenReturn(Map.of("vdmsId", "v1"));
        PublishResult fail = new PublishResult(false, "e1", "boom");
        when(vdmsClient.publishEvent(eq("device.audit"), any())).thenReturn(fail);

        // no exception expected
        service.addUserAction("user", "type", "CREATE", "msg", "FAIL", "sub", "rec1");

        verify(vdmsClient).publishEvent(eq("device.audit"), any());
    }

    @Test
    void addUserAction_cachesVdmsId() {
        when(vdmsClient.getVdmsId()).thenReturn(Map.of("vdmsId", "v1"));
        when(vdmsClient.publishEvent(eq("device.audit"), any())).thenReturn(ok());

        service.addUserAction("user", "type", "CREATE", "m", "S", "sub", "r1");
        service.addUserAction("user", "type", "UPDATE", "m", "S", "sub", "r2");

        verify(vdmsClient, times(1)).getVdmsId();
        verify(vdmsClient, times(2)).publishEvent(eq("device.audit"), any());
    }

    @Test
    void batchUpdate_null_doesNothing() {
        service.batchUpdateUserActionLogs(null);
        verify(vdmsClient, never()).publishEvent(any(), any());
    }

    @Test
    void batchUpdate_publishesPerEntry() {
        when(vdmsClient.getVdmsId()).thenReturn(Map.of("vdmsId", "v1"));
        when(vdmsClient.publishEvent(eq("device.audit"), any())).thenReturn(ok());

        UserActionLogDTO entry = mock(UserActionLogDTO.class);
        when(entry.getAction()).thenReturn("CREATE");

        service.batchUpdateUserActionLogs(List.of(entry, entry));

        verify(vdmsClient, times(2)).publishEvent(eq("device.audit"), any());
    }
}
