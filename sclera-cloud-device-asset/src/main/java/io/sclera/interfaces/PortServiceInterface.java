package io.sclera.interfaces;

import io.sclera.dto.Product_PortsDTO;
import java.util.Set;

/** Service contract for {@link io.sclera.service.PortService}. */
public interface PortServiceInterface {
    Set<Product_PortsDTO> getPortsByDeviceId(String deviceId);

    Integer getDeviceportStatus(String vdmsId, String dockerName, String ipAddress, String port);

    void updatePortStatusById(Product_PortsDTO dto);

    void deleteGlobalPortByDeviceId(String deviceId);

    void upsertGlobalPortByDeviceId(Set<Product_PortsDTO> ports, String deviceId);
}
