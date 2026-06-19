package io.sclera.vdms.subscriber;

import io.dapr.client.DaprClient;
import io.sclera.dapr.events.UserActionLogEvent;
import io.sclera.vdms.model.UserActionLog;
import io.sclera.vdms.repository.UserActionLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class UserActionLogVdmsSubscriberTest {

    private final DaprClient dapr = Mockito.mock(DaprClient.class);
    private final UserActionLogRepository repo = Mockito.mock(UserActionLogRepository.class);
    private final UserActionLogVdmsSubscriber subscriber = new UserActionLogVdmsSubscriber(dapr, repo);

    @Test
    void handleEvent_mapsDtoFieldsAndPersists() {
        UserActionLogEvent event = new UserActionLogEvent(
            "ops@sclera.com", "maximo", "ADD", "Created config",
            "success", "maximo_configuration", "cfg-1", "demo-vdms-001", "req-abc");

        subscriber.handleEvent(event);

        ArgumentCaptor<UserActionLog> captor = ArgumentCaptor.forClass(UserActionLog.class);
        verify(repo).save(captor.capture());
        UserActionLog saved = captor.getValue();
        assertThat(saved.getUserEmail()).isEqualTo("ops@sclera.com");
        assertThat(saved.getType()).isEqualTo("maximo");
        assertThat(saved.getAction()).isEqualTo("ADD");
        assertThat(saved.getStatus()).isEqualTo("success");
        assertThat(saved.getMessage()).isEqualTo("Created config");
        assertThat(saved.getAffectedRecordId()).isEqualTo("cfg-1");
        assertThat(saved.getVdmsId()).isEqualTo("demo-vdms-001");
        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
