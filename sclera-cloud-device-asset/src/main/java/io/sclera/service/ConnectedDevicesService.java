
package io.sclera.service;
import io.sclera.client.APICallClient;
import io.sclera.interfaces.ConnectedDevicesServiceInterface;


import com.alibaba.fastjson.JSONObject;
import io.sclera.client.ConnectedDevicesClient;
import io.sclera.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages connections between devices and their power sources, and assembles
 * power-source topology and load-calculation views.
 *
 * <p>Collaborates with {@link ConnectedDevicesClient} for persistence and
 * retrieval of connection records, {@link DeviceService} for device details,
 * {@link SpecificationsService} for specification, power and load-calculation
 * data, and {@link APICallClient} for outbound API calls.
 */
@Service
public class ConnectedDevicesService implements ConnectedDevicesServiceInterface {

    @Autowired
    ConnectedDevicesClient connectedDevicesRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    SpecificationsService specificationsService;

    @Autowired
    APICallClient apiCallService;


    /**
     * Persists a new connected-device record linking a device specification to a connected specification.
     *
     * @param connectedDevicesDTO the connection details to persist
     */
    public void addConnectedDevices(ConnectedDevicesDTO connectedDevicesDTO) {

        connectedDevicesRepository.addConnectedDevices(connectedDevicesDTO.getId(), connectedDevicesDTO.getConnected_specifications_id(), connectedDevicesDTO.getSpecifications_id());
    }

    /**
     * Returns the devices connected to a specification, each enriched with its mapped specifications and per-port power details.
     *
     * @param specification_id the source specification identifier
     * @param pagesize the maximum number of connected records to fetch
     * @param offset the starting offset for pagination
     * @return the list of connected devices with populated specifications and power data
     */
    public List<DeviceDTO> getConnectedDevicesSpecifications(String specification_id, Integer pagesize, Integer offset) {
        List<DeviceDTO> devices = new ArrayList<>();
        List<ConnectedDevicesDTO> connectedDevicesDTOS = connectedDevicesRepository.getConnectedDevicesSpecifications(specification_id, pagesize, offset);

        for (ConnectedDevicesDTO specification : connectedDevicesDTOS) {

            DecimalFormat decimal_format = new DecimalFormat("#.##");

            DeviceDTO device = deviceService.getDeviceDetails(specification.getDevice_id());

            SpecificationsDTO spaceName = this.getSpaceNameByDeviceId(specification);

            List<SpecificationsDTO> specificationsDTOs = new ArrayList<>((this.mappingConnectedDevicesToSpecifications(Collections.singletonList(specification))));
            if (spaceName != null) {
                specificationsDTOs.add(spaceName);
            }

            for (SpecificationsDTO power_specification : specificationsDTOs) {
                Double total_power = specificationsService.calculateConsumedPower(power_specification.getKey_name(), power_specification.getDevice_id());
                JSONObject power = new JSONObject();
                power.put("power_rating", decimal_format.format(total_power));
                power.put("power_unit", this.getPowerUnit(power_specification.getDevice_id(), power_specification.getKey_name()));

                power_specification.setPower(power);

            }
            device.setSpecifications(specificationsDTOs);
            devices.add(device);
        }

        return devices;
    }

    /**
     * Returns the power unit for a device specification, defaulting to "W" when none is recorded.
     *
     * @param device_id the device identifier
     * @param key_name the specification key name
     * @return the power unit string
     */
    public String getPowerUnit(String device_id, String key_name) {
        String power_unit = "W";
        SpecificationsDTO specificationsDTO = specificationsService.getPowerDetails(device_id, key_name);
        if (specificationsDTO != null) {
            power_unit = specificationsDTO.getKey_unit();
        }

        return power_unit;

    }

    /**
     * Returns the power sources tagged to a device, each enriched with its mapped specifications and space name.
     *
     * @param username the requesting user's name
     * @param vdmsid the VDMS identifier
     * @param device_id the device identifier whose tagged power sources are retrieved
     * @return the list of tagged power-source devices with populated specifications
     */
    public List<DeviceDTO> getTaggedPowerSourcesByDeviceId(String username, String vdmsid, String device_id) {

        List<DeviceDTO> devices = new ArrayList<>();
        List<ConnectedDevicesDTO> connectedSpecifications = this.getConnectedSpecifications(device_id);

        for (ConnectedDevicesDTO specification : connectedSpecifications) {
            DeviceDTO device = deviceService.getDeviceDetails(specification.getDevice_id());
            SpecificationsDTO spaceName = this.getSpaceNameByDeviceId(specification);
            List<SpecificationsDTO> specificationsDTOs = new ArrayList<>((this.mappingConnectedDevicesToSpecifications(Collections.singletonList(specification))));
            if (spaceName != null) {
                specificationsDTOs.add(spaceName);
            }
            device.setSpecifications(specificationsDTOs);
            devices.add(device);
        }
        return devices;

    }

