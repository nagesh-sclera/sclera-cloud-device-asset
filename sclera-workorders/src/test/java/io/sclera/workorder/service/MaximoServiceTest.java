package io.sclera.workorder.service;

import io.sclera.workorder.service.impl.MaximoServiceImpl;
import com.alibaba.fastjson.JSONObject;
import io.sclera.workorder.client.MaximoApiClient;
import io.sclera.workorder.client.UserActionLogClient;
import io.sclera.workorder.client.VdmsClient;
import io.sclera.workorder.dto.MaximoConfigurationDTO;
import io.sclera.workorder.dto.MaximoDTO;
import io.sclera.workorder.dto.VdmsDetailsDTO;
import io.sclera.workorder.exception.MaximoException;
import io.sclera.workorder.exception.VdmsNotFoundException;
import io.sclera.workorder.repository.MaximoConfigurationRepository;
import io.sclera.workorder.util.MaximoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alibaba.fastjson.JSONArray;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaximoServiceTest {

    @Mock private MaximoConfigurationRepository maximoConfigurationRepository;
    @Mock private MaximoApiClient maximoApiClient;
    @Mock private MaximoUtils maximoUtils;
    @Mock private UserActionLogClient userActionLogClient;
    @Mock private VdmsClient vdmsClient;

    @InjectMocks private MaximoServiceImpl maximoService;

    private MaximoConfigurationDTO sampleDtoWithSecrets() {
        return new MaximoConfigurationDTO(
                "cfg-1", "name", "https://maximo/oslc", "https://maximo/auth",
                "client-id", "client-secret", "[]");
    }

    @BeforeEach
    void resetCacheBehavior() {
        lenient().when(maximoUtils.getServerUrl(any())).thenReturn(null);
        lenient().when(maximoUtils.getToken(any())).thenReturn(null);
        lenient().when(maximoUtils.getTokenCreatedAt(any())).thenReturn(null);
    }

    // ── Upsert ───────────────────────────────────────────────────────────

    @Test
    void upsertMaximoConfiguration_generatesIdWhenAbsent_andEmitsAddAudit() {
        MaximoConfigurationDTO incoming = new MaximoConfigurationDTO(
                null, "n", "srv", "auth", "c", "s", "[]");

        String result = maximoService.upsertMaximoConfiguration("user1", "v1", incoming);

        assertThat(result).isEqualTo("success");
        assertThat(incoming.getId()).isNotNull();
        verify(maximoConfigurationRepository).upsertMaximoConfiguration(
                eq(incoming.getId()), eq("n"), eq("srv"), eq("auth"),
                eq("c"), eq("s"), eq("[]"), eq("v1"));
        verify(userActionLogClient).addUserAction(
                eq("user1"), eq("maximo"), eq("ADD"), anyString(), eq("success"),
                eq("maximo_configuration"), eq(incoming.getId()), eq("v1"));
    }

    @Test
    void upsertMaximoConfiguration_preservesIdWhenProvided_andEmitsUpdateAudit() {
        MaximoConfigurationDTO incoming = sampleDtoWithSecrets();

        maximoService.upsertMaximoConfiguration("user1", "v1", incoming);

        verify(userActionLogClient).addUserAction(
                eq("user1"), eq("maximo"), eq("UPDATE"), anyString(), eq("success"),
                eq("maximo_configuration"), eq("cfg-1"), eq("v1"));
    }

    // ── Delete ───────────────────────────────────────────────────────────

    @Test
    void deleteMaximoConfiguration_successAuditOnHappyPath() {
        maximoService.deleteMaximoConfiguration("user1", "cfg-1", "v1");

        verify(maximoConfigurationRepository).deleteById("cfg-1");
        verify(userActionLogClient).addUserAction(
                eq("user1"), eq("maximo"), eq("DELETE"), anyString(), eq("success"),
                eq("maximo_configuration"), eq("cfg-1"), eq("v1"));
    }

    // ── Work-order 401 retry ─────────────────────────────────────────────

    @Test
    void getMaximoWorkOrders_retriesOnceWithFreshTokenWhen401() {
        when(maximoConfigurationRepository.getMaximoConfigByVdmsId("v1"))
                .thenReturn(sampleDtoWithSecrets());
        when(maximoApiClient.generateMaximoAccessToken(any())).thenReturn("token-2");

        // First call throws 401, second returns a workorder
        JSONObject second = new JSONObject();
        second.put("member", new com.alibaba.fastjson.JSONArray());
        when(maximoApiClient.getAllWorkorders(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new MaximoException("Token Expired", 401, "x"))
                .thenReturn(second);

        List<MaximoDTO> result = maximoService.getMaximoWorkOrders("v1", "all", 1, 10, null);

        assertThat(result).isEmpty();
        verify(maximoApiClient, times(2)).getAllWorkorders(any(), any(), any(), any(), anyInt(), anyInt());
    }

    // ── New sample Dapr method ───────────────────────────────────────────

    @Test
    void getVdmsDetailsForMaximoConfig_returnsValueOnHappyPath() {
        VdmsDetailsDTO dto = new VdmsDetailsDTO();
        dto.setId("v1");
        dto.setName("Site A");
        when(vdmsClient.getVdmsDetails("v1")).thenReturn(Optional.of(dto));

        VdmsDetailsDTO result = maximoService.getVdmsDetailsForMaximoConfig("v1");

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getVdmsDetailsForMaximoConfig_throwsNotFoundWhenSiblingReturnsEmpty() {
        when(vdmsClient.getVdmsDetails("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maximoService.getVdmsDetailsForMaximoConfig("missing"))
                .isInstanceOf(VdmsNotFoundException.class)
                .hasMessageContaining("missing");
    }

    // ── getServerUrl ─────────────────────────────────────────────────────

    @Test
    void getServerUrl_returnsCachedValueWithoutHittingRepo() {
        when(maximoUtils.getServerUrl("v1")).thenReturn("https://cached.url/oslc");

        String result = maximoService.getServerUrl("v1");

        assertThat(result).isEqualTo("https://cached.url/oslc");
        verify(maximoConfigurationRepository, never()).getMaximoConfigByVdmsId(any());
    }

    @Test
    void getServerUrl_loadsFromRepoAndCachesWhenNull() {
        when(maximoUtils.getServerUrl("v1")).thenReturn(null);
        when(maximoConfigurationRepository.getMaximoConfigByVdmsId("v1")).thenReturn(sampleDtoWithSecrets());

        maximoService.getServerUrl("v1");

        verify(maximoConfigurationRepository).getMaximoConfigByVdmsId("v1");
        verify(maximoUtils).setServerUrl("v1", "https://maximo/oslc");
    }

    // ── getToken ─────────────────────────────────────────────────────────

    @Test
    void getToken_returnsCachedTokenWhenStillValidAndNotForced() {
        when(maximoUtils.getToken("v1")).thenReturn("cached-token");
        when(maximoUtils.getTokenCreatedAt("v1")).thenReturn(System.currentTimeMillis() - 1000L);

        String result = maximoService.getToken(false, "v1");

        assertThat(result).isEqualTo("cached-token");
        verify(maximoConfigurationRepository, never()).getMaximoConfigByVdmsId(any());
    }

    @Test
    void getToken_fetchesFreshTokenWhenCacheEmpty() {
        // maximoUtils.getToken("v1") returns null (from resetCacheBehavior)
        when(maximoConfigurationRepository.getMaximoConfigByVdmsId("v1")).thenReturn(sampleDtoWithSecrets());
        when(maximoApiClient.generateMaximoAccessToken(any())).thenReturn("fresh-token");

        String result = maximoService.getToken(false, "v1");

        assertThat(result).isEqualTo("fresh-token");
        verify(maximoUtils).setToken("v1", "fresh-token");
    }

    // ── updateToken ───────────────────────────────────────────────────────

    @Test
    void updateToken_delegatesSettersToUtils() {
        maximoService.updateToken("my-token", "v1");

        verify(maximoUtils).setToken("v1", "my-token");
        verify(maximoUtils).setTokenCreatedAt(eq("v1"), anyLong());
    }

    // ── getMaximoConfigurationByVdmsId ────────────────────────────────────

    @Test
    void getMaximoConfigurationByVdmsId_delegatesToRepository() {
        MaximoConfigurationDTO expected = new MaximoConfigurationDTO("c1", "n", "srv", "auth", "[]");
        when(maximoConfigurationRepository.getMaximoConfigurationByVdmsId("v1")).thenReturn(expected);

        MaximoConfigurationDTO result = maximoService.getMaximoConfigurationByVdmsId("v1");

        assertThat(result).isEqualTo(expected);
    }

    // ── getMaximoWorkOrders ───────────────────────────────────────────────

    @Test
    void getMaximoWorkOrders_returnsMappedDtosWhenMemberPresent() {
        when(maximoUtils.getServerUrl(any())).thenReturn("https://maximo/oslc");
        when(maximoConfigurationRepository.getMaximoConfigByVdmsId("v1")).thenReturn(sampleDtoWithSecrets());
        when(maximoApiClient.generateMaximoAccessToken(any())).thenReturn("token");

        JSONObject response = new JSONObject();
        com.alibaba.fastjson.JSONArray members = new com.alibaba.fastjson.JSONArray();
        JSONObject wo = new JSONObject();
        wo.put("wonum", "WO-001");
        wo.put("description", "Test work order");
        members.add(wo);
        response.put("member", members);
        when(maximoApiClient.getAllWorkorders(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        List<MaximoDTO> result = maximoService.getMaximoWorkOrders("v1", "all", 1, 10, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWonum()).isEqualTo("WO-001");
    }

    @Test
    void getMaximoWorkOrders_returnsEmptyListWhenNoMemberKey() {
        when(maximoUtils.getServerUrl(any())).thenReturn("https://maximo/oslc");
        when(maximoConfigurationRepository.getMaximoConfigByVdmsId("v1")).thenReturn(sampleDtoWithSecrets());
        when(maximoApiClient.generateMaximoAccessToken(any())).thenReturn("token");
        when(maximoApiClient.getAllWorkorders(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new JSONObject());

        assertThat(maximoService.getMaximoWorkOrders("v1", "all", 1, 10, null)).isEmpty();
    }

    @Test
    void getMaximoWorkOrders_returnsEmptyListWhenApiReturnsNull() {
        when(maximoUtils.getServerUrl(any())).thenReturn("https://maximo/oslc");
        when(maximoConfigurationRepository.getMaximoConfigByVdmsId("v1")).thenReturn(sampleDtoWithSecrets());
        when(maximoApiClient.generateMaximoAccessToken(any())).thenReturn("token");
        when(maximoApiClient.getAllWorkorders(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(null);

        assertThat(maximoService.getMaximoWorkOrders("v1", "all", 1, 10, null)).isEmpty();
    }

    // ── getMaximoWorkOrderId ──────────────────────────────────────────────

    @Test
    void getMaximoWorkOrderId_returnsWonumList() {
        when(maximoUtils.getServerUrl(any())).thenReturn("https://maximo/oslc");
        when(maximoConfigurationRepository.getMaximoConfigByVdmsId("v1")).thenReturn(sampleDtoWithSecrets());
        when(maximoApiClient.generateMaximoAccessToken(any())).thenReturn("token");

        JSONObject response = new JSONObject();
        com.alibaba.fastjson.JSONArray members = new com.alibaba.fastjson.JSONArray();
        JSONObject wo1 = new JSONObject(); wo1.put("wonum", "WO-001");
        JSONObject wo2 = new JSONObject(); wo2.put("wonum", "WO-002");
        members.add(wo1); members.add(wo2);
        response.put("member", members);
        when(maximoApiClient.getAllWorkorders(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        List<String> result = maximoService.getMaximoWorkOrderId("v1", "all", 1, 10, null);

        assertThat(result).containsExactlyInAnyOrder("WO-001", "WO-002");
    }

    @Test
    void getMaximoWorkOrderId_retriesOnceWith401() {
        when(maximoConfigurationRepository.getMaximoConfigByVdmsId("v1")).thenReturn(sampleDtoWithSecrets());
        when(maximoApiClient.generateMaximoAccessToken(any())).thenReturn("token");

        JSONObject second = new JSONObject();
        second.put("member", new com.alibaba.fastjson.JSONArray());
        when(maximoApiClient.getAllWorkorders(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new MaximoException("Token Expired", 401, "x"))
                .thenReturn(second);

        List<String> result = maximoService.getMaximoWorkOrderId("v1", "all", 1, 10, null);

        assertThat(result).isEmpty();
        verify(maximoApiClient, times(2)).getAllWorkorders(any(), any(), any(), any(), anyInt(), anyInt());
    }

    // ── checkConfigurationStatus ──────────────────────────────────────────

    @Test
    void checkConfigurationStatus_backfillsCredentialsFromRepoWhenNull() {
        MaximoConfigurationDTO incoming = new MaximoConfigurationDTO(
                "cfg-1", "name", "srv", "auth", null, null, "[]");
        when(maximoConfigurationRepository.getMaximoConfigByVdmsId("v1")).thenReturn(sampleDtoWithSecrets());
        when(maximoApiClient.generateMaximoAccessToken(any())).thenReturn("");

        maximoService.checkConfigurationStatus("user1", "v1", incoming);

        assertThat(incoming.getClientId()).isEqualTo("client-id");
        assertThat(incoming.getClientSecret()).isEqualTo("client-secret");
    }

    @Test
    void checkConfigurationStatus_returnsSuccessWhenProbeReturns200() {
        when(maximoConfigurationRepository.getMaximoConfigByVdmsId("v1")).thenReturn(sampleDtoWithSecrets());
        when(maximoApiClient.generateMaximoAccessToken(any())).thenReturn("valid-token");
        JSONObject probe = new JSONObject();
        probe.put("status_code", 200);
        when(maximoApiClient.getAllWorkorders(any(), any(), isNull(), isNull(), eq(1), eq(1)))
                .thenReturn(probe);

        String result = maximoService.checkConfigurationStatus("user1", "v1", sampleDtoWithSecrets());

        assertThat(result).isEqualTo("success");
    }

    @Test
    void checkConfigurationStatus_returnsFailureWhenTokenIsEmpty() {
        when(maximoApiClient.generateMaximoAccessToken(any())).thenReturn("");

        String result = maximoService.checkConfigurationStatus("user1", "v1", sampleDtoWithSecrets());

        assertThat(result).isEqualTo("failure");
    }

    // ── getMaximoSites ────────────────────────────────────────────────────

    @Test
    void getMaximoSites_delegatesToUtils() {
        JSONArray sites = new JSONArray();
        when(maximoUtils.getSites()).thenReturn(sites);

        assertThat(maximoService.getMaximoSites()).isSameAs(sites);
    }

    // ── Audit fire-and-forget happens even when repository throws ────────

    @Test
    void deleteMaximoConfiguration_emitsFailedAuditAndRethrowsWhenRepositoryThrows() {
        org.mockito.Mockito.doThrow(new RuntimeException("db error"))
                .when(maximoConfigurationRepository).deleteById("cfg-x");

        assertThatThrownBy(() -> maximoService.deleteMaximoConfiguration("user1", "cfg-x", "v1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db error");

        verify(userActionLogClient, atLeastOnce()).addUserAction(
                eq("user1"), eq("maximo"), eq("DELETE"), anyString(), eq("failed"),
                eq("maximo_configuration"), eq("cfg-x"), eq("v1"));
    }
}
