
package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.DeviceConditionsRepository;
import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.ScheduledJobDTO;
import io.sclera.dto.ShareConditionsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

@Service
public class DeviceConditionsService {
    private static final Logger log = LoggerFactory.getLogger(DeviceConditionsService.class);

    @Autowired
    DeviceConditionsRepository deviceConditionsRepository;

    @Autowired
    AlertProfileService alertProfileService;

    @Autowired
    DeviceService deviceService;

    @Autowired
    JobSchedulerService jobSchedulerService;

    public void upsertDeviceConditions(String username, String vdmsid, String dockername, Set<DeviceConditionsDTO> device_conditions) {
        for (DeviceConditionsDTO device_condition : device_conditions) {

            DeviceDTO device = deviceService.getDeviceDetails(device_condition.getDevice_id());

            if (deviceConditionsRepository.deviceConditionById(device_condition.getId()) != 0) {
                Boolean isScheduleConditionsChanged = false;
                DeviceConditionsDTO deviceConditionDetails = deviceConditionsRepository.getDeviceConditionsById(device_condition.getId());

                device_condition.setLast_alerted(deviceConditionDetails.getLast_alerted());
                device_condition.setAlert_count(deviceConditionDetails.getAlert_count());


                if (deviceConditionDetails.getAlert_count_time() != null) {
                    if ((!(deviceConditionDetails.getAlert_count_time().equals(device_condition.getAlert_count_time())))) {
                        device_condition.setAlert_count(0);
                        device_condition.setLast_alerted(false);
                        deviceConditionsRepository.updateLastAlertedTimestamp(device_condition.getId());
                    }

                } else if (device_condition.getAlert_count_time() != null && deviceConditionDetails.getAlert_count_time() == null) {
                    device_condition.setAlert_count(0);
                    device_condition.setLast_alerted(false);
                    deviceConditionsRepository.updateLastAlertedTimestamp(device_condition.getId());
                }


                if ((deviceConditionDetails.getAlert_count_enabled() != device_condition.getAlert_count_enabled()) || (deviceConditionDetails.getMax_alert_count() != device_condition.getMax_alert_count()) ||
                        (!device_condition.getAlert_condition().equals(deviceConditionDetails.getAlert_condition())) ||
                        (deviceConditionDetails.getSchedule() != device_condition.getSchedule()) ||
                        (!deviceConditionDetails.getStart_time().equals(device_condition.getStart_time()) || (!deviceConditionDetails.getEnd_time().equals(device_condition.getEnd_time()))) ||
                        (deviceConditionDetails.getSchedule_conditions() != null && device_condition.getSchedule_conditions() == null) ||
                        (deviceConditionDetails.getSchedule_conditions() == null && device_condition.getSchedule_conditions() != null)) {
                    device_condition.setAlert_count(0);
                    device_condition.setLast_alerted(false);
                }

                if (deviceConditionDetails.getSchedule_conditions() != null && device_condition.getSchedule_conditions() != null) {
                    if (!deviceConditionDetails.getSchedule_conditions().equals(device_condition.getSchedule_conditions())) {
                        isScheduleConditionsChanged = true;
                        device_condition.setAlert_count(0);
                        device_condition.setLast_alerted(false);
                    }
                }

                if (deviceConditionDetails.getTrigger_time() != null) {
                    if (device_condition.getSchedule_conditions() == null)
                        if ((!(deviceConditionDetails.getTrigger_time().equals(device_condition.getTrigger_time()))) || (!device_condition.getAlert_condition().equals(deviceConditionDetails.getAlert_condition()))
                                || (deviceConditionDetails.getSchedule() != device_condition.getSchedule()) ||
                                (!deviceConditionDetails.getStart_time().equals(device_condition.getStart_time()) || (!deviceConditionDetails.getEnd_time().equals(device_condition.getEnd_time())) || isScheduleConditionsChanged)) {
                            device_condition.setLast_alerted(false);
                            ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(device_condition.getId());
                            if (scheduledJobDTO != null) {
                                deviceService.deleteDeviceAlertJob(device_condition.getId());
                            }
                        }

                } else if (deviceConditionDetails.getTrigger_time() == null && device_condition.getTrigger_time() != null) {
                    device_condition.setLast_alerted(false);
                }
                deviceConditionsRepository.updateDeviceConditions(device_condition.getId(),
                        device_condition.getAlert_condition(), device_condition.getDevice_id(), device_condition.getAlert_profile_id(), device_condition.getTrigger_time(), device_condition.getPriority(), device_condition.getStart_time(), device_condition.getEnd_time(), device_condition.getAlert_count(), device_condition.getMax_alert_count(), device_condition.getAlert_count_enabled(), device_condition.getSchedule(), device_condition.getSchedule_conditions(), device_condition.getAlert_count_time(), device_condition.getLast_alerted(), device_condition.getAlert_message());

            } else {
                if (device_condition.getId() == null) {
                    String device_condition_id = Generators.timeBasedGenerator().generate().toString();
                    device_condition.setId(device_condition_id);

                    deviceConditionsRepository.addDeviceConditions(device_condition.getId(), device_condition.getAlert_condition(), device_condition.getDevice_id(),
                            device_condition.getAlert_profile_id(), device_condition.getTrigger_time(), device_condition.getPriority(), device_condition.getStart_time(), device_condition.getEnd_time(), device_condition.getMax_alert_count(), device_condition.getAlert_count_enabled(), device_condition.getSchedule(), device_condition.getSchedule_conditions(), device_condition.getAlert_count_time(), false, device_condition.getAlert_message());

                }
            }

            if (device != null && device.getStatus() != null) {
                deviceService.getDeviceConditionStatus(device.getId(), device.getStatus());

            }

        }
    }

