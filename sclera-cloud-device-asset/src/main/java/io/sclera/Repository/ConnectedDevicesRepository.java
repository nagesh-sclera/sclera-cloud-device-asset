package io.sclera.Repository;

import io.sclera.dto.ConnectedDevicesDTO;
import io.sclera.dto.PowerSourceConnectionsDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: Phase 2 will implement with real JPA queries */
@Component
@Repository
public class ConnectedDevicesRepository {

    public void addConnectedDevices(String deviceId, String connectedDeviceId, String type) {
        StubLog.warn("ConnectedDevicesRepository", "addConnectedDevices", "local-db");
    }
    public List<ConnectedDevicesDTO> getConnectedDevicesSpecifications(String deviceId, Integer page, Integer size) {
        StubLog.warn("ConnectedDevicesRepository", "getConnectedDevicesSpecifications", "local-db");
        return Collections.emptyList();
    }
    public List<ConnectedDevicesDTO> getConnectedSpecificationsByDeviceId(String deviceId) {
        StubLog.warn("ConnectedDevicesRepository", "getConnectedSpecificationsByDeviceId", "local-db");
        return Collections.emptyList();
    }
    public List<ConnectedDevicesDTO> getAllInputConnectedSpecifications(String deviceId) {
        StubLog.warn("ConnectedDevicesRepository", "getAllInputConnectedSpecifications", "local-db");
        return Collections.emptyList();
    }
    public List<ConnectedDevicesDTO> getAllOutputConnectedSpecifications(String deviceId) {
        StubLog.warn("ConnectedDevicesRepository", "getAllOutputConnectedSpecifications", "local-db");
        return Collections.emptyList();
    }
    public void untagPowerSource(String specificationsId, String connectedSpecificationsId) {
        StubLog.warn("ConnectedDevicesRepository", "untagPowerSource", "local-db");
    }
    public void untagDevice(String specificationsId, String connectedSpecificationsId) {
        StubLog.warn("ConnectedDevicesRepository", "untagDevice", "local-db");
    }
    public void untagPowerSourceByDeviceId(String deviceId) {
        StubLog.warn("ConnectedDevicesRepository", "untagPowerSourceByDeviceId", "local-db");
    }
    public List<PowerSourceConnectionsDTO> getPowerSourceTopologyForDevice(Set<String> deviceIds) {
        StubLog.warn("ConnectedDevicesRepository", "getPowerSourceTopologyForDevice", "local-db");
        return Collections.emptyList();
    }
    public List<ConnectedDevicesDTO> getAllConnectedDevicesForLoadCalculation(String specificationId) {
        StubLog.warn("ConnectedDevicesRepository", "getAllConnectedDevicesForLoadCalculation", "local-db");
        return Collections.emptyList();
    }
    public Integer getPowerSourceTopologyConnectionsCount() {
        StubLog.warn("ConnectedDevicesRepository", "getPowerSourceTopologyConnectionsCount", "local-db");
        return 0;
    }
    public List<PowerSourceConnectionsDTO> getPowerSourceTopologyByPagination(Integer pageSize, Integer offset) {
        StubLog.warn("ConnectedDevicesRepository", "getPowerSourceTopologyByPagination", "local-db");
        return Collections.emptyList();
    }
    public void deleteConnectedDevicesBySpecificationId(String specificationsId) {
        StubLog.warn("ConnectedDevicesRepository", "deleteConnectedDevicesBySpecificationId", "local-db");
    }
}
