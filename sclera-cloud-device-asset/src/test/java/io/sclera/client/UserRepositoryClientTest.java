package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserRepositoryClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        UserRepositoryClient client = new UserRepositoryClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void getOrganisationIdByUserEmail_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-identity"), eq("user/getOrganisationIdByUserEmail"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        UserRepositoryClient client = new UserRepositoryClient(dapr);
        String result = client.getOrganisationIdByUserEmail("test@test.com");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq("sclera-identity"), eq("user/getOrganisationIdByUserEmail"), any(), extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void getAllUsers_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-identity"), eq("user/getAllUsers"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        UserRepositoryClient client = new UserRepositoryClient(dapr);
        var result = client.getAllUsers(10, 0, "");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq("sclera-identity"), eq("user/getAllUsers"), any(), extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEmpty();
    }

    @Test
    void returnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
                .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        UserRepositoryClient client = new UserRepositoryClient(dapr);
        assertThat(client.getOrganisationIdByUserEmail("test@test.com")).isNull();
        assertThat(client.checkUser("test@test.com", "org-1")).isEqualTo(0);
        assertThat(client.getAllUsers(10, 0, "")).isEmpty();
        assertThat(client.getUserByEmail("test@test.com")).isNull();
        assertThat(client.getMasterUserEmail()).isNull();
        // void methods swallow
        client.insertUser("test@test.com", "co", "by", BigInteger.ONE, "name", "123", "mobile", "val", "web", "org", "url", "en", "ADMIN");
        client.deleteById("test@test.com");
    }
}
