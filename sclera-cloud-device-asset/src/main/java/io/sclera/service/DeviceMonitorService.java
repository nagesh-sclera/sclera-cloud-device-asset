package io.sclera.service;

import java.util.List;

/** Service contract for the matching service class. */
public interface DeviceMonitorService {
    List<String> getUniqueAssignedUserEmail(String vdmsId, String networkName);
}
