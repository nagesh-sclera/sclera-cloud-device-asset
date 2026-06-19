package io.sclera.workorder.service;

import io.sclera.workorder.client.MaximoApiClient;
import io.sclera.workorder.client.UserActionLogClient;
import io.sclera.workorder.client.VdmsClient;
import io.sclera.workorder.dto.MaximoConfigurationDTO;
import io.sclera.workorder.repository.MaximoConfigurationRepository;
import io.sclera.workorder.service.impl.MaximoServiceImpl;
import io.sclera.workorder.util.MaximoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
class MaximoServiceCacheTest {

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean CacheManager cacheManager() { return new ConcurrentMapCacheManager("maximoConfig"); }
        @Bean MaximoConfigurationRepository repo() { return Mockito.mock(MaximoConfigurationRepository.class); }
        @Bean MaximoApiClient apiClient() { return Mockito.mock(MaximoApiClient.class); }
        @Bean MaximoUtils maximoUtils() { return Mockito.mock(MaximoUtils.class); }
        @Bean UserActionLogClient userActionLogClient() { return Mockito.mock(UserActionLogClient.class); }
        @Bean VdmsClient vdmsClient() { return Mockito.mock(VdmsClient.class); }
        @Bean MaximoService maximoService(MaximoConfigurationRepository r, MaximoApiClient a,
                                          MaximoUtils u, UserActionLogClient l, VdmsClient v) {
            return new MaximoServiceImpl(r, a, u, l, v);
        }
    }

    @Autowired MaximoService service;
    @Autowired MaximoConfigurationRepository repo;
    @Autowired CacheManager cacheManager;

    private MaximoConfigurationDTO cfg() {
        MaximoConfigurationDTO dto = new MaximoConfigurationDTO();
        dto.setId("cfg-1");
        return dto;
    }

    @BeforeEach
    void reset() {
        Mockito.reset(repo);
        cacheManager.getCacheNames().forEach(n -> cacheManager.getCache(n).clear());
    }

    @Test
    void getMaximoConfig_isCachedAfterFirstCall() {
        when(repo.getMaximoConfigurationByVdmsId("v1")).thenReturn(cfg());

        service.getMaximoConfigurationByVdmsId("v1");   // miss -> repo
        clearInvocations(repo);
        service.getMaximoConfigurationByVdmsId("v1");   // hit

        verify(repo, never()).getMaximoConfigurationByVdmsId("v1");
    }

    @Test
    void upsert_evictsCachedConfig() {
        when(repo.getMaximoConfigurationByVdmsId("v1")).thenReturn(cfg());

        service.getMaximoConfigurationByVdmsId("v1");   // populate
        service.upsertMaximoConfiguration("user", "v1", cfg());   // evicts maximoConfig[v1]
        clearInvocations(repo);
        service.getMaximoConfigurationByVdmsId("v1");   // miss -> repo

        verify(repo, times(1)).getMaximoConfigurationByVdmsId("v1");
    }

    @Test
    void delete_evictsCachedConfig() {
        when(repo.getMaximoConfigurationByVdmsId("v1")).thenReturn(cfg());

        service.getMaximoConfigurationByVdmsId("v1");   // populate
        service.deleteMaximoConfiguration("user", "cfg-1", "v1");   // evicts maximoConfig[v1]
        clearInvocations(repo);
        service.getMaximoConfigurationByVdmsId("v1");   // miss -> repo

        verify(repo, times(1)).getMaximoConfigurationByVdmsId("v1");
    }
}
