package io.sclera.service.impl.touchscreen;
import io.sclera.service.*;

import io.sclera.service.DeviceMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/** STUB: edge-only touchscreen device monitor */
@Service
public class DeviceMonitorServiceImpl implements DeviceMonitorService {
    private static final Logger log = LoggerFactory.getLogger(DeviceMonitorServiceImpl.class);

    /**
     * Stub for retrieving the unique emails of users assigned within a VDMS network; always returns an empty list.
     */
    public List<String> getUniqueAssignedUserEmail(String vdmsId, String networkName) {
        log.warn("STUB: getUniqueAssignedUserEmail called");
        return Collections.emptyList();
    }
}
