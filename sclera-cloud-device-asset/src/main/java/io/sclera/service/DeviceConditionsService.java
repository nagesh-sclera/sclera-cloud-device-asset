
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

/**
 * Manages device alert conditions: their persistence, sharing, resetting, and
 * alert-count bookkeeping, including a dedicated path for AI-call conditions.
 *
 * <p>Collaborators:
 * <ul>
 *   <li>{@link DeviceConditionsRepository} — reads and writes device condition records.</li>
 *   <li>{@link io.sclera.client.AlertProfileClient} — resolves alert profile details by id.</li>
 *   <li>{@link DeviceService} — fetches device details and recomputes device condition status.</li>
 *   <li>{@link JobSchedulerService} — looks up scheduled jobs tied to a condition.</li>
 * </ul>
 */
@Service
public class DeviceConditionsService {
    private static final Logger log = LoggerFactory.getLogger(DeviceConditionsService.class);

    @Autowired
    DeviceConditionsRepository deviceConditionsRepository;

    @Autowired
    io.sclera.client.AlertProfileClient alertProfileClient;

    @Autowired
    DeviceService deviceService;

    @Autowired
    JobSchedulerService jobSchedulerService;

    /**
     * Inserts new device conditions or updates existing ones, resetting alert counters and
     * cancelling scheduled alert jobs when schedule-affecting fields change, and refreshes
     * the affected device's condition status.
     *
     * @param username the requesting user
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @param device_conditions the conditions to upsert
     */
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

    /**
     * Returns the conditions for a device, enriching each with its resolved alert profile.
     *
     * @param username the requesting user
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @param device_id the device whose conditions are retrieved
     * @return the device's conditions with alert profiles populated
     */
    public Set<DeviceConditionsDTO> getDeviceConditions(String username, String vdmsid, String dockername, String device_id) {
        Set<DeviceConditionsDTO> deviceConditions = deviceConditionsRepository.getDeviceConditions(device_id);
        for (DeviceConditionsDTO deviceCondition : deviceConditions) {
            if (deviceCondition.getAlert_profile_id() != null) {
                deviceCondition.setAlert_profile(alertProfileClient.getAlertProfileDetailsById(null, null, deviceCondition.getAlert_profile_id()));
            }
        }
        return deviceConditions;
    }

    /**
     * Deletes every condition associated with the given device.
     *
     * @param username the requesting user
     * @param vdmsid the VDMS identifier
     * @param device_id the device whose conditions are deleted
     */
    public void deleteAllDeviceConditions(String username, String vdmsid, String device_id) {
        Set<DeviceConditionsDTO> device_conditions = deviceConditionsRepository.getDeviceConditions(device_id);
        for (DeviceConditionsDTO deviceCondition : device_conditions) {
            deviceConditionsRepository.deleteById(deviceCondition.getId());
        }

    }

    /**
     * Returns a single condition by id, enriched with its resolved alert profile.
     *
     * @param username the requesting user
     * @param vdmsid the VDMS identifier
     * @param device_condition_id the condition identifier
     * @return the matching condition with its alert profile populated
     */
    public DeviceConditionsDTO getDeviceConditionsById(String username, String vdmsid, String device_condition_id) {
        DeviceConditionsDTO deviceConditions = deviceConditionsRepository.getDeviceConditionsById(device_condition_id);
        if (deviceConditions.getAlert_profile_id() != null) {
            deviceConditions.setAlert_profile(alertProfileClient.getAlertProfileDetailsById(null, null, deviceConditions.getAlert_profile_id()));
        }
        return deviceConditions;
    }

    /**
     * Deletes the supplied conditions by their ids.
     *
     * @param username the requesting user
     * @param vdmsid the VDMS identifier
     * @param device_conditions the conditions to delete
     */
    public void deleteDeviceConditions(String username, String vdmsid, Set<DeviceConditionsDTO> device_conditions) {
        for (DeviceConditionsDTO deviceCondition : device_conditions) {
            deviceConditionsRepository.deleteById(deviceCondition.getId());
        }
    }

