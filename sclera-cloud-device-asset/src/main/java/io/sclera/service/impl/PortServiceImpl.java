package io.sclera.service.impl;
import io.sclera.service.*;

import io.sclera.dto.Product_PortsDTO;
import io.sclera.service.PortService;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class PortServiceImpl implements PortService {
    /**
     * Returns the ports for the given device. Stub returns an empty set.
     */
    public Set<Product_PortsDTO> getPortsByDeviceId(String deviceId) { return Collections.emptySet(); }
    /**
     * Returns the status of a specific port on a device. Stub returns null.
     */
    public Integer getDeviceportStatus(String vdmsId, String dockerName, String ipAddress, String port) { return null; }
    /**
     * Updates the status of a port. Stub is a no-op.
     */
    public void updatePortStatusById(Product_PortsDTO dto) {}
    /**
     * Deletes the global ports for the given device. Stub is a no-op.
     */
    public void deleteGlobalPortByDeviceId(String deviceId) {}
    /**
     * Inserts or updates the global ports for the given device. Stub is a no-op.
     */
    public void upsertGlobalPortByDeviceId(Set<Product_PortsDTO> ports, String deviceId) {}
}
