package io.sclera.scheduler;

import io.sclera.service.DeviceService;
import io.sclera.service.DeviceSpecificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Executes device-asset-owned scheduler jobs. Each method mirrors the monolith
 * Schedular's same-named {@code @Scheduled} method; where the underlying logic was
 * not extracted into this service the method is left as stub-with-WARN and documented
 * in migration-notes/scheduler-handler-wiring.md.
 */
@Component
public class DeviceAssetJobHandlers {

    private static final Logger log = LoggerFactory.getLogger(DeviceAssetJobHandlers.class);

    // --- wired services ---
    private final DeviceService deviceService;
    private final DeviceSpecificationService deviceSpecificationService;

    public DeviceAssetJobHandlers(DeviceService deviceService,
                                  DeviceSpecificationService deviceSpecificationService) {
        this.deviceService = deviceService;
        this.deviceSpecificationService = deviceSpecificationService;
    }

    // -------------------------------------------------------------------------
    // STUBBED — see migration-notes/scheduler-handler-wiring.md for follow-ups
    // -------------------------------------------------------------------------

    /** Mirrors {@code SchedularService#scheduleHistoryRecord}.
     *  STUB: requires HistoryService.deleteHistoryRecords() and
     *  InspectionRecordHistoryService (both live in sclera-audit, not here). */
    public void historyRecord()            { stub("historyRecord"); }

    /** Mirrors {@code SchedularService#scheduleUnlinkVendorOrganisation}.
     *  STUB: requires VendorOrganisationService.deleteUnlinkedVendorOrganisations()
     *  which was not extracted into this service. */
    public void unlinkVendorOrganisation() { stub("unlinkVendorOrganisation"); }

    /** Mirrors {@code SchedularService#scheduleInternetBandwidthCheck}.
     *  STUB: requires DockerService.getDockerInternetRequired() /
     *  DockerService.getNetworkSpeed() — the local DockerService is a stub
     *  and these methods are absent from it. */
    public void internetBandwidthCheck()   { stub("internetBandwidthCheck"); }

    /** Mirrors {@code Schedular#scheduleVdmsSystemHealth}, now per-VDMS. The scheduler fires
     *  this once per VDMS; vdmsId identifies which site to process.
     *  STUB: LorawanService, vdmsService.AddSystemHealthAsResponse(), and
     *  vdmsService.sendVDMSData() were not extracted into this service. */
    public void vdmsSystemHealth(String vdmsId) { stub("vdmsSystemHealth[" + vdmsId + "]"); }

    /** Mirrors {@code Schedular#scheduleConnectedStatusForIOC} (scheduleConnectedStatusForIOC
     *  + scheduleUserActivityData).
     *  STUB: requires IOCService.sendAllOnlineConnectionData(), DaintreeService,
     *  SiemensService, PortUtilityService, RabbitmqService.rabbitMqUserActivityData() —
     *  none of which are in this service. */
    public void connectedStatusForIOC()    { stub("connectedStatusForIOC"); }

    /** Mirrors {@code SchedularService#scheduleQrcodeNFCBarcodeSync}.
     *  STUB: requires essentialService.syncQrCodeNfcBarCode() which is fully
     *  commented-out in this service's EssentialService. */
    // QR is owned locally by device-asset; no cloud QR sync. Intentionally a no-op.
    public void qrcodeNfcBarcodeSync()     { stub("qrcodeNfcBarcodeSync"); }

    /** Mirrors {@code SchedularService#syncAssetCountToCloud}.
     *  STUB: DeviceService.getAssetCount() exists in this service, but
     *  APICallClient.updateVdmsAssetCountCloud() was not added to APICallClient —
     *  there is no cloud-push path available here yet. */
    public void syncAssetCountToCloud()    { stub("syncAssetCountToCloud"); }

    /** Mirrors the monolith's user-activity-data scheduled method
     *  (scheduleHistoryRecord calls userActionLogService.deleteUserActionLogRecords();
     *   scheduleConnectedStatusForIOC calls scheduleUserActivityData).
     *  STUB: this service's UserActionLogService only publishes events and does not
     *  expose deleteUserActionLogRecords() or getUserActivityRecords(); RabbitmqService
     *  is absent. */
    public void userActionLog()            { stub("userActionLog"); }

    // -------------------------------------------------------------------------
    // WIRED
    // -------------------------------------------------------------------------

    /**
     * Mirrors the monolith's DND-expiry scheduled logic (lines 2811-2876 in DeviceService).
     * Calls {@link DeviceService#dndCheckAndUpdate()} which iterates DND-enabled devices and
     * disables DND after 24 hours.
     */
    public void deviceDndEnable() {
        log.info("[scheduler] deviceDndEnable: running DND expiry check");
        deviceService.dndCheckAndUpdate();
    }

    /**
     * Mirrors the monolith's offline-device-check scheduled logic.
     * Calls {@link DeviceSpecificationService#updateDeviceStatusToOffline()} which marks all
     * stale online devices (spec not updated within 90 s) as offline.
     */
    public void offlineDeviceCheck() {
        log.info("[scheduler] offlineDeviceCheck: marking stale online devices offline");
        deviceSpecificationService.updateDeviceStatusToOffline();
    }

    // -------------------------------------------------------------------------

    private void stub(String job) {
        log.warn("scheduler job '{}' not yet wired to device-asset logic (stub-with-WARN)", job);
    }
}
