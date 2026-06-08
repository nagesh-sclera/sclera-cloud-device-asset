package io.sclera.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Executes device-asset-owned scheduler jobs. Each method mirrors the monolith
 *  Schedular's same-named @Scheduled method; absent dependencies stub-with-WARN. */
@Component
public class DeviceAssetJobHandlers {

    private static final Logger log = LoggerFactory.getLogger(DeviceAssetJobHandlers.class);

    // Constructor-inject the device-asset services each handler needs (e.g. DeviceService,
    // UserActionLogService, ...). Added as bodies are wired.

    public void historyRecord()            { stub("historyRecord"); }
    public void unlinkVendorOrganisation() { stub("unlinkVendorOrganisation"); }
    public void internetBandwidthCheck()   { stub("internetBandwidthCheck"); }
    public void vdmsSystemHealth()         { stub("vdmsSystemHealth"); }
    public void connectedStatusForIOC()    { stub("connectedStatusForIOC"); }
    public void qrcodeNfcBarcodeSync()     { stub("qrcodeNfcBarcodeSync"); }
    public void syncAssetCountToCloud()    { stub("syncAssetCountToCloud"); }
    public void userActionLog()            { stub("userActionLog"); }
    public void deviceDndEnable()          { stub("deviceDndEnable"); }
    public void offlineDeviceCheck()       { stub("offlineDeviceCheck"); }

    private void stub(String job) {
        log.warn("scheduler job '{}' not yet wired to device-asset logic (stub-with-WARN)", job);
    }
}
