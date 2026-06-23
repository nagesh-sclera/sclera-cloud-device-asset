package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.DeviceInstalledAppsRepository;
import io.sclera.Repository.DeviceNetworkSpecificationRepository;
import io.sclera.Repository.DeviceRepository;
import io.sclera.Repository.DeviceSpecificationRepository;
import io.sclera.Repository.UserRepository;
import io.sclera.client.APICallClient;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for DeviceSpecificationService.saveFullJson - the full agent payload ingestion that the
 * main test deferred. Covers the null/invalid-payload guards and the device-not-found path
 * (forwards to inventory and persists the device + network specifications).
 */
@ExtendWith(MockitoExtension.class)
class DeviceSpecificationServiceSaveJsonTest {

    @Mock DeviceRepository deviceRepository;
    @Mock DeviceSpecificationRepository deviceSpecificationRepository;
    @Mock DeviceInstalledAppsRepository deviceInstalledAppsRepository;
    @Mock DeviceNetworkSpecificationRepository deviceNetworkSpecificationRepository;
    @Mock APICallClient apiCallService;
    @Mock UserRepository userRepository;
    @Mock ManagedSoftwareService managedSoftwareService;

    @InjectMocks DeviceSpecificationService service;

    @Test
    void saveFullJson_nullPayload_returnsNull() {
        assertThat(service.saveFullJson(null, mock(HttpServletRequest.class), "assignee")).isNull();
    }

    @Test
    void saveFullJson_missingSystemInfo_returnsNull() {
        JSONObject json = new JSONObject();
        json.put("id", "SN1"); // no systemInfo
        assertThat(service.saveFullJson(json, mock(HttpServletRequest.class), "assignee")).isNull();
    }

    @Test
    void saveFullJson_deviceNotFound_forwardsToInventoryAndPersistsSpecs() {
        JSONObject json = new JSONObject();
        json.put("id", "SN1");
        json.put("systemInfo", new JSONObject());
        when(deviceRepository.findDeviceIdBySerialNumber("SN1")).thenReturn(null);
        when(userRepository.getMasterUserEmail()).thenReturn("admin@acme.com");
        when(deviceSpecificationRepository.findById("SN1")).thenReturn(Optional.empty());

        String result = service.saveFullJson(json, mock(HttpServletRequest.class), "assignee");

        assertThat(result).isNull(); // no local device id resolved
        verify(apiCallService).sendAgentDataToInventory(any());
        verify(deviceSpecificationRepository).save(any());
        verify(deviceNetworkSpecificationRepository).save(any());
        verify(deviceInstalledAppsRepository).saveAll(any());
    }
}