    public Set<DeviceConditionsDTO> getDeviceConditions(String username, String vdmsid, String dockername, String device_id) {
        Set<DeviceConditionsDTO> deviceConditions = deviceConditionsRepository.getDeviceConditions(device_id);
        for (DeviceConditionsDTO deviceCondition : deviceConditions) {
            if (deviceCondition.getAlert_profile_id() != null) {
                deviceCondition.setAlert_profile(alertProfileService.getAlertProfileDetailsById(null, null, deviceCondition.getAlert_profile_id()));
            }
        }
        return deviceConditions;
    }

    public void deleteAllDeviceConditions(String username, String vdmsid, String device_id) {
        Set<DeviceConditionsDTO> device_conditions = deviceConditionsRepository.getDeviceConditions(device_id);
        for (DeviceConditionsDTO deviceCondition : device_conditions) {
            deviceConditionsRepository.deleteById(deviceCondition.getId());
        }

    }

    public DeviceConditionsDTO getDeviceConditionsById(String username, String vdmsid, String device_condition_id) {
        DeviceConditionsDTO deviceConditions = deviceConditionsRepository.getDeviceConditionsById(device_condition_id);
        if (deviceConditions.getAlert_profile_id() != null) {
            deviceConditions.setAlert_profile(alertProfileService.getAlertProfileDetailsById(null, null, deviceConditions.getAlert_profile_id()));
        }
        return deviceConditions;
    }

    public void deleteDeviceConditions(String username, String vdmsid, Set<DeviceConditionsDTO> device_conditions) {
        for (DeviceConditionsDTO deviceCondition : device_conditions) {
            deviceConditionsRepository.deleteById(deviceCondition.getId());
        }
    }

    public void updateLastAlertedDetails(String id, BigInteger current_timestamp, Boolean last_alerted, Integer alert_count) {
        deviceConditionsRepository.updateLastAlertedDetails(id, current_timestamp, last_alerted, alert_count);

    }

