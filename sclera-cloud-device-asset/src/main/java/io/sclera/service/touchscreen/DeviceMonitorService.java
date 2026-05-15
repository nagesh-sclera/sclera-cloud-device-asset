package io.sclera.service.touchscreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/** STUB: edge-only touchscreen device monitor */
@Service
public class DeviceMonitorService {
    private static final Logger log = LoggerFactory.getLogger(DeviceMonitorService.class);

    public List<String> getUniqueAssignedUserEmail(String vdmsId, String networkName) {
        log.warn("STUB: getUniqueAssignedUserEmail called");
        return Collections.emptyList();
    }
}
