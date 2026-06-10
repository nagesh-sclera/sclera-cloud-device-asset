package io.sclera.controller.admin;

import io.sclera.Repository.DeviceOnboardStatusRepository;
import io.sclera.Repository.DeviceRepository;
import io.sclera.service.UserActionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

/**
 * Simple onboard toggle for the POC. Sets device.onboard_status (3 = onboarded)
 * and, when onboarding, marks the image/tag/field/geolocation sub-statuses so the
 * Onboarded-Details filters have something to match. Uses existing repo methods.
 */
@RestController
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class DeviceOnboardActionController {

    private static final Logger log = LoggerFactory.getLogger(DeviceOnboardActionController.class);
    private final DeviceRepository deviceRepository;
    private final DeviceOnboardStatusRepository onboardRepo;
    private final UserActionLogService userActionLogService;

    public DeviceOnboardActionController(DeviceRepository deviceRepository,
                                         DeviceOnboardStatusRepository onboardRepo,
                                         UserActionLogService userActionLogService) {
        this.deviceRepository = deviceRepository;
        this.onboardRepo = onboardRepo;
        this.userActionLogService = userActionLogService;
    }

    @PostMapping("/docker/{dockername}/device/{device_id}/onboard")
    @Transactional
    public void onboard(@PathVariable String dockername,
                        @PathVariable("device_id") String deviceId,
                        @RequestParam(required = false) String username,
                        @RequestParam(required = false) String vdmsid,
                        @RequestParam(defaultValue = "3") Integer status) {
        log.info("onboard device_id={} status={}", deviceId, status);
        deviceRepository.updateOnboardAssetStatus(Set.of(deviceId), status,
                BigInteger.valueOf(System.currentTimeMillis()));
        if (status != null && status == 3) {
            String existing = onboardRepo.getOnboardAssetIdByDeviceId(deviceId);
            if (existing == null) {
                onboardRepo.addOnboardAsset(UUID.randomUUID().toString(), deviceId, username, 1, 1, 1, 1);
            } else {
                onboardRepo.updateAssetOnboardData(deviceId, 1, 1, 1, 1);
            }
        }
        String msg = (status != null && status == 3)
                ? "Device " + deviceId + " was onboarded"
                : "Device " + deviceId + " was set to not onboarded";
        userActionLogService.addUserAction(username, "asset", "UPDATE", msg, "success", "onboard", deviceId);
    }
}
