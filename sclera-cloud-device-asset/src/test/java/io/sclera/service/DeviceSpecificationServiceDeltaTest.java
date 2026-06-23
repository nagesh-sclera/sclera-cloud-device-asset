package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.DeviceNetworkSpecificationRepository;
import io.sclera.Repository.DeviceRepository;
import io.sclera.Repository.DeviceSpecificationRepository;
import io.sclera.models.Device;
import io.sclera.models.DeviceSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for DeviceSpecificationService.upsertDeltaJson - the incremental agent payload apply.
 * Covers the invalid-payload, device-not-found and spec-not-found guards, and the happy path that
 * updates the spec and returns "success".
 */
@ExtendWith(MockitoExtension.class)
class DeviceSpecificationServiceDeltaTest {

    @Mock DeviceRepository deviceRepository;
    @Mock DeviceSpecificationRepository deviceSpecificationRepository;
    @Mock DeviceNetworkSpecificationRepository deviceNetworkSpecificationRepository;

    @InjectMocks DeviceSpecificationService service;

    private JSONObject deltaPayload() {
        JSONObject json = new JSONObject();
        json.put("id", "SN1");
        json.put("vdmsId", "v1");
        json.put("eventType", "normal");
        json.put("systemInfo", new JSONObject());
        return json;
    }

    @Test
    void upsertDeltaJson_missingSystemInfo_returnsNull() {
        assertThat(service.upsertDeltaJson(new JSONObject())).isNull();
    }

    @Test
    void upsertDeltaJson_deviceNotFound_returnsNull() {
        when(deviceRepository.findDeviceIdBySerialNumber("SN1")).thenReturn(null);
        assertThat(service.upsertDeltaJson(deltaPayload())).isNull();
    }

    @Test
    void upsertDeltaJson_specNotFound_returnsNull() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn("d1");
        when(deviceRepository.findDeviceIdBySerialNumber("SN1")).thenReturn(device);
        when(deviceRepository.findById("d1")).thenReturn(Optional.empty()); // updateDeviceStatusToOnline no-op
        when(deviceSpecificationRepository.findById("SN1")).thenReturn(Optional.empty());

        assertThat(service.upsertDeltaJson(deltaPayload())).isNull();
    }

    @Test
    void upsertDeltaJson_happyPath_updatesSpecAndReturnsSuccess() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn("d1");
        when(deviceRepository.findDeviceIdBySerialNumber("SN1")).thenReturn(device);
        when(deviceRepository.findById("d1")).thenReturn(Optional.empty());
        DeviceSpecification spec = mock(DeviceSpecification.class);
        when(spec.getUsername()).thenReturn("user");
        when(deviceSpecificationRepository.findById("SN1")).thenReturn(Optional.of(spec));
        when(deviceNetworkSpecificationRepository.findById("SN1")).thenReturn(Optional.empty());
        // updateChildDevices: no child data -> returns early
        when(deviceSpecificationRepository.getChildDeviceByDeviceId("d1")).thenReturn(null);

        String result = service.upsertDeltaJson(deltaPayload());

        assertThat(result).isEqualTo("success");
        verify(spec).setUpdatedAt(org.mockito.ArgumentMatchers.anyLong());
        verify(deviceSpecificationRepository).save(spec);
    }
}
