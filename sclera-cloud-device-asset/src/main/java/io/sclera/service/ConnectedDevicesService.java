package io.sclera.service;

import io.sclera.dto.*;

import java.util.List;
import java.util.Set;

/** Service contract for {@link io.sclera.service.ConnectedDevicesService}. */
public interface ConnectedDevicesService {

    void addConnectedDevices(ConnectedDevicesDTO connectedDevicesDTO);

    List<DeviceDTO> getConnectedDevicesSpecifications(String specification_id, Integer pagesize, Integer offset);

    String getPowerUnit(String device_id, String key_name);

    List<DeviceDTO> getTaggedPowerSourcesByDeviceId(String username, String vdmsid, String device_id);

    List<SpecificationsDTO> mappingConnectedDevicesToSpecifications(List<ConnectedDevicesDTO> connectedDevicesDTO);

    List<ConnectedDevicesDTO> getConnectedSpecifications(String device_id);

    List<ConnectedDevicesDTO> getAllInputConnectedSpecifications(String device_id);

    List<ConnectedDevicesDTO> getAllOutputConnectedSpecifications(String device_id);

    SpecificationsDTO getSpaceNameByDeviceId(ConnectedDevicesDTO specificationsDTO);

    String getPortPattern(String key_name);

    void untagPowerSource(String specifications_id, String connected_specifications_id);

    void untagDevice(String specifications_id, String connected_specifications_id);

    void untagPowerSourceByDeviceId(String device_id);

    List<DeviceDTO> getDeviceSpecificationsByDevices(List<DeviceDTO> devices);

    List<PowerSourceConnectionsDTO> getPowerSourceTopologyForDevice(Set<String> device_ids);

    List<ConnectedDevicesDTO> getAllConnectedDevicesForLoadCalculation(String specification_id);

    Integer getPowerSourceTopologyConnectionsCount();

    PowerSourceTopologyDTO getPowerSourceTopologyByPagination(Integer pageno, Integer pagesize);

    List<LoadCalculationDTO> calculateLoadForTopology(List<PowerSourceConnectionsDTO> connections);

    List<PowerSourceConnectionsDTO> getPowerSourceTopologyPagination(Integer pageno, Integer pagesize);

    void deleteConnectedDevicesBySpecificationId(String specifications_id);
}
