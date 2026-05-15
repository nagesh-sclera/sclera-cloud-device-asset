package io.sclera.service;

import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class GlobalQrcodeService {
    public Integer getDeviceQrcodeCountByDeviceId(String deviceId) { return 0; }

    public void deleteGlobalQRCodeByLocationId(String locationId) {
        StubLog.warn("GlobalQrcodeService", "deleteGlobalQRCodeByLocationId", "AP-C1edge");
    }
}
