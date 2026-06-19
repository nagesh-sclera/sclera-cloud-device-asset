package io.sclera.workorder.service;

import io.sclera.workorder.service.impl.UserActionLogServiceImpl;
import io.sclera.workorder.client.UserActionLogClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserActionLogServiceTest {

    @Mock private UserActionLogClient userActionLogClient;
    @InjectMocks private UserActionLogServiceImpl userActionLogService;

    @Test
    void addUserAction_delegatesToClient() {
        userActionLogService.addUserAction("alice", "v1", "maximo", "ADD", "success", "maximo_configuration", "cfg-1", "v1");

        verify(userActionLogClient).addUserAction("alice", "maximo", "ADD", "success", "maximo_configuration", "cfg-1", "v1", "v1");
    }

    @Test
    void addUserAction_swallowsClientException() {
        doThrow(new RuntimeException("publish failed"))
                .when(userActionLogClient).addUserAction(any(), any(), any(), any(), any(), any(), any(), any());

        // must not throw
        userActionLogService.addUserAction("alice", "v1", "maximo", "ADD", "success", "maximo_configuration", "cfg-1", "v1");
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
}
