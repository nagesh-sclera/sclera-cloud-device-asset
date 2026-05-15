package io.sclera.service;

import io.sclera.dto.PmsAttributesDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C3 */
@Service
public class PmsService {
    public Set<String> getLocationIdsByRoomStatus(String vdmsId, String status) {
        StubLog.warn("PmsService", "getLocationIdsByRoomStatus", "AP-C3");
        return Collections.emptySet();
    }
    public Set<PmsAttributesDTO> getPmsAttributesByLocationIds(Set<String> locationIds) {
        StubLog.warn("PmsService", "getPmsAttributesByLocationIds", "AP-C3");
        return Collections.emptySet();
    }
    public void updatePmsAttributesByLocationId(String locationId) {
        StubLog.warn("PmsService", "updatePmsAttributesByLocationId", "AP-C3");
    }
}
