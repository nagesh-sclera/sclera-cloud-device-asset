package io.sclera.conditions;

import io.dapr.client.domain.CloudEvent;
import io.sclera.service.ConditionsService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ConditionAlertSubscriberTest {

    @Test
    void appliesMonolithSignatureConstants() {
        ConditionsService conditions = mock(ConditionsService.class);
        ConditionAlertSubscriber sub = new ConditionAlertSubscriber(conditions);

        sub.handleEvent(Map.of("measuringInstrumentId", "mi-1", "value", "42"));

        verify(conditions).updateConditionAlert("measuring_instrument", "mi-1", "", "42", "", "sync");
    }

    @Test
    void nullOrMissingFieldsDoNotThrow() {
        ConditionsService conditions = mock(ConditionsService.class);
        ConditionAlertSubscriber sub = new ConditionAlertSubscriber(conditions);
        sub.handleEvent(Map.of());                 // missing keys
        sub.handleEvent(null);                     // null payload
        // no exception = pass
    }
}
