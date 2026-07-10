package io.sclera.controller.internal;

import io.sclera.dto.DeviceDTO;
import io.sclera.service.DeviceService;
import io.sclera.service.LocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MiDeviceBridgeController.class)
class MiDeviceBridgeControllerTest {

    @Autowired MockMvc mvc;
    @MockBean DeviceService deviceService;
    @MockBean LocationService locationService;

    @Test
    void getByIdReturnsDeviceFromService() throws Exception {
        DeviceDTO d = new DeviceDTO();
        d.setId("dev-1");
        d.setDisplay_name("Boiler 3");
        when(deviceService.getDeviceById(eq("dev-1"))).thenReturn(d);

        mvc.perform(get("/api/v1/device-asset-service/internal/device/dev-1/getbyid"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value("dev-1"))
           .andExpect(jsonPath("$.display_name").value("Boiler 3"));
    }

    @Test
    void measureCountUpdatesEachDeviceId() throws Exception {
        mvc.perform(post("/api/v1/device-asset-service/internal/devices/measurecount")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"dev-1\",\"dev-2\"]"))
           .andExpect(status().isNoContent());
        verify(deviceService).updateDeviceMeasureCountByDeviceId("dev-1");
        verify(deviceService).updateDeviceMeasureCountByDeviceId("dev-2");
    }

    @Test
    void measuringInstrumentStatusUpdatesDevice() throws Exception {
        mvc.perform(post("/api/v1/device-asset-service/internal/device/dev-1/measuringinstrumentstatus"))
           .andExpect(status().isNoContent());
        verify(deviceService).updateDeviceMeasuringInstrumentStatusByDeviceId("dev-1");
    }

    @Test
    void deviceIdsByFloorResolvesLocationsThenDevices() throws Exception {
        when(locationService.getLocationIdsByFilter(isNull(), isNull(), isNull(), isNull(),
                eq(List.of("floor-1")), isNull())).thenReturn(List.of("loc-1", "loc-2"));
        when(deviceService.getDeviceIdsByFilter(isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(List.of("loc-1", "loc-2")))).thenReturn(List.of("dev-1"));

        mvc.perform(get("/api/v1/device-asset-service/internal/devices/ids/byfloor/floor-1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0]").value("dev-1"));
    }

    @Test
    void deviceIdsByLocationFiltersByLocationId() throws Exception {
        when(deviceService.getDeviceIdsByFilter(isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(List.of("loc-9")))).thenReturn(List.of("dev-7"));
        mvc.perform(get("/api/v1/device-asset-service/internal/devices/ids/bylocation/loc-9"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0]").value("dev-7"));
    }

    @Test
    void deviceIdsBySearchPassesSearchKey() throws Exception {
        when(deviceService.getDeviceIdsByFilter(isNull(), isNull(), isNull(), eq("boiler"), isNull(),
                isNull(), isNull(), isNull())).thenReturn(List.of("dev-3"));
        mvc.perform(get("/api/v1/device-asset-service/internal/devices/ids/bysearch").param("searchKey", "boiler"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0]").value("dev-3"));
    }

    @Test
    void monitoredReturnsSubset() throws Exception {
        when(deviceService.getMonitoredDeviceIds(eq(Set.of("dev-1","dev-2"))))
            .thenReturn(Set.of("dev-1"));
        mvc.perform(post("/api/v1/device-asset-service/internal/devices/monitored")
                .contentType(MediaType.APPLICATION_JSON).content("[\"dev-1\",\"dev-2\"]"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0]").value("dev-1"));
    }
}
