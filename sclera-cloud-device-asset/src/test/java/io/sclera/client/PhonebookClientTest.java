package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.PhonebookAddressDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PhonebookClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        PhonebookClient client = new PhonebookClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertion ─────────────────────────────

    @Test
    void addPhoneBookByDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-identity"),
                eq("phonebook/addPhoneBookByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        PhonebookClient client = new PhonebookClient(dapr);
        Set<PhonebookAddressDto> vendors = Collections.emptySet();
        client.addPhoneBookByDeviceId("user", "vdms1", "docker1", vendors, "device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-identity"),
                eq("phonebook/addPhoneBookByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void getPhoneAddressById_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-identity"),
                eq("phonebook/getPhoneAddressById"),
                any(),
                any(HttpExtension.class),
                eq(PhonebookAddressDto.class)))
            .thenReturn(Mono.justOrEmpty(null));

        PhonebookClient client = new PhonebookClient(dapr);
        PhonebookAddressDto result = client.getPhoneAddressById("vendor-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-identity"),
                eq("phonebook/getPhoneAddressById"),
                any(),
                extCaptor.capture(),
                eq(PhonebookAddressDto.class));
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class), any(Class.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        PhonebookClient client = new PhonebookClient(dapr);
        // void method swallows
        client.addPhoneBookByDeviceId("user", "vdms1", "docker1", Collections.emptySet(), "device-001");
        // returns null on error
        PhonebookAddressDto result = client.getPhoneAddressById("vendor-001");
        assertThat(result).isNull();
    }
}
