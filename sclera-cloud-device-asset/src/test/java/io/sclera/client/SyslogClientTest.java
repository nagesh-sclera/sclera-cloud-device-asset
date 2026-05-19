package io.sclera.client;

import com.alibaba.fastjson.JSONObject;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyslogClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        SyslogClient client = new SyslogClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertion ─────────────────────────────

    @Test
    void getSyslogExcludeDeviceIds_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-audit"),
                eq("syslog/getSyslogExcludeDeviceIds"),
                any(),
                any(HttpExtension.class),
                eq(JSONObject.class)))
            .thenReturn(Mono.just(new JSONObject()));

        SyslogClient client = new SyslogClient(dapr);
        JSONObject result = client.getSyslogExcludeDeviceIds("user", "vdms1", "docker1", "type1");
        assertThat(result).isNotNull();

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-audit"),
                eq("syslog/getSyslogExcludeDeviceIds"),
                any(),
                extCaptor.capture(),
                eq(JSONObject.class));
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
    }

    // ── resilience: exception swallowing, documented default ─────────────────

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class), any(Class.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        SyslogClient client = new SyslogClient(dapr);
        assertThat(client).isNotNull();

        JSONObject result = client.getSyslogExcludeDeviceIds("user", "vdms1", "docker1", "type1");
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(JSONObject.class);
    }

    @Test
    void getSyslogExcludeDeviceIdsReturnsEmptyJsonOnDaprDown() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class), any(Class.class)))
            .thenReturn(Mono.error(new RuntimeException("network failure")));

        SyslogClient client = new SyslogClient(dapr);
        JSONObject result = client.getSyslogExcludeDeviceIds("admin", "vdms-123", "nginx", "profile-a");
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }
}