    public void shareDeviceConditions(String username, String vdmsid, String dockername, ShareConditionsDTO shareConditions) {
        for (DeviceDTO device : shareConditions.getDevices()) {
            try {
                if (shareConditions.getCondition_method() != null && shareConditions.getCondition_method().equals("replace")) {
                    Set<DeviceConditionsDTO> deleteDeviceConditions = deviceConditionsRepository.getDeviceConditions(device.getId());
                    if (deleteDeviceConditions != null) {
                        deleteDeviceConditions(username, vdmsid, deleteDeviceConditions);
                    }

                }
                if (shareConditions.getCondition_method() != null && (shareConditions.getCondition_method().equals("add") || shareConditions.getCondition_method().equals("replace"))) {
                    Set<DeviceConditionsDTO> newDeviceConditionsList = new HashSet<>();
                    System.out.println(shareConditions.getDeviceConditions().size());

                    for (DeviceConditionsDTO deviceCondition : shareConditions.getDeviceConditions()) {
                        DeviceConditionsDTO deviceConditions = new DeviceConditionsDTO(deviceCondition.getAlert_condition(), device.getId(),
                                deviceCondition.getAlert_profile_id(), deviceCondition.getTrigger_time(), deviceCondition.getLast_alerted_time(), deviceCondition.getPriority(), deviceCondition.getStart_time(), deviceCondition.getEnd_time(), deviceCondition.getSchedule(), deviceCondition.getSchedule_conditions(), deviceCondition.getMax_alert_count(), deviceCondition.getAlert_count_time(), deviceCondition.getAlert_count_enabled(), deviceCondition.getAlert_message());
                        newDeviceConditionsList.add(deviceConditions);
                    }
                    try {
                        this.upsertDeviceConditions(username, vdmsid, dockername, newDeviceConditionsList);
                    } catch (Exception e) {
                        System.out.println("Error in upsert the share the device_condition " + e);
                        System.out.println(e);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in share device_conditions " + e);
                System.out.println(e);
            }
        }

    }

    public void updateAlertProfileId(String alert_profile_id) {
        deviceConditionsRepository.updateAlertProfileId(alert_profile_id);
    }

    public void resetDeviceConditions(String username, String vdmsid, String dockername, Set<DeviceConditionsDTO> deviceConditions) {
        for (DeviceConditionsDTO deviceConditionsDTO : deviceConditions) {
            DeviceDTO device = deviceService.getDeviceDetails(deviceConditionsDTO.getDevice_id());
            deviceConditionsRepository.resetDeviceConditions(deviceConditionsDTO.getId(), 0, false);
            if (device != null && device.getStatus() != null) {
                deviceService.getDeviceConditionStatus(device.getId(), device.getStatus());
            }

        }

    }

    public void upsertDeviceConditionsForAiCall(String username, String vdmsid, String dockername, Set<DeviceConditionsDTO> device_conditions) {
        for (DeviceConditionsDTO device_condition : device_conditions) {
            try {
                DeviceDTO device = deviceService.getDeviceDetails(device_condition.getDevice_id());
                String id = deviceConditionsRepository.getDeviceConditionIdByDeviceId(device_condition.getDevice_id());
                if (id == null) {
                    String device_condition_id = Generators.timeBasedGenerator().generate().toString();
                    device_condition.setId(device_condition_id);
                    System.out.println("Adding new device condition with ID: " + device_condition_id);
                    deviceConditionsRepository.addDeviceConditions(device_condition.getId(), device_condition.getAlert_condition(), device_condition.getDevice_id(),
                            device_condition.getAlert_profile_id(), device_condition.getTrigger_time(), device_condition.getPriority(), device_condition.getStart_time(), device_condition.getEnd_time(), device_condition.getMax_alert_count(), device_condition.getAlert_count_enabled(), device_condition.getSchedule(), device_condition.getSchedule_conditions(), device_condition.getAlert_count_time(), false, device_condition.getAlert_message());
                    System.out.println("New device condition added successfully.");
                } else {
                    log.info("Device condition ID {} not null",id);
                    int alertCount = this.getAlertCount(device_condition.getDevice_id());
                    if (alertCount == 3) {
                        this.deleteDeviceConditionByDeviceId(device_condition.getDevice_id());
                        String device_condition_id = Generators.timeBasedGenerator().generate().toString();
                        device_condition.setId(device_condition_id);
                        deviceConditionsRepository.addDeviceConditions(device_condition.getId(), device_condition.getAlert_condition(), device_condition.getDevice_id(),
                                device_condition.getAlert_profile_id(), device_condition.getTrigger_time(), device_condition.getPriority(), device_condition.getStart_time(), device_condition.getEnd_time(), device_condition.getMax_alert_count(), device_condition.getAlert_count_enabled(), device_condition.getSchedule(), device_condition.getSchedule_conditions(), device_condition.getAlert_count_time(), false, device_condition.getAlert_message());
                    }

                }
                if (device != null && device.getStatus() != null) {
                    deviceService.getAiCallDeviceOfflineConditionStatus(device.getId(), device.getStatus());

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

    }

    public Integer getAlertCount(String deviceId) {
        return deviceConditionsRepository.getAlertCount(deviceId);
    }

    private void deleteDeviceConditionByDeviceId(String deviceId) {
        Set<DeviceConditionsDTO> deviceConditions = deviceConditionsRepository.getDeviceConditionsForAiCall(deviceId);
        if (deviceConditions != null && !deviceConditions.isEmpty()) {
            for (DeviceConditionsDTO deviceCondition : deviceConditions) {
                deviceConditionsRepository.deleteById(deviceCondition.getId());
            }
        }
    }

    public Set<DeviceConditionsDTO> getDeviceConditionsForAiCall(String username, String vdmsid, String dockername, String device_id) {
        Set<DeviceConditionsDTO> deviceConditions = deviceConditionsRepository.getDeviceConditionsForAiCall(device_id);
        return deviceConditions;
    }

    public DeviceConditionsDTO getDeviceConditionsByIdForAiCall(String username, String vdmsid, String device_condition_id) {
        System.out.println("Fetching device conditions for AI call with ID: " + device_condition_id);
        DeviceConditionsDTO deviceConditions = deviceConditionsRepository.getDeviceConditionsByIdForAiCall(device_condition_id);
        System.out.println("Device conditions fetched: " + deviceConditions);
        return deviceConditions;
    }

    public void updateAlertCountByConditionId(String id, int alertCount) {
        if(id!=null && alertCount > 0) {
            deviceConditionsRepository.updateAlertCountByConditionId(id,alertCount);
        } else {
            System.out.println("Invalid condition ID or alert count.");
        }
    }
}
