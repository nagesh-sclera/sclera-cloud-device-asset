
package io.sclera.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.ConnectedDevicesRepository;
import io.sclera.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ConnectedDevicesService {

    @Autowired
    ConnectedDevicesRepository connectedDevicesRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    SpecificationsService specificationsService;

    @Autowired
    APICallService apiCallService;


    public void addConnectedDevices(ConnectedDevicesDTO connectedDevicesDTO) {

        connectedDevicesRepository.addConnectedDevices(connectedDevicesDTO.getId(), connectedDevicesDTO.getConnected_specifications_id(), connectedDevicesDTO.getSpecifications_id());
    }

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

    public String getPowerUnit(String device_id, String key_name) {
        String power_unit = "W";
        SpecificationsDTO specificationsDTO = specificationsService.getPowerDetails(device_id, key_name);
        if (specificationsDTO != null) {
            power_unit = specificationsDTO.getKey_unit();
        }

        return power_unit;

    }

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


    public List<ConnectedDevicesDTO> getConnectedSpecifications(String device_id) {
        return connectedDevicesRepository.getConnectedSpecificationsByDeviceId(device_id);

    }


    public List<ConnectedDevicesDTO> getAllInputConnectedSpecifications(String device_id) {
        return connectedDevicesRepository.getAllInputConnectedSpecifications(device_id);

    }

    public List<ConnectedDevicesDTO> getAllOutputConnectedSpecifications(String device_id) {
        return connectedDevicesRepository.getAllOutputConnectedSpecifications(device_id);

    }


    public SpecificationsDTO getSpaceNameByDeviceId(ConnectedDevicesDTO specificationsDTO) {

        String key_name = specificationsDTO.getKey_name();

        String space_name = this.getPortPattern(key_name) + " Name";

        return specificationsService.getDeviceSpecificationsBasedOnDeviceIdAndKeyName(specificationsDTO.getDevice_id(), space_name);

    }


    public String getPortPattern(String key_name) {

        Pattern pattern = Pattern.compile("\\D+\\s+\\d+");
        Matcher matcher = pattern.matcher(key_name);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;

    }


    public void untagPowerSource(String specifications_id, String connected_specifications_id) {

        connectedDevicesRepository.untagPowerSource(specifications_id, connected_specifications_id);

    }

    public void untagDevice(String specifications_id, String connected_specifications_id) {

        connectedDevicesRepository.untagDevice(specifications_id, connected_specifications_id);

    }


    public void untagPowerSourceByDeviceId(String device_id) {
        connectedDevicesRepository.untagPowerSourceByDeviceId(device_id);

    }



    public List<DeviceDTO> getDeviceSpecificationsByDevices(List<DeviceDTO> devices) {
        for (DeviceDTO device : devices) {
            device.setSpecifications(specificationsService.getDeviceSpecificationsByDeviceId(null, null, device.getId()));
        }

        return devices;

    }


    public List<PowerSourceConnectionsDTO> getPowerSourceTopologyForDevice(Set<String> device_ids) {
        return connectedDevicesRepository.getPowerSourceTopologyForDevice(device_ids);
    }


    public List<ConnectedDevicesDTO> getAllConnectedDevicesForLoadCalculation(String specification_id) {

        return connectedDevicesRepository.getAllConnectedDevicesForLoadCalculation(specification_id);

    }


    public Integer getPowerSourceTopologyConnectionsCount() {
        return connectedDevicesRepository.getPowerSourceTopologyConnectionsCount();

    }

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

    public List<PowerSourceConnectionsDTO> getPowerSourceTopologyPagination(Integer pageno, Integer pagesize) {

        Integer offset = pagesize * (pageno - 1);
        return connectedDevicesRepository.getPowerSourceTopologyByPagination(pagesize, offset);

    }


    public void deleteConnectedDevicesBySpecificationId(String specifications_id) {
        connectedDevicesRepository.deleteConnectedDevicesBySpecificationId(specifications_id);

    }
}
