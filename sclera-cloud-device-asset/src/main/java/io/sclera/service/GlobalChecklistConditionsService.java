package io.sclera.service;

import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

import java.util.Set;

/** STUB: replace with remote call to future microservice */
@Service
public class GlobalChecklistConditionsService {

    public void updateGlobalChecklistConditionsDeviceAndIsRemoved(Set<String> ids) {
        StubLog.warn("GlobalChecklistConditionsService", "updateGlobalChecklistConditionsDeviceAndIsRemoved", "AP-C4");
    }

    public void updateGlobalChecklistConditionsLocationAndIsRemoved(Set<String> locationIds) {
        StubLog.warn("GlobalChecklistConditionsService", "updateGlobalChecklistConditionsLocationAndIsRemoved", "AP-C4");
    }
}
