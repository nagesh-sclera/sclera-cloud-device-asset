package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.InventoryDeviceSyncDTO;
import io.sclera.utils.StubLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to future microservice */
@Service
public class InventoryDeviceService {
    private static final Logger log = LoggerFactory.getLogger(InventoryDeviceService.class);

    public void retireInventoryDevice(String vdmsId, String deviceId, String username, String description, String inventoryTrackingId) {
        log.warn("STUB: retireInventoryDevice called");
    }
    public Set<DeviceDTO> upsertInventoryDevices(JSONObject stockedOutItems, String vdmsId, String email, InventoryDeviceSyncDTO dto) {
        StubLog.warn("InventoryDeviceService", "upsertInventoryDevices", "inventory-microservice");
        return Collections.emptySet();
    }
}