    /**
     * Maps connected-device records to specification DTOs, copying identifiers and key names.
     *
     * @param connectedDevicesDTO the connected-device records to map
     * @return the corresponding list of specification DTOs
     */
    public List<SpecificationsDTO> mappingConnectedDevicesToSpecifications(List<ConnectedDevicesDTO> connectedDevicesDTO) {
        List<SpecificationsDTO> specifications = new ArrayList<>();
        for (ConnectedDevicesDTO connectedDevice : connectedDevicesDTO) {
            SpecificationsDTO specificationsDTO = new SpecificationsDTO();
            specificationsDTO.setId(connectedDevice.getId());
            specificationsDTO.setConnected_specifications_id(connectedDevice.getConnected_specifications_id());
            specificationsDTO.setConnected_device(connectedDevice.getConnected_device());
            specificationsDTO.setDevice_id(connectedDevice.getDevice_id());
            specificationsDTO.setKey_name(connectedDevice.getKey_name());

            specifications.add(specificationsDTO);
        }

        return specifications;
    }


    /**
     * Returns the connected specifications associated with a device.
     *
     * @param device_id the device identifier
     * @return the list of connected-device records for the device
     */
    public List<ConnectedDevicesDTO> getConnectedSpecifications(String device_id) {
        return connectedDevicesRepository.getConnectedSpecificationsByDeviceId(device_id);

    }


    /**
     * Returns all input connected specifications for a device.
     *
     * @param device_id the device identifier
     * @return the list of input connected-device records
     */
    public List<ConnectedDevicesDTO> getAllInputConnectedSpecifications(String device_id) {
        return connectedDevicesRepository.getAllInputConnectedSpecifications(device_id);

    }

    /**
     * Returns all output connected specifications for a device.
     *
     * @param device_id the device identifier
     * @return the list of output connected-device records
     */
    public List<ConnectedDevicesDTO> getAllOutputConnectedSpecifications(String device_id) {
        return connectedDevicesRepository.getAllOutputConnectedSpecifications(device_id);

    }


    /**
     * Returns the space-name specification for the port referenced by a connected-device record.
     *
     * @param specificationsDTO the connected-device record whose port key name is inspected
     * @return the matching space-name specification, or {@code null} if none exists
     */
    public SpecificationsDTO getSpaceNameByDeviceId(ConnectedDevicesDTO specificationsDTO) {

        String key_name = specificationsDTO.getKey_name();

        String space_name = this.getPortPattern(key_name) + " Name";

        return specificationsService.getDeviceSpecificationsBasedOnDeviceIdAndKeyName(specificationsDTO.getDevice_id(), space_name);

    }


    /**
     * Extracts the port label (non-digit prefix followed by a number) from a specification key name.
     *
     * @param key_name the specification key name to parse
     * @return the matched port label, or {@code null} if no match is found
     */
    public String getPortPattern(String key_name) {

        Pattern pattern = Pattern.compile("\\D+\\s+\\d+");
        Matcher matcher = pattern.matcher(key_name);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;

    }


    /**
     * Removes the power-source tag between a specification and a connected specification.
     *
     * @param specifications_id the specification identifier
     * @param connected_specifications_id the connected specification identifier
     */
    public void untagPowerSource(String specifications_id, String connected_specifications_id) {

        connectedDevicesRepository.untagPowerSource(specifications_id, connected_specifications_id);

    }

    /**
     * Removes the connection tag between a specification and a connected specification.
     *
     * @param specifications_id the specification identifier
     * @param connected_specifications_id the connected specification identifier
     */
    public void untagDevice(String specifications_id, String connected_specifications_id) {

        connectedDevicesRepository.untagDevice(specifications_id, connected_specifications_id);

    }


    /**
     * Removes all power-source tags associated with a device.
     *
     * @param device_id the device identifier
     */
    public void untagPowerSourceByDeviceId(String device_id) {
        connectedDevicesRepository.untagPowerSourceByDeviceId(device_id);

    }



    /**
     * Populates each device with its full specification list.
     *
     * @param devices the devices to enrich with specifications
     * @return the same devices with their specifications populated
     */
    public List<DeviceDTO> getDeviceSpecificationsByDevices(List<DeviceDTO> devices) {
        for (DeviceDTO device : devices) {
            device.setSpecifications(specificationsService.getDeviceSpecificationsByDeviceId(null, null, device.getId()));
        }

        return devices;

    }


