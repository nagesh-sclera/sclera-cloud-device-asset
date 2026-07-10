package io.sclera.controller.internal;

import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.SensorAlertDTO;
import io.sclera.service.MeasuringInstrumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal (Dapr) conditions-read routes for the measuring-instrument service: alert messages by
 * a set of device ids, and alert details by measuring-instrument id. Both delegate to the
 * device-asset-owned {@code MeasuringInstrumentService} conditions readers (no tenant needed).
 */
@RestController
public class MiConditionsBridgeController {

    private static final Logger log = LoggerFactory.getLogger(MiConditionsBridgeController.class);

    private final MeasuringInstrumentService measuringInstrumentService;

    public MiConditionsBridgeController(MeasuringInstrumentService measuringInstrumentService) {
        this.measuringInstrumentService = measuringInstrumentService;
    }

    @PostMapping("/api/v1/device-asset-service/internal/conditions/alertmessages")
    public List<ConditionsDTO> alertMessagesByDeviceIds(@RequestBody List<String> deviceIds) {
        log.info("internal alertMessagesByDeviceIds count={}", deviceIds == null ? 0 : deviceIds.size());
        return measuringInstrumentService.listMeasuringIntrumentDevicesAlertMessagesByDeviceIds(deviceIds);
    }

    @GetMapping("/api/v1/device-asset-service/internal/conditions/alertdetails/{measuringInstrumentId}")
    public SensorAlertDTO alertDetailsByMiId(@PathVariable String measuringInstrumentId) {
        log.info("internal alertDetailsByMiId measuringInstrumentId={}", measuringInstrumentId);
        return measuringInstrumentService.getMeasuringInstrumentAlertDetails(measuringInstrumentId);
    }
}
