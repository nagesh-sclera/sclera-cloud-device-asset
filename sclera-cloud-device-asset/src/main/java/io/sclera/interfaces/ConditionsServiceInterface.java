package io.sclera.interfaces;

import io.sclera.dto.*;
import io.sclera.dto.touchscreen.SensorDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/** Service contract for {@link io.sclera.service.ConditionsService}. */
public interface ConditionsServiceInterface {

    void upsertConditions(String username, String vdmsid, String dockername, String conditionGroup, Set<ConditionsDTO> conditions, HttpServletRequest httpServletRequest);

    void updateConditionAlertOnUpsert(String conditionType, String conditionGroup, ConditionsDTO condition);

    void updateConditionAlert(String conditionGroup, String id, String sub_id, String value, String conditionType, String type);

    void deleteConditions(String username, String vdmsid, String dockername, String conditionGroup, Set<ConditionsDTO> conditions, HttpServletRequest httpServletRequest);

    Set<ConditionsDTO> getConditions(String username, String vdmsid, String dockername, String conditionGroup, SensorDTO sensorData);

    Set<ConditionsDTO> getConditionsTS(String conditionGroup, SensorDTO sensorData);

    Set<ConditionsDTO> getSensorConditions(String conditionGroup, SensorDTO sensorData);

    Set<ConditionsDTO> getConditionsFrontend(String username, String vdmsid, String dockername, String conditionGroup, String id, String sub_id);

    ConditionsDTO updateShareConditionSensorIds(String conditionMethod, String conditionGroup, ConditionsDTO condition, SensorDTO sensor);

    void deleteAllSensorConditions(String username, String vdmsid, String dockername, String conditionGroup, Set<SensorDTO> sensorData, HttpServletRequest httpServletRequest);

    void deleteSensorConditions(String username, String vdmsid, String dockername, String conditionGroup, SensorDTO sensor, HttpServletRequest httpServletRequest);

    void resetConditions(String username, String vdmsid, String dockername, String conditionGroup, Set<ConditionsDTO> conditions);

    void resetConditionsTS(String conditionGroup, Set<ConditionsDTO> conditions);

    void resetConditionsAlerted(String conditionGroup, Set<ConditionsDTO> conditions);

    void sendBacnetAlertInfo(String bacnet_device_id, String bacnet_object_id, String alert_message);

    void sendLorawanAlertInfo(String lorawan_sensor_id, String lorawan_sensor_attributes_name, String alert_message);

    void sendDisruptiveAlertInfo(String disruptive_sensor_id, String alert_message);

    void sendMyDevicesAlertInfo(String my_devices_sensor_id, String my_devices_sensor_attributes_name, String alert_message);

    void sendMonnitAlertInfo(String monnit_sensor_id, String alert_message);

    void sendPelicanAlertInfo(String pelican_sensor_id, String pelican_sensor_attributes_name, String alert_message);

    void sendKNXAlertInfo(String knx_device_address, String knx_group_address, String alert_message);

    void sendMeasuringInstrumentAlertInfo(String measuring_instrument_id, String alert_message);

    void sendAlertInfo(String conditionGroup, ConditionsDTO condition, String alert_message, BigInteger schedule_job_created_time);

    void scheduleSensorAlertJob(String job_type, ConditionsDTO condition, String condition_group);

    void replaceSensorAlertJob(String job_type, ConditionsDTO condition, String condition_group, String job_key);

    ConditionsDTO getConditionByConditionId(String condition_id);

    void deleteSensorAlertJob(String conditionId);

    List<ConditionsAdvanceExportExcelDto> getConditionsForAdvanceExcelExport(String username, String vdmsid, String device_id);
}
