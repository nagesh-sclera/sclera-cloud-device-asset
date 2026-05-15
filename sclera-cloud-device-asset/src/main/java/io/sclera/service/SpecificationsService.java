
package io.sclera.service;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.uuid.Generators;
import io.sclera.Repository.SpecificationsRepository;
import io.sclera.dto.ConnectedDevicesDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.LoadCalculationDTO;
import io.sclera.dto.SpecificationsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


@Service
public class SpecificationsService {

    @Autowired
    SpecificationsRepository specificationsRepository;


    @Autowired
    ConnectedDevicesService connectedDevicesService;


    public void editDeviceSpecifications(String username, String vdmsid, List<SpecificationsDTO> specifications) {

        for (SpecificationsDTO specificationsDTO : specifications) {
            specificationsRepository.editDeviceSpecifications(specificationsDTO.getId(), specificationsDTO.getKey_value(), specificationsDTO.getKey_unit(), specificationsDTO.getKey_name());

        }

    }

    public int checkSpecificationByDeviceId(String virtual_device_id, String key_name) {
        return specificationsRepository.checkSpecificationByDeviceId(virtual_device_id, key_name);
    }

    public List<SpecificationsDTO> getDeviceSpecificationsByDeviceId(String username, String vdmsid, String device_id) {
        List<SpecificationsDTO> specifications = specificationsRepository.getDeviceSpecificationsBasedOnDeviceId(device_id);
        return specifications;
    }

    public void upsertDeviceSpecification(SpecificationsDTO specificationsDTO) {
        if (specificationsDTO.getId() == null) {
            String id = Generators.timeBasedGenerator().generate().toString();
            specificationsDTO.setId(id);
        }

        specificationsRepository.upsertDeviceSpecification(specificationsDTO.getId(), specificationsDTO.getKey_name(), specificationsDTO.getKey_value(), specificationsDTO.getKey_unit(), specificationsDTO.getDevice_id());
    }


    public List<DeviceDTO> getTaggedDevices(String username, String vdmsid, SpecificationsDTO specificationsDTO, Integer pageno, Integer pagesize) {

        if(specificationsDTO!= null)
        {
            Integer offset = pagesize * (pageno - 1);
            return connectedDevicesService.getConnectedDevicesSpecifications(specificationsDTO.getId(), pagesize, offset);
        }
        return null;

    }

    public List<DeviceDTO> getTaggedPowerSourcesByDeviceId(String username, String vdmsid, String device_id) {

        return connectedDevicesService.getTaggedPowerSourcesByDeviceId(username, vdmsid, device_id);

    }


    public void untagPowerSource(String username, String vdmsid, List<SpecificationsDTO> specifications) {

        for (SpecificationsDTO specificationsDTO : specifications) {

            connectedDevicesService.untagPowerSource(specificationsDTO.getId(), specificationsDTO.getConnected_specifications_id());

        }

    }

    public void untagDevice(String username, String vdmsid, List<SpecificationsDTO> specifications) {

        for (SpecificationsDTO specificationsDTO : specifications) {

            connectedDevicesService.untagDevice(specificationsDTO.getConnected_specifications_id(), specificationsDTO.getId());

        }

    }

    public List<SpecificationsDTO> upsertDeviceSpecifications(String username, String vdmsid, List<SpecificationsDTO> specifications) {

        String device_id = null;

        for (SpecificationsDTO specificationsDTO : specifications) {
            int count = this.checkSpecificationByDeviceId(specificationsDTO.getDevice_id(), specificationsDTO.getKey_name());

            if (count == 0) {

                this.upsertDeviceSpecification(specificationsDTO);
                device_id = specificationsDTO.getDevice_id();
            } else {
                if (specificationsDTO.getId() != null) {
                    this.upsertDeviceSpecification(specificationsDTO);
                    device_id = specificationsDTO.getDevice_id();

                }

            }

        }
        if (device_id != null) {
            return this.getDeviceSpecificationsByDeviceId(username, vdmsid, device_id);
        }
        return null;
    }

    public void deleteSpecifications(String username, String vdmsid, List<SpecificationsDTO> specifications) {
        for (SpecificationsDTO specificationsDTO : specifications) {
            connectedDevicesService.deleteConnectedDevicesBySpecificationId(specificationsDTO.getId());
            specificationsRepository.deleteById(specificationsDTO.getId());
        }

    }

