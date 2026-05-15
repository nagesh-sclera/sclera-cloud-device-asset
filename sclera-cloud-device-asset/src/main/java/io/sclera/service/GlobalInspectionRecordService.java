package io.sclera.service;

import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C4 */
@Service
public class GlobalInspectionRecordService {

    public void updateGlobalInspectionRelationDeviceAndIsRemoved(Set<String> ids) {
        StubLog.warn("GlobalInspectionRecordService", "updateGlobalInspectionRelationDeviceAndIsRemoved", "AP-C4");
    }

    public void deleteGlobalInspectionRelationInBatch(List<String> ids) {
        StubLog.warn("GlobalInspectionRecordService", "deleteGlobalInspectionRelationInBatch", "AP-C4");
    }

    public void updateGlobalInspectionByDeviceId(String primaryDeviceId, String existingDeviceId) {
        StubLog.warn("GlobalInspectionRecordService", "updateGlobalInspectionByDeviceId", "AP-C4");
    }

    public void updateGlobalInspectionRelationLocationAndIsRemoved(Set<String> locationIds) {
        StubLog.warn("GlobalInspectionRecordService", "updateGlobalInspectionRelationLocationAndIsRemoved", "AP-C4");
    }
    public void updateGlobalInspectionRecord(String email) {
        StubLog.warn("GlobalInspectionRecordService", "updateGlobalInspectionRecord", "AP-C4");
    }
}