    /**
     * Updates a condition's last-alerted timestamp, last-alerted flag, and alert count.
     *
     * @param id the condition identifier
     * @param current_timestamp the timestamp of the most recent alert
     * @param last_alerted whether the condition has alerted
     * @param alert_count the updated alert count
     */
    public void updateLastAlertedDetails(String id, BigInteger current_timestamp, Boolean last_alerted, Integer alert_count) {
        deviceConditionsRepository.updateLastAlertedDetails(id, current_timestamp, last_alerted, alert_count);

    }

    /**
     * Shares a set of conditions across target devices, replacing existing conditions first
     * when the share method is {@code replace} and adding new conditions for {@code add} or
     * {@code replace} methods.
     *
     * @param username the requesting user
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @param shareConditions the share request describing target devices, method, and conditions
     */
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

    /**
     * Clears the given alert profile id from any conditions referencing it.
     *
     * @param alert_profile_id the alert profile identifier to update
     */
    public void updateAlertProfileId(String alert_profile_id) {
        deviceConditionsRepository.updateAlertProfileId(alert_profile_id);
    }

    /**
     * Resets each condition's alert count and last-alerted flag and refreshes the affected
     * device's condition status.
     *
     * @param username the requesting user
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @param deviceConditions the conditions to reset
     */
    public void resetDeviceConditions(String username, String vdmsid, String dockername, Set<DeviceConditionsDTO> deviceConditions) {
        for (DeviceConditionsDTO deviceConditionsDTO : deviceConditions) {
            DeviceDTO device = deviceService.getDeviceDetails(deviceConditionsDTO.getDevice_id());
            deviceConditionsRepository.resetDeviceConditions(deviceConditionsDTO.getId(), 0, false);
            if (device != null && device.getStatus() != null) {
                deviceService.getDeviceConditionStatus(device.getId(), device.getStatus());
            }

        }

    }

    /**
     * Upserts AI-call device conditions: adds a new condition when none exists for the device,
     * or replaces it once the alert count reaches the threshold, then refreshes the device's
     * AI-call offline condition status.
     *
     * @param username the requesting user
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @param device_conditions the AI-call conditions to upsert
     */
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

    /**
     * Returns the current alert count for a device.
     *
     * @param deviceId the device identifier
     * @return the device's alert count
     */
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

    /**
     * Returns the AI-call conditions for a device.
     *
     * @param username the requesting user
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @param device_id the device whose AI-call conditions are retrieved
     * @return the device's AI-call conditions
     */
    public Set<DeviceConditionsDTO> getDeviceConditionsForAiCall(String username, String vdmsid, String dockername, String device_id) {
        Set<DeviceConditionsDTO> deviceConditions = deviceConditionsRepository.getDeviceConditionsForAiCall(device_id);
        return deviceConditions;
    }

    /**
     * Returns a single AI-call condition by id.
     *
     * @param username the requesting user
     * @param vdmsid the VDMS identifier
     * @param device_condition_id the condition identifier
     * @return the matching AI-call condition
     */
    public DeviceConditionsDTO getDeviceConditionsByIdForAiCall(String username, String vdmsid, String device_condition_id) {
        System.out.println("Fetching device conditions for AI call with ID: " + device_condition_id);
        DeviceConditionsDTO deviceConditions = deviceConditionsRepository.getDeviceConditionsByIdForAiCall(device_condition_id);
        System.out.println("Device conditions fetched: " + deviceConditions);
        return deviceConditions;
    }

    /**
     * Updates a condition's alert count when the id is non-null and the count is positive.
     *
     * @param id the condition identifier
     * @param alertCount the new alert count
     */
    public void updateAlertCountByConditionId(String id, int alertCount) {
        if(id!=null && alertCount > 0) {
            deviceConditionsRepository.updateAlertCountByConditionId(id,alertCount);
        } else {
            System.out.println("Invalid condition ID or alert count.");
        }
    }
}