    // store the output specification and its connected entry
    public void tagPowerSources(String username, String vdmsid, List<SpecificationsDTO> specifications) {

        List<String> specification_ids = new ArrayList<>();
        SpecificationsDTO outputSpecification = new SpecificationsDTO();

        for (SpecificationsDTO specification : specifications) {

            int check_input = this.checkSpecificationsAdded(specification.getKey_name(), specification.getDevice_id());
            if (check_input == 0) {
                this.upsertDeviceSpecification(specification);
            }

            if (specification.getKey_name().startsWith("I")) {
                SpecificationsDTO addSpecification = this.getDeviceSpecificationsBasedOnDeviceIdAndKeyName(specification.getDevice_id(), specification.getKey_name());
                specification_ids.add(addSpecification.getId());
            } else {
                SpecificationsDTO addOutputSpecification = this.getDeviceSpecificationsBasedOnDeviceIdAndKeyName(specification.getDevice_id(), specification.getKey_name());
                outputSpecification = addOutputSpecification;
            }
        }

        for (String specification_id : specification_ids) {
            outputSpecification.setConnected_specifications_id(specification_id);
            this.addConnectedDevices(outputSpecification);

        }

    }

    public SpecificationsDTO getDeviceSpecificationsBasedOnDeviceIdAndKeyName(String device_id, String key_name) {
        return specificationsRepository.getDeviceSpecificationsBasedOnDeviceIdAndKeyName(device_id, key_name);
    }

    public int checkSpecificationsAdded(String key_name, String device_id) {
        return specificationsRepository.checkSpecificationByDeviceId(device_id, key_name);
    }


    public void addConnectedDevices(SpecificationsDTO specificationsDTO) {
        String id = Generators.timeBasedGenerator().generate().toString();
        ConnectedDevicesDTO connectedDevicesDTO = new ConnectedDevicesDTO(id, specificationsDTO.getConnected_specifications_id(), specificationsDTO.getId());
        connectedDevicesService.addConnectedDevices(connectedDevicesDTO);
    }


    /************************ Calculate 20 cent head room from total capacity *************************/

    public Double getTotalPowerWithHeadRoom(Double total_power) {

        Double reduced_power = total_power * 0.2;
        return total_power - reduced_power;

    }

    public Double getAllConnectedDevices(String specification_id) {

        Double total_consumed_power = 0.00;

        List<ConnectedDevicesDTO> connectedDevicesDTOS = connectedDevicesService.getAllConnectedDevicesForLoadCalculation(specification_id);

        if (connectedDevicesDTOS != null && connectedDevicesDTOS.size() > 0) {
            for (ConnectedDevicesDTO connectedDevicesDTO : connectedDevicesDTOS) {

                total_consumed_power = total_consumed_power + this.calculateConsumedPower(connectedDevicesDTO.getKey_name(), connectedDevicesDTO.getDevice_id());
            }

            return total_consumed_power;
        }
        return total_consumed_power;

    }

    public Double calculateConsumedPower(String key_name, String device_id) {

        SpecificationsDTO specificationsDTOS = this.getPowerDetails(device_id, key_name);
        return this.calculatePower(specificationsDTOS);

    }

    public SpecificationsDTO getPowerDetails(String device_id, String key_name) {

        String port_prefix = connectedDevicesService.getPortPattern(key_name);
        return specificationsRepository.getPower(device_id, port_prefix + " Power");
    }


    public Double calculatePower(SpecificationsDTO specificationsDTO) {
        Double power = 0.00;

        if (specificationsDTO != null && specificationsDTO.getKey_name().contains("Power") && specificationsDTO.getKey_value()!= null && (!specificationsDTO.getKey_value().equals(""))){

            power = Double.parseDouble(specificationsDTO.getKey_value());
        }

        if (power != 0.00) {
            DecimalFormat decimal_format = new DecimalFormat("#.##");
            return Double.parseDouble(decimal_format.format(power));
        }

        return power;
    }

    public List<LoadCalculationDTO> getPowerBasedLoadCalculation(String username, String vdmsid, List<SpecificationsDTO> specifications) {

        DecimalFormat decimal_format = new DecimalFormat("#.##");
        List<LoadCalculationDTO> loadCalculations = new ArrayList<>();

        for (SpecificationsDTO specification : specifications) {

            Double total_power = this.calculateConsumedPower(specification.getKey_name(), specification.getDevice_id());

            Double consumed_power = this.getAllConnectedDevices(specification.getId());

            Double residue_power = this.getTotalPowerWithHeadRoom(total_power);


            JSONObject power = new JSONObject();
            power.put("power_rating", decimal_format.format(total_power));
            power.put("power_usage", decimal_format.format(consumed_power));
            power.put("power_capacity", decimal_format.format(residue_power));
            power.put("power_unit", connectedDevicesService.getPowerUnit(specification.getDevice_id(), specification.getKey_name()));


            if (total_power > 0.00) {
                LoadCalculationDTO loadCalculationDTO = new LoadCalculationDTO(specification.getId(), specification.getKey_name(), specification.getDevice_id(), power);
                loadCalculations.add(loadCalculationDTO);
            }

        }

        return loadCalculations;
    }


}
