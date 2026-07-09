package io.sclera.service;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.LoadCalculationDTO;
import io.sclera.dto.SpecificationsDTO;

import java.util.List;

/** Service contract for the matching service class. */
public interface SpecificationsService {

    void editDeviceSpecifications(String username, String vdmsid, List<SpecificationsDTO> specifications);

    int checkSpecificationByDeviceId(String virtual_device_id, String key_name);

    List<SpecificationsDTO> getDeviceSpecificationsByDeviceId(String username, String vdmsid, String device_id);

    void upsertDeviceSpecification(SpecificationsDTO specificationsDTO);

    List<DeviceDTO> getTaggedDevices(String username, String vdmsid, SpecificationsDTO specificationsDTO, Integer pageno, Integer pagesize);

    List<DeviceDTO> getTaggedPowerSourcesByDeviceId(String username, String vdmsid, String device_id);

    void untagPowerSource(String username, String vdmsid, List<SpecificationsDTO> specifications);

    void untagDevice(String username, String vdmsid, List<SpecificationsDTO> specifications);

    List<SpecificationsDTO> upsertDeviceSpecifications(String username, String vdmsid, List<SpecificationsDTO> specifications);

    void deleteSpecifications(String username, String vdmsid, List<SpecificationsDTO> specifications);

    void tagPowerSources(String username, String vdmsid, List<SpecificationsDTO> specifications);

    SpecificationsDTO getDeviceSpecificationsBasedOnDeviceIdAndKeyName(String device_id, String key_name);

    int checkSpecificationsAdded(String key_name, String device_id);

    void addConnectedDevices(SpecificationsDTO specificationsDTO);

    Double getTotalPowerWithHeadRoom(Double total_power);

    Double getAllConnectedDevices(String specification_id);

    Double calculateConsumedPower(String key_name, String device_id);

    SpecificationsDTO getPowerDetails(String device_id, String key_name);

    Double calculatePower(SpecificationsDTO specificationsDTO);

    List<LoadCalculationDTO> getPowerBasedLoadCalculation(String username, String vdmsid, List<SpecificationsDTO> specifications);
}
