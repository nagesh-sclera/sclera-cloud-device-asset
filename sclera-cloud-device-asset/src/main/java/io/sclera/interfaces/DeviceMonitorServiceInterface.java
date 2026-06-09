package io.sclera.interfaces;

import java.util.List;

/** Service contract for the matching service class. */
public interface DeviceMonitorServiceInterface {
    List<String> getUniqueAssignedUserEmail(String vdmsId, String networkName);
}
