package io.sclera.service.impl;
import io.sclera.service.*;

import io.sclera.dto.InterfaceDTO;
import io.sclera.service.InterfaceService;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

/** STUB: replace with remote call to AP-C3 */
@Service
public class InterfaceServiceImpl implements InterfaceService {
    /**
     * Returns the number of interfaces for the given device. Stub returns zero.
     */
    public Integer getInterfaceCountByDevice(String deviceId) { return 0; }
    /**
     * Lists the SNMP interfaces for a device. Stub returns an empty list.
     */
    public List<InterfaceDTO> listDeviceSnmpInterfaceByDeviceId(String a, String b, String c, String d) { return Collections.emptyList(); }
    /**
     * Deletes all interfaces associated with the given device. Stub is a no-op.
     */
    public void deleteInterfaceByDeviceId(String deviceId) {}
    /**
     * Reassigns interfaces from one device id to another. Stub is a no-op.
     */
    public void updateInterfaceDeviceId(String oldId, String newId) {}
}