    /**
     * Returns the power-source topology connections involving the given devices.
     *
     * @param device_ids the set of device identifiers
     * @return the list of power-source connections for those devices
     */
    public List<PowerSourceConnectionsDTO> getPowerSourceTopologyForDevice(Set<String> device_ids) {
        return connectedDevicesRepository.getPowerSourceTopologyForDevice(device_ids);
    }


    /**
     * Returns all connected devices linked to a specification for use in load calculation.
     *
     * @param specification_id the source specification identifier
     * @return the list of connected-device records for load calculation
     */
    public List<ConnectedDevicesDTO> getAllConnectedDevicesForLoadCalculation(String specification_id) {

        return connectedDevicesRepository.getAllConnectedDevicesForLoadCalculation(specification_id);

    }


    /**
     * Returns the total number of power-source topology connections.
     *
     * @return the connection count
     */
    public Integer getPowerSourceTopologyConnectionsCount() {
        return connectedDevicesRepository.getPowerSourceTopologyConnectionsCount();

    }

    /**
     * Builds a paginated power-source topology, including connections, participating devices with specifications, and per-device load calculations.
     *
     * @param pageno the 1-based page number
     * @param pagesize the number of connections per page
     * @return the assembled power-source topology for the page
     */
    public PowerSourceTopologyDTO getPowerSourceTopologyByPagination(Integer pageno, Integer pagesize) {

        List<PowerSourceConnectionsDTO> powerSources = null;

        List<PowerSourceConnectionsDTO> allPowerSources = null;

        PowerSourceTopologyDTO powerSourceTopologyDTO = new PowerSourceTopologyDTO();

        powerSources = this.getPowerSourceTopologyPagination(pageno, pagesize);
        Set<String> devices = new HashSet<>();

        for (PowerSourceConnectionsDTO powerSource : powerSources) {

            devices.add(powerSource.getSource_device_id());
            devices.add(powerSource.getTarget_device_id());
        }

        allPowerSources = this.getPowerSourceTopologyForDevice(devices);


        powerSourceTopologyDTO.setConnections(powerSources);
        powerSourceTopologyDTO.setDevices(this.getDeviceSpecificationsByDevices(deviceService.getDeviceDetailsByDeviceIdList(devices)));

        List<PowerSourceConnectionsDTO> connections = allPowerSources;

        List<DeviceDTO> devices_for_topology = powerSourceTopologyDTO.getDevices();

        List<LoadCalculationDTO> load_calculation = this.calculateLoadForTopology(connections);

        for (DeviceDTO deviceDTO : devices_for_topology) {
            List<LoadCalculationDTO> load = new ArrayList<>();

            for (LoadCalculationDTO loadCalculationDTO : load_calculation) {
                if (loadCalculationDTO.getDevice_id().equals(deviceDTO.getId())) {
                    load.add(loadCalculationDTO);
                }
            }
            deviceDTO.setLoad_calculation(load);
        }

        return powerSourceTopologyDTO;

    }

    /**
     * Computes load calculations for the distinct power-source specifications referenced by the given connections.
     *
     * @param connections the power-source connections to evaluate
     * @return the list of load-calculation results
     */
    public List<LoadCalculationDTO> calculateLoadForTopology(List<PowerSourceConnectionsDTO> connections) {
        List<SpecificationsDTO> specifications = new ArrayList<>();
        List<SpecificationsDTO> power_source_specifications = new ArrayList<>();

        for (PowerSourceConnectionsDTO connection : connections) {
            SpecificationsDTO specificationsDTO = new SpecificationsDTO(connection.getSource_specifications_id(), connection.getSource_specifications_name(), connection.getSource_device_id());
            specifications.add(specificationsDTO);
        }
        for (SpecificationsDTO specification : specifications) {
            if (!power_source_specifications.contains(specification)) {
                power_source_specifications.add(specification);
            }
        }

        return specificationsService.getPowerBasedLoadCalculation(null, null, power_source_specifications);

    }

    /**
     * Returns a single page of power-source topology connections.
     *
     * @param pageno the 1-based page number
     * @param pagesize the number of connections per page
     * @return the list of power-source connections for the page
     */
    public List<PowerSourceConnectionsDTO> getPowerSourceTopologyPagination(Integer pageno, Integer pagesize) {

        Integer offset = pagesize * (pageno - 1);
        return connectedDevicesRepository.getPowerSourceTopologyByPagination(pagesize, offset);

    }


    /**
     * Deletes all connected-device records associated with a specification.
     *
     * @param specifications_id the specification identifier
     */
    public void deleteConnectedDevicesBySpecificationId(String specifications_id) {
        connectedDevicesRepository.deleteConnectedDevicesBySpecificationId(specifications_id);

    }
}
