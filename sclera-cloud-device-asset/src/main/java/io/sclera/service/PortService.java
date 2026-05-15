package io.sclera.service;

import io.sclera.dto.Product_PortsDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class PortService {
    public Set<Product_PortsDTO> getPortsByDeviceId(String deviceId) { return Collections.emptySet(); }
    public Integer getDeviceportStatus(String vdmsId, String dockerName, String ipAddress, String port) { return null; }
    public void updatePortStatusById(Product_PortsDTO dto) {}
    public void deleteGlobalPortByDeviceId(String deviceId) {}
    public void upsertGlobalPortByDeviceId(Set<Product_PortsDTO> ports, String deviceId) {}
}
