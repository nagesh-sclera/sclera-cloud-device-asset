package io.sclera.service;

import java.math.BigInteger;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.ScheduledJobRepository;
import io.sclera.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.ConditionsRepository;
import io.sclera.dto.touchscreen.SensorDTO;
import io.sclera.rabbitmq.RabbitmqService;
import io.sclera.sockets.SocketService;
import io.sclera.utils.ConditionUtils;

import javax.servlet.http.HttpServletRequest;

@Service
public class ConditionsService {

    private static final Logger log = LoggerFactory.getLogger(ConditionsService.class);

    @Autowired
    SocketService sockertService;

    @Autowired
    ConditionsRepository conditionsRepository;

    @Autowired
    LorawanService lorawanService;

    @Autowired
    BacnetService bacnetService;

    @Autowired
    SnmpService snmpService;

    @Autowired
    DisruptiveService disruptiveService;

    @Autowired
    HistoryService historyService;

    @Autowired
    AlertService alertService;

    @Autowired
    DeviceService deviceService;

    @Autowired
    MyDevicesService myDeviceService;

    @Autowired
    ConditionUtils conditionUtils;


    @Autowired
    RabbitmqService rabbitmqService;

    @Autowired
    MonnitService monnitService;

    @Autowired
    PelicanService pelicanService;

    @Autowired
    KNXService knxService;

    @Autowired
    MeasuringInstrumentService measuringInstrumentService;

    @Autowired
    DaintreeService daintreeService;

    @Autowired
    AlertProfileService alertProfileService;

    @Autowired
    EcobeeService ecobeeService;


    @Autowired
    ModbusService modbusService;

    @Autowired
    IOCService iocService;


    @Autowired
    JobSchedulerService jobSchedulerService;

    @Autowired
    ScheduledJobRepository scheduledJobRepository;

    @Autowired
    AlertDowntimeScheduleService alertDowntimeScheduleService;

    @Autowired
    SiemensService siemensService;


    public void upsertConditions(String username, String vdmsid, String dockername, String conditionGroup, Set<ConditionsDTO> conditions, HttpServletRequest httpServletRequest) {
        for (ConditionsDTO condition : conditions) {
            try {
                if (conditionsRepository.conditionById(condition.getId()) != 0) {

                    ConditionsDTO conditionAlertCountDetails = conditionsRepository.getConditionAlertCountDetails(condition.getId());

                    condition.setLast_alerted(conditionAlertCountDetails.getLast_alerted());

                    condition.setAlert_count(conditionAlertCountDetails.getAlert_count());

                    if (condition.getSecond_value() != conditionAlertCountDetails.getSecond_value()) {
                        condition.setAlert_count(0);
                        condition.setLast_alerted(false);
                        conditionsRepository.updateLastAlertedTimestamp(condition.getId());
                    }

                    if ((conditionAlertCountDetails.getAlert_count_enabled() != condition.getAlert_count_enabled()) || (conditionAlertCountDetails.getMax_alert_count() != condition.getMax_alert_count()) ||
                            (!condition.getAlert_condition().equals(conditionAlertCountDetails.getAlert_condition())) || (!condition.getValue().equals(conditionAlertCountDetails.getValue())) ||
                            (!(condition.getShow_alert().equals(conditionAlertCountDetails.getShow_alert()))) || (conditionAlertCountDetails.getSchedule() != condition.getSchedule()) ||
                            (!conditionAlertCountDetails.getStart_time().equals(condition.getStart_time()) || (!conditionAlertCountDetails.getEnd_time().equals(condition.getEnd_time()))) ||
                            (conditionAlertCountDetails.getSchedule_conditions() == null && condition.getSchedule_conditions() != null) ||
                            (conditionAlertCountDetails.getSchedule_conditions() != null && condition.getSchedule_conditions() == null)) {
                        condition.setAlert_count(0);
                        condition.setLast_alerted(false);
                        conditionsRepository.updateLastAlertedTimestamp(condition.getId());
                    }

                    if ((conditionAlertCountDetails.getSchedule_conditions() != null && condition.getSchedule_conditions() != null)) {
                        if (!(conditionAlertCountDetails.getSchedule_conditions().equals(condition.getSchedule_conditions()))) {
                            condition.setAlert_count(0);
                            condition.setLast_alerted(false);
                            conditionsRepository.updateLastAlertedTimestamp(condition.getId());
                        }
                    }

                    if ((condition.getAlert_count_time() != null)) {
                        if ((!(condition.getAlert_count_time().equals(conditionAlertCountDetails.getAlert_count_time())))) {
                            condition.setAlert_count(0);
                            condition.setLast_alerted(false);
                            conditionsRepository.updateLastAlertedTimestamp(condition.getId());

                        }
                    }


                    if ((conditionAlertCountDetails.getAlert_count_time() != null)) {
                        if (condition.getAlert_count_time() == null) {
                            conditionsRepository.updateLastAlertedTimestamp(condition.getId());
                        }
                        if (!(conditionAlertCountDetails.getAlert_count_time().equals(condition.getAlert_count_time()))) {
                            condition.setAlert_count(0);
                            condition.setLast_alerted(false);
                            conditionsRepository.updateLastAlertedTimestamp(condition.getId());
                        }
                    }


                    if (conditionAlertCountDetails.getAlert_time() != null) {
                        if (!condition.getShow_alert().equals(conditionAlertCountDetails.getShow_alert())) {
                            this.deleteSensorAlertJob(condition.getId());

                            condition.setLast_alerted(false);
                        }


                        if (condition.getAlert_time() != null) {
                            if ((!(condition.getAlert_time().equals(conditionAlertCountDetails.getAlert_time()))) || (!condition.getAlert_condition().equals(conditionAlertCountDetails.getAlert_condition()))
                                    || (!condition.getValue().equals(conditionAlertCountDetails.getValue())) || (conditionAlertCountDetails.getSchedule() != condition.getSchedule()) ||
                                    (!conditionAlertCountDetails.getStart_time().equals(condition.getStart_time()) || (!conditionAlertCountDetails.getEnd_time().equals(condition.getEnd_time())))) {
                                ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(condition.getId());
                                if (scheduledJobDTO != null) {
                                    this.replaceSensorAlertJob("replace", condition, conditionGroup, scheduledJobDTO.getId());
                                } else {
                                    this.scheduleSensorAlertJob("add", condition, conditionGroup);
                                }
                            }
                        } else {
                            this.deleteSensorAlertJob(condition.getId());
                            condition.setLast_alerted(false);
                        }

                    } else if (conditionAlertCountDetails.getAlert_time() == null && condition.getAlert_time() != null) {
                        condition.setLast_alerted(false);
                    }

                    conditionsRepository.updateCondition(condition.getId(), condition.getName(), condition.getValue(), condition.getSecond_value(), condition.getAlert_message(),
                            condition.getStart_time(), condition.getEnd_time(), condition.getSchedule(), condition.getSchedule_conditions(), condition.getMax_alert_count(), condition.getAlert_count_enabled(), condition.getAlert_count(),
                            condition.getAlert_condition(), condition.getShow_alert(), condition.getShow_alert_message_as_value(), condition.getBacnet_device_id(),
                            condition.getBacnet_object_id(), condition.getLorawan_sensor_id(), condition.getLorawan_sensor_attributes_name(),
                            condition.getSnmp_device_id(), condition.getDisruptive_sensor_id(), condition.getMy_devices_sensor_id(),
                            condition.getMy_devices_sensor_attributes_name(), condition.getMonnit_sensor_id(), condition.getPelican_sensor_id(),
                            condition.getPelican_sensor_attributes_name(), condition.getKnx_group_address(), condition.getKnx_device_address(),
                            condition.getSnmp_device_configuration_id(), condition.getSnmp_object_oid(), condition.getMeasuring_instrument_id(),
                            condition.getAlert_time(), condition.getDaintree_device_id(), condition.getDaintree_point_id(), condition.getAlert_profile_id(),
                            condition.getEcobee_sensor_id(), condition.getEcobee_sensor_attributes_name(), condition.getModbus_register_id(), condition.getPriority(), condition.getLast_alerted(), condition.getAlert_count_time(), condition.getEnable_threshold_line_onchart(), condition.getColor_of_threshold_line_onchart());
                    log.info("endpoint: {}, upsertConditions, Params: :conditionAlertCountDetails:{}", httpServletRequest.getRequestURI(), conditionAlertCountDetails);
                } else {
                    if (condition.getId() == null) {
                        String condition_id = Generators.timeBasedGenerator().generate().toString();
                        condition.setId(condition_id);

                        conditionsRepository.addCondition(condition.getId(), condition.getName(), condition.getValue(), condition.getSecond_value(), condition.getAlert_message(),
                                condition.getStart_time(), condition.getEnd_time(), condition.getSchedule(), condition.getSchedule_conditions(), condition.getMax_alert_count(), condition.getAlert_count_enabled(),
                                condition.getAlert_condition(), false, condition.getShow_alert(), condition.getShow_alert_message_as_value(), condition.getBacnet_device_id(),
                                condition.getBacnet_object_id(), condition.getLorawan_sensor_id(), condition.getLorawan_sensor_attributes_name(),
                                condition.getSnmp_device_id(), condition.getDisruptive_sensor_id(), condition.getMy_devices_sensor_id(),
                                condition.getMy_devices_sensor_attributes_name(), condition.getMonnit_sensor_id(), condition.getPelican_sensor_id(),
                                condition.getPelican_sensor_attributes_name(), condition.getKnx_group_address(), condition.getKnx_device_address(),
                                condition.getSnmp_device_configuration_id(), condition.getSnmp_object_oid(), condition.getMeasuring_instrument_id(),
                                condition.getAlert_time(), condition.getDaintree_device_id(), condition.getDaintree_point_id(), condition.getAlert_profile_id(),
                                condition.getEcobee_sensor_id(), condition.getEcobee_sensor_attributes_name(), condition.getModbus_register_id(), condition.getPriority(), false, condition.getAlert_count_time(), condition.getEnable_threshold_line_onchart(), condition.getColor_of_threshold_line_onchart());
                    }
                }
                updateConditionAlertOnUpsert("", conditionGroup, condition);
                log.info("endpoint: {}, upsertConditions,  Params: :condition:{}, ", httpServletRequest.getRequestURI(), condition);
            } catch (Exception e) {
                log.error("Exception in UPDATE/ADD Conditions, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
            }
        }
    }


    public void updateConditionAlertOnUpsert(String conditionType, String conditionGroup, ConditionsDTO condition) {
        String id, sub_id;
        String value = condition.getCurrent_value();

        switch (conditionGroup) {
            case "bacnet": {
                id = condition.getBacnet_device_id();
                sub_id = condition.getBacnet_object_id();
                break;
            }
            case "lorawan": {
                id = condition.getLorawan_sensor_id();
                sub_id = condition.getLorawan_sensor_attributes_name();
                break;
            }
            case "snmp": {
                id = condition.getSnmp_device_id();
                sub_id = "";
                break;
            }
            case "disruptive": {
                id = condition.getDisruptive_sensor_id();
                sub_id = "";
                break;
            }
            case "my_devices": {
                id = condition.getMy_devices_sensor_id();
                sub_id = condition.getMy_devices_sensor_attributes_name();
                break;
            }
            case "monnit": {
                id = condition.getMonnit_sensor_id();
                sub_id = "";
                break;
            }
            case "pelican": {
                id = condition.getPelican_sensor_id();
                sub_id = condition.getPelican_sensor_attributes_name();
                break;
            }
            case "knx": {
                id = condition.getKnx_group_address();
                sub_id = condition.getKnx_device_address();
                break;
            }
            case "snmp_object": {
                id = condition.getSnmp_object_oid();
                sub_id = condition.getSnmp_device_configuration_id();
                break;
            }
            case "measuring_instrument": {
                id = condition.getMeasuring_instrument_id();
                sub_id = "";
                break;
            }
            case "daintree": {
                id = condition.getDaintree_device_id();
                sub_id = condition.getDaintree_point_id();
                break;
            }
            case "ecobee": {
                id = condition.getEcobee_sensor_id();
                sub_id = condition.getEcobee_sensor_attributes_name();
                break;
            }
            case "modbus": {
                id = condition.getModbus_register_id();
                sub_id = "";
                break;
            }
            default:
                return;
        }

//        updateConditionAlert(conditionGroup, id, sub_id, value, conditionType);
        updateConditionAlert(conditionGroup, id, sub_id, value, conditionType, "update");
    }


    public void updateConditionAlert(String conditionGroup, String id, String sub_id, String value, String conditionType, String type) {

        String bacnet_device_id = "null";
        String bacnet_object_id = "null";
        String lorawan_sensor_id = "null";
        String lorawan_sensor_attributes_name = "null";
        String snmp_device_id = "null";
        String disruptive_sensor_id = "null";
        String my_devices_sensor_id = "null";
        String my_devices_sensor_attributes_name = "null";
        String monnit_sensor_id = "null";
        String pelican_sensor_id = "null";
        String pelican_sensor_attributes_name = "null";
        String knx_group_address = "null";
        String knx_device_address = "null";
        String snmp_device_configuration_id = "null";
        String snmp_object_oid = "null";
        String measuring_instrument_id = "null";
        String daintree_device_id = "null";
        String daintree_point_id = "null";
        String ecobee_sensor_id = "null";
        String ecobee_sensor_attributes_name = "null";
        String modbus_register_id = "null";

        switch (conditionGroup) {
            case "bacnet": {
                bacnet_device_id = id;   // primary_id
                bacnet_object_id = sub_id;  // secondary_id
                break;
            }
            case "lorawan": {
                System.out.println("testing lorawan");
                lorawan_sensor_id = id;  // primary_id
                lorawan_sensor_attributes_name = sub_id;  // secondary_id
                break;
            }
            case "snmp": {
                snmp_device_id = id;  // primary_id
                break;
            }
            case "disruptive": {
                disruptive_sensor_id = id;  // primary_id
                break;
            }
            case "my_devices": {
                my_devices_sensor_id = id;  // primary_id
                my_devices_sensor_attributes_name = sub_id; // secondary_id
                break;
            }
            case "monnit": {
                monnit_sensor_id = id;  // primary_id
                break;
            }
            case "pelican": {
                pelican_sensor_id = id;  // primary_id
                pelican_sensor_attributes_name = sub_id;  // secondary_id
                break;
            }
            case "knx": {
                knx_group_address = id;  // primary_id
                knx_device_address = sub_id;   // secondary_id
                break;
            }
            case "snmp_object": {
                snmp_object_oid = id;  // primary_id
                snmp_device_configuration_id = sub_id;  // secondary_id
                break;
            }
            case "measuring_instrument": {
                measuring_instrument_id = id; //primary_id
                break;
            }
            case "daintree": {
                daintree_device_id = id; //primary_id
                daintree_point_id = sub_id; //secondary_id
                break;
            }
            case "ecobee": {
                ecobee_sensor_id = id;  // primary_id
                ecobee_sensor_attributes_name = sub_id;  // secondary_id
                break;
            }
            case "modbus": {
                modbus_register_id = id; //primary_id
                break;
            }
            default:
                return;
        }


        Set<ConditionsDTO> conditions = conditionsRepository.getConditionsById(bacnet_device_id, bacnet_object_id, lorawan_sensor_id,
                lorawan_sensor_attributes_name, snmp_device_id, disruptive_sensor_id, my_devices_sensor_id, my_devices_sensor_attributes_name,
                monnit_sensor_id, pelican_sensor_id, pelican_sensor_attributes_name, knx_group_address, knx_device_address,
                snmp_device_configuration_id, snmp_object_oid, measuring_instrument_id, daintree_device_id, daintree_point_id, ecobee_sensor_id, ecobee_sensor_attributes_name, modbus_register_id);


        System.out.println(conditions);

        Boolean oldAlert = false;
        Boolean newAlert = false;
        String user_data_value = "";
        String alert_message = "";
        Boolean alerted = false; // alerted variable is used to check whether sensor is cooldown or not


        for (ConditionsDTO condition : conditions) {


            if (condition.getLast_alerted() != null) {
                alerted = condition.getLast_alerted();
            }


            System.out.println("**************reached time based alert conditions before *************\n condition.getLast_alerted_timestamp() " +
                    condition.getLast_alerted_timestamp() + " \n   condition alert count " + condition.getAlert_count());

            BigInteger currentTimestamp = BigInteger.valueOf(System.currentTimeMillis());

            Boolean checkCondition = true;
            if (condition.getAlert()) {
                oldAlert = true;
            }
            condition.setAlert(false);


            if (condition.getSchedule() != null && condition.getSchedule() == 1) {
                if (condition.getSchedule_conditions() == null) {
                    System.out.println("Start Time " + condition.getStart_time() + "    End Time " + condition.getEnd_time());
                    checkCondition = conditionUtils.verifyCurrentSystemTimeWithinScheduledTime(condition.getStart_time(), condition.getEnd_time());
                    System.out.println("checkCondition inside a " + checkCondition);
                } else {
                    System.out.println("Start Time " + condition.getStart_time() + "    End Time " + condition.getEnd_time());
                    JSONObject scheduleCondition = JSONObject.parseObject(condition.getSchedule_conditions());
                    if (conditionUtils.verifyDayOfWeek(scheduleCondition)) {
                        checkCondition = conditionUtils.verifyCurrentSystemTimeWithinScheduledTime(condition.getStart_time(), condition.getEnd_time());
                        System.out.println("checkCondition inside b " + checkCondition);
                    } else {
                        checkCondition = false;
                        System.out.println("checkCondition inside b " + checkCondition);
                    }
                }
            }

            if (checkCondition) {

                switch (condition.getAlert_condition()) {

                    case "lt": {

                        if (value != null && !(value.equals("")) && condition.getValue() != null && !(condition.getValue().equals(""))) {
                            if (Float.parseFloat(value) <= Float.parseFloat(condition.getValue())) {
                                if (condition.getShow_alert() || condition.getShow_alert_message_as_value()) {
                                    if (condition.getAlert_count_enabled() == 1) {

                                        if (type.equals("sync")) {
                                            if (condition.getAlert_count_time() == null) {

                                                if (alerted == false) {
                                                    condition.setAlert_count(condition.getAlert_count() + 1);
                                                    condition.setLast_alerted(true);
                                                }
                                            } else {
                                                if (condition.getLast_alerted_timestamp() != null) {
                                                    Integer timeDifference = (currentTimestamp.subtract(condition.getLast_alerted_timestamp())).intValue() / 1000;
                                                    if (timeDifference > condition.getAlert_count_time()) {
                                                        condition.setAlert_count(1);
                                                        condition.setLast_alerted_timestamp(currentTimestamp);
                                                        condition.setLast_alerted(true);

                                                    } else {
                                                        if (alerted == false) {
                                                            condition.setAlert_count(condition.getAlert_count() + 1);
                                                            condition.setLast_alerted(true);
                                                        }
                                                    }
                                                } else {
                                                    if (alerted == false) {
                                                        condition.setAlert_count(1);
                                                        condition.setLast_alerted_timestamp(currentTimestamp);
                                                        condition.setLast_alerted(true);
                                                    }
                                                }
                                            }
                                        }

                                        if (condition.getMax_alert_count() != null && (condition.getAlert_count() >= condition.getMax_alert_count())) {
                                            if (condition.getShow_alert_message_as_value()) {
                                                user_data_value = condition.getAlert_message();
                                            }

                                            if (condition.getShow_alert()) {
                                                condition.setAlert(true);
                                                condition.setLast_alerted(true);
                                            }
                                        }

                                    } else {
                                        if (condition.getShow_alert()) {
                                            condition.setAlert(true);
                                            condition.setLast_alerted(true);

                                            if (condition.getAlert_time() != null) {
                                                ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(condition.getId());
                                                if (scheduledJobDTO == null && !alerted) {
                                                    this.scheduleSensorAlertJob("add", condition, conditionGroup);
                                                }
                                            }
                                        }

                                        if (condition.getShow_alert_message_as_value()) {
                                            user_data_value = condition.getAlert_message();
                                        }
                                    }
                                }

                            } else {

                                if (condition.getAlert_time() != null && condition.getAlert_count_enabled() == 0) {
                                    this.deleteSensorAlertJob(condition.getId());
                                }
                                condition.setLast_alerted(false);

                            }
                        }
                        break;
                    }
                    case "gt": {
                        if (value != null && !(value.equals("")) && condition.getValue() != null && !(condition.getValue().equals(""))) {
                            if (Float.parseFloat(value) >= Float.parseFloat(condition.getValue())) {
                                if (condition.getShow_alert() || condition.getShow_alert_message_as_value()) {
                                    if (condition.getAlert_count_enabled() == 1) {

                                        if (type.equals("sync")) {

                                            if (condition.getAlert_count_time() == null) {

                                                if (alerted == false) {

                                                    condition.setAlert_count(condition.getAlert_count() + 1);
                                                    condition.setLast_alerted(true);
                                                }
                                            } else {
                                                if (condition.getLast_alerted_timestamp() != null) {
                                                    Integer timeDifference = (currentTimestamp.subtract(condition.getLast_alerted_timestamp())).intValue() / 1000;
                                                    if (timeDifference > condition.getAlert_count_time()) {
                                                        condition.setAlert_count(1);
                                                        condition.setLast_alerted_timestamp(currentTimestamp);
                                                        condition.setLast_alerted(true);

                                                    } else {
                                                        if (alerted == false) {
                                                            condition.setAlert_count(condition.getAlert_count() + 1);
                                                            condition.setLast_alerted(true);
                                                        }
                                                    }
                                                } else {
                                                    if (alerted == false) {
                                                        condition.setAlert_count(1);
                                                        condition.setLast_alerted_timestamp(currentTimestamp);
                                                        condition.setLast_alerted(true);
                                                    }
                                                }
                                            }
                                        }

                                        if ((condition.getAlert_count() >= condition.getMax_alert_count())) {
                                            if (condition.getShow_alert_message_as_value()) {
                                                user_data_value = condition.getAlert_message();
                                            }

                                            if (condition.getShow_alert()) {
                                                condition.setAlert(true);
                                                condition.setLast_alerted(true);
                                            }
                                        }

                                    } else {
                                        if (condition.getShow_alert()) {
                                            condition.setAlert(true);
                                            condition.setLast_alerted(true);

                                            if (condition.getAlert_time() != null) {
                                                ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(condition.getId());
                                                if (scheduledJobDTO == null && !alerted) {
                                                    this.scheduleSensorAlertJob("add", condition, conditionGroup);
                                                }
                                            }
                                        }

                                        if (condition.getShow_alert_message_as_value()) {
                                            user_data_value = condition.getAlert_message();
                                        }
                                    }
                                }
                            } else {
                                if (condition.getAlert_time() != null && condition.getAlert_count_enabled() == 0) {
                                    this.deleteSensorAlertJob(condition.getId());
                                }
                                condition.setLast_alerted(false);
                            }
                        }
                        break;
                    }
                    case "eq": {

                        if (value != null && !(value.equals("")) && condition.getValue() != null && !(condition.getValue().equals(""))) {

                            if (value.equalsIgnoreCase(condition.getValue())) {

                                if (condition.getShow_alert() || condition.getShow_alert_message_as_value()) {
                                    if (condition.getAlert_count_enabled() == 1) {
                                        if (type.equals("sync")) {
                                            if (condition.getAlert_count_time() == null) {

                                                if (!alerted) {
                                                    condition.setAlert_count(condition.getAlert_count() + 1);
                                                    condition.setLast_alerted(true);

                                                }
                                            } else {
                                                if (condition.getLast_alerted_timestamp() != null) {
                                                    Integer timeDifference = (currentTimestamp.subtract(condition.getLast_alerted_timestamp())).intValue() / 1000;
                                                    if (timeDifference > condition.getAlert_count_time()) {
                                                        condition.setAlert_count(1);
                                                        condition.setLast_alerted_timestamp(currentTimestamp);
                                                        condition.setLast_alerted(true);

                                                    } else {
                                                        if (alerted == false) {
                                                            condition.setAlert_count(condition.getAlert_count() + 1);
                                                            condition.setLast_alerted(true);
                                                        }
                                                    }
                                                } else {
                                                    if (alerted == false) {
                                                        condition.setAlert_count(1);
                                                        condition.setLast_alerted_timestamp(currentTimestamp);
                                                        condition.setLast_alerted(true);
                                                    }
                                                }
                                            }
                                        }


                                        if ((condition.getAlert_count() >= condition.getMax_alert_count())) {
                                            if (condition.getShow_alert_message_as_value()) {
                                                user_data_value = condition.getAlert_message();
                                            }

                                            if (condition.getShow_alert()) {
                                                condition.setAlert(true);
                                                condition.setLast_alerted(true);
                                            }
                                        }

                                    } else {

                                        if (condition.getShow_alert()) {
                                            condition.setAlert(true);
                                            condition.setLast_alerted(true);

                                            if (condition.getAlert_time() != null) {
                                                ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(condition.getId());
                                                if (scheduledJobDTO == null && !alerted) {
                                                    this.scheduleSensorAlertJob("add", condition, conditionGroup);
                                                }
                                            }
                                        }

                                        if (condition.getShow_alert_message_as_value()) {
                                            user_data_value = condition.getAlert_message();
                                        }
                                    }
                                }

                            } else {
                                if (condition.getAlert_time() != null && condition.getAlert_count_enabled() == 0) {

                                    this.deleteSensorAlertJob(condition.getId());
                                }
                                condition.setLast_alerted(false);
                            }
                        }
                        break;
                    }
                    case "neq": {
                        if (value != null && !(value.equals("")) && condition.getValue() != null && !(condition.getValue().equals(""))) {
                            if (!(value.equalsIgnoreCase(condition.getValue()))) {
                                if (condition.getShow_alert() || condition.getShow_alert_message_as_value()) {
                                    if (condition.getAlert_count_enabled() != null && condition.getAlert_count_enabled() == 1) {

                                        if (type.equals("sync")) {
                                            if (condition.getAlert_count_time() == null) {
                                                if (alerted == false) {
                                                    condition.setAlert_count(condition.getAlert_count() + 1);
                                                    condition.setLast_alerted(true);
                                                }
                                            } else {
                                                if (condition.getLast_alerted_timestamp() != null) {
                                                    Integer timeDifference = (currentTimestamp.subtract(condition.getLast_alerted_timestamp())).intValue() / 1000;
                                                    if (timeDifference > condition.getAlert_count_time()) {
                                                        condition.setAlert_count(1);
                                                        condition.setLast_alerted_timestamp(currentTimestamp);
                                                        condition.setLast_alerted(true);

                                                    } else {
                                                        if (alerted == false) {
                                                            condition.setAlert_count(condition.getAlert_count() + 1);
                                                            condition.setLast_alerted(true);
                                                        }
                                                    }
                                                } else {
                                                    if (alerted == false) {
                                                        condition.setAlert_count(1);
                                                        condition.setLast_alerted_timestamp(currentTimestamp);
                                                        condition.setLast_alerted(true);
                                                    }
                                                }
                                            }
                                        }


                                        if ((condition.getAlert_count() >= condition.getMax_alert_count())) {


                                            if (condition.getShow_alert_message_as_value()) {
                                                user_data_value = condition.getAlert_message();
                                            }

                                            if (condition.getShow_alert()) {
                                                condition.setAlert(true);
                                                condition.setLast_alerted(true);
                                            }
                                        }

                                    } else {
                                        if (condition.getShow_alert()) {
                                            condition.setAlert(true);
                                            condition.setLast_alerted(true);

                                            if (condition.getAlert_time() != null) {

                                                ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(condition.getId());
                                                if (scheduledJobDTO == null && !alerted) {
                                                    this.scheduleSensorAlertJob("add", condition, conditionGroup);
                                                }
                                            }
                                        }

                                        if (condition.getShow_alert_message_as_value()) {
                                            user_data_value = condition.getAlert_message();
                                        }
                                    }
                                }
                            } else {
                                if (condition.getAlert_time() != null && condition.getAlert_count_enabled() == 0) {
                                    this.deleteSensorAlertJob(condition.getId());
                                }
                                condition.setLast_alerted(false);
                            }
                        }
                        break;
                    }
                    case "lt_gt": {
                        if (value != null && !(value.equals("")) && condition.getValue() != null && !(condition.getValue().equals("")) && condition.getSecond_value() != null && !(condition.getSecond_value().equals(""))) {
                            if (Float.parseFloat(value) <= Float.parseFloat(condition.getValue()) && Float.parseFloat(value) >= Float.parseFloat(condition.getSecond_value())) {
                                if (condition.getShow_alert() || condition.getShow_alert_message_as_value()) {
                                    if (condition.getAlert_count_enabled() == 1) {

                                        if (type.equals("sync")) {
                                            if (condition.getAlert_count_time() == null) {

                                                if (alerted == false) {
                                                    condition.setAlert_count(condition.getAlert_count() + 1);
                                                    condition.setLast_alerted(true);
                                                }
                                            } else {
                                                if (condition.getLast_alerted_timestamp() != null) {
                                                    Integer timeDifference = (currentTimestamp.subtract(condition.getLast_alerted_timestamp())).intValue() / 1000;
                                                    if (timeDifference > condition.getAlert_count_time()) {
                                                        condition.setAlert_count(1);
                                                        condition.setLast_alerted_timestamp(currentTimestamp);
                                                        condition.setLast_alerted(true);

                                                    } else {
                                                        if (alerted == false) {
                                                            condition.setAlert_count(condition.getAlert_count() + 1);
                                                            condition.setLast_alerted(true);
                                                        }
                                                    }
                                                } else {
                                                    if (alerted == false) {
                                                        condition.setAlert_count(1);
                                                        condition.setLast_alerted_timestamp(currentTimestamp);
                                                        condition.setLast_alerted(true);
                                                    }
                                                }
                                            }
                                        }

                                        if (condition.getMax_alert_count() != null && (condition.getAlert_count() >= condition.getMax_alert_count())) {
                                            if (condition.getShow_alert_message_as_value()) {
                                                user_data_value = condition.getAlert_message();
                                            }

                                            if (condition.getShow_alert()) {
                                                condition.setAlert(true);
                                                condition.setLast_alerted(true);
                                            }
                                        }

                                    } else {
                                        if (condition.getShow_alert()) {
                                            condition.setAlert(true);
                                            condition.setLast_alerted(true);

                                            if (condition.getAlert_time() != null) {
                                                ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(condition.getId());
                                                if (scheduledJobDTO == null && !alerted) {
                                                    this.scheduleSensorAlertJob("add", condition, conditionGroup);
                                                }
                                            }
                                        }

                                        if (condition.getShow_alert_message_as_value()) {
                                            user_data_value = condition.getAlert_message();
                                        }
                                    }
                                }

                            } else {

                                if (condition.getAlert_time() != null && condition.getAlert_count_enabled() == 0) {
                                    this.deleteSensorAlertJob(condition.getId());
                                }
                                condition.setLast_alerted(false);

                            }
                        }
                        break;
                    }

                    default:
                        break;
                }

                if (condition.getAlert_count_enabled() == 1 && (condition.getAlert_count() >= condition.getMax_alert_count() && condition.getLast_alerted() == true)) {
                    if (condition.getShow_alert()) {
                        condition.setAlert(true);
                    }

                    if (condition.getShow_alert_message_as_value()) {
                        user_data_value = condition.getAlert_message();
                    }
                }
            } else {
                if (condition.getAlert_time() != null) {
                    this.deleteSensorAlertJob(condition.getId());
                }
                condition.setLast_alerted(false);
            }


            System.out.println("**************reached time based alert conditions after *************\n currentTimestamp " +
                    currentTimestamp + "     condition.getLast_alerted_timestamp() " + condition.getLast_alerted_timestamp());

            System.out.println("Checking Before Updating the value of Last Altered..............");
            System.out.println("Last alerted:" + condition.getLast_alerted());

            conditionsRepository.updateConditionAlert(condition.getId(), condition.getAlert(), condition.getAlert_count(),
                    condition.getLast_alerted_timestamp(), condition.getLast_alerted());

            System.out.println("condition count" + condition.getAlert_count());

            if (condition.getAlert()) {
                newAlert = true;

                if (alert_message.equals("")) {
                    alert_message = condition.getAlert_message();
                } else {
                    alert_message = alert_message + ", " + condition.getAlert_message();
                }

                try {
                    if (condition.getAlert_profile_id() != null) {
                        if (alerted == false && condition.getLast_alerted() == true) {
                            if (condition.getAlert_count_enabled() != 1 && condition.getAlert_time() == null) {
                                System.out.println("----- Checking to send mail for normal condition ----");
                                System.out.println("Last alerted " + condition.getLast_alerted() + "Alerted " + alerted);
                                System.out.println("Entered here since alert profile not null" + condition.getAlert_profile_id());
                                this.sendAlertInfo(conditionGroup, condition, alert_message, null);
                            } else if (condition.getAlert_count() == condition.getMax_alert_count() && condition.getAlert_count_enabled() == 1) {
                                System.out.println("----- Checking to send mail for count based condition ----");
                                System.out.println("Entered here since alert profile not null" + condition.getAlert_profile_id());
                                this.sendAlertInfo(conditionGroup, condition, alert_message, null);
                            }

                        }
                    }

                } catch (Exception e) {
                    System.out.println("Error sending  alert info " + e);
                    System.out.println(e);
                }

            }


        }
        System.out.println("user data value " + user_data_value);


        switch (conditionGroup) {
            case "bacnet": {
                System.out.println("inside old and new " + oldAlert + "      " + newAlert);
                if (oldAlert != newAlert) {
                    System.out.println("inside old and new mismatch " + newAlert);
//                    bacnetService.updateBacnetObjectAlert(bacnet_device_id, bacnet_object_id, newAlert);

                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    if (newAlert) {
                        try {
                            this.sendBacnetAlertInfo(bacnet_device_id, bacnet_object_id, alert_message);

                        } catch (Exception e) {
                            System.out.println("Error sending bacnet alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {
                    if (conditionType.equals("delete")) {
//                        bacnetService.updateBacnetObjectAlert(bacnet_device_id, bacnet_object_id, newAlert);
                    }
                }
//                bacnetService.updateBacnetObjectUserDataValue(bacnet_device_id, bacnet_object_id, user_data_value);

                //update device bacnet status
                deviceService.updateDeviceBacnetStatus(bacnet_device_id, bacnet_object_id);
                break;
            }
            case "lorawan": {
                if (oldAlert != newAlert) {
//                    lorawanService.updateLorawanSensorAttributesAlert(lorawan_sensor_id, lorawan_sensor_attributes_name, newAlert);

                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    if (newAlert) {
                        try {
                            this.sendLorawanAlertInfo(lorawan_sensor_id, lorawan_sensor_attributes_name, alert_message);
                        } catch (Exception e) {
                            System.out.println("Error sending lorawan alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {
                    if (conditionType.equals("delete")) {
//                        lorawanService.updateLorawanSensorAttributesAlert(lorawan_sensor_id, lorawan_sensor_attributes_name, newAlert);
                    }
                }
//                lorawanService.updateLorawanSensorAttributesUserDataValue(lorawan_sensor_id, lorawan_sensor_attributes_name, user_data_value);

                //update lorawan sensor alert
//                lorawanService.updateLorawanSensorAlert(lorawan_sensor_id);

                //update device lorawan status
                deviceService.updateDeviceLorawanStatus(lorawan_sensor_id);
                break;
            }
            case "snmp": {
                if (oldAlert != newAlert) {
//                    snmpService.updateSnmpDeviceAlert(snmp_device_id, newAlert);
                    //					if(newAlert){
                    //						//insert snmp alert into history
                    //						historyService.insertSnmpAlertHistory(snmp_device_id, value, user_data_value, alert_message);
                    //					}
                } else {
                    if (conditionType.equals("delete")) {
//                        snmpService.updateSnmpDeviceAlert(snmp_device_id, newAlert);
                    }
                }
//                snmpService.updateSnmpDeviceUserDataValue(snmp_device_id, user_data_value);

                //update device snmp status
                deviceService.updateDeviceSnmpStatus(snmp_device_id);
                break;
            }
            case "disruptive": {
                if (oldAlert != newAlert) {
//                    disruptiveService.updateDisruptiveSensorAlert(disruptive_sensor_id, newAlert);

                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                    if (newAlert) {
                        try {
                            this.sendDisruptiveAlertInfo(disruptive_sensor_id, alert_message);
                        } catch (Exception e) {
                            System.out.println("Error sending diruptive alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {
                    if (conditionType.equals("delete")) {
//                        disruptiveService.updateDisruptiveSensorAlert(disruptive_sensor_id, newAlert);
                    }
                }
//                disruptiveService.updateDisruptiveSensorUserDataValue(disruptive_sensor_id, user_data_value);

                //update device disruptive status
                deviceService.updateDeviceDisruptiveStatus(disruptive_sensor_id);
                break;
            }
            case "my_devices": {
                if (oldAlert != newAlert) {
//                    myDeviceService.updateMyDevicesSensorAttributesAlert(my_devices_sensor_id, my_devices_sensor_attributes_name, newAlert);

                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    if (newAlert) {

                        try {
                            this.sendMyDevicesAlertInfo(my_devices_sensor_id, my_devices_sensor_attributes_name, alert_message);
                        } catch (Exception e) {
                            System.out.println("Error sending mydevices alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {
                    if (conditionType.equals("delete")) {
//                        myDeviceService.updateMyDevicesSensorAttributesAlert(my_devices_sensor_id, my_devices_sensor_attributes_name, newAlert);
                    }
                }

//                myDeviceService.updateMyDevicesSensorAttributesUserDataValue(my_devices_sensor_id, my_devices_sensor_attributes_name, user_data_value);
//                myDeviceService.updateMyDevicesSensorAlert(my_devices_sensor_id);
                //update device my devices status
                deviceService.updateDeviceMyDevicesStatus(my_devices_sensor_id);
                break;
            }
            case "monnit": {
                if (oldAlert != newAlert) {
//                    monnitService.updateMonnitSensorAlert(monnit_sensor_id, newAlert);

                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        System.out.println("Error updating monnit sensor alert count " + e);
                        System.out.println(e);
                    }
                    if (newAlert) {

                        try {
                            this.sendMonnitAlertInfo(monnit_sensor_id, alert_message);
                        } catch (Exception e) {
                            System.out.println("Error sending monnit alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {
                    if (conditionType.equals("delete")) {
//                        monnitService.updateMonnitSensorAlert(monnit_sensor_id, newAlert);
                    }

                }
//                monnitService.updateMonnitSensorUserDataValue(monnit_sensor_id, user_data_value);

                //update device disruptive status
                deviceService.updateDeviceMonnitStatus(monnit_sensor_id);
                break;
            }
            case "pelican": {

                if (oldAlert != newAlert) {
//                    pelicanService.updatePelicanSensorAttributesAlert(pelican_sensor_id, pelican_sensor_attributes_name, newAlert);

                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    if (newAlert) {


                        try {
                            this.sendPelicanAlertInfo(pelican_sensor_id, pelican_sensor_attributes_name, alert_message);
                        } catch (Exception e) {
                            System.out.println("Error sending pelican alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {
                    if (conditionType.equals("delete")) {
//                        pelicanService.updatePelicanSensorAttributesAlert(pelican_sensor_id, pelican_sensor_attributes_name, newAlert);
                    }
                }

//                pelicanService.updatePelicanSensorAttributesUserDataValue(pelican_sensor_id, pelican_sensor_attributes_name, user_data_value);
//                pelicanService.updatePelicanSensorAlert(pelican_sensor_id);

                //update device pelican status
                deviceService.updateDevicePelicanStatus(pelican_sensor_id);
                break;
            }
            case "knx": {

                if (oldAlert != newAlert) {
//                    knxService.updateKNXGroupAlert(knx_group_address, knx_device_address, newAlert);

                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    if (newAlert) {

                        try {
                            this.sendKNXAlertInfo(knx_device_address, knx_group_address, alert_message);
                        } catch (Exception e) {
                            System.out.println("Error sending knx alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {
                    if (conditionType.equals("delete")) {
//                        knxService.updateKNXGroupAlert(knx_group_address, knx_device_address, newAlert);
                    }
                }

//                knxService.updateKNXGroupUserDataValue(knx_device_address, knx_group_address, user_data_value);

                //update device knx status
                deviceService.updateDeviceKNXStatus(knx_device_address, knx_group_address);
                break;
            }

            case "snmp_object": {

                if (oldAlert != newAlert) {

//                    snmpService.updateSnmpObjectAlert(snmp_device_configuration_id, snmp_object_oid, newAlert);

                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    if (newAlert) {
                        try {
                            this.sendSnmpObjectAlertInfo(snmp_device_configuration_id, snmp_object_oid, alert_message);
                        } catch (Exception e) {
                            System.out.println("Error sending snmp alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {
                    if (conditionType.equals("delete")) {
//                        snmpService.updateSnmpObjectAlert(snmp_device_configuration_id, snmp_object_oid, newAlert);
                    }
                }

//                snmpService.updateSnmpObjectUserDataValue(snmp_device_configuration_id, snmp_object_oid, user_data_value);

                //update device snmp status
                deviceService.updateDeviceSnmpObjectStatus(snmp_device_configuration_id, snmp_object_oid);
                break;
            }
            case "measuring_instrument": {
                if (oldAlert != newAlert) {

                    measuringInstrumentService.updateMeasuringInstrumentSensorAlert(measuring_instrument_id, newAlert);

                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        System.out.println("Error updating measuring_instrument sensor alert count " + e);
                        System.out.println(e);
                    }
                    if (newAlert) {

                        try {
                            log.info("Sending Data to RabbitMq");
                            this.sendMeasuringInstrumentAlertInfo(measuring_instrument_id, alert_message);
                        } catch (Exception e) {
                            System.out.println("Error sending measuring_instrument alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {
                    if (conditionType.equals("delete")) {
                        measuringInstrumentService.updateMeasuringInstrumentSensorAlert(measuring_instrument_id, newAlert);
                    }

                }
                measuringInstrumentService.updateMeasuringinstrumentSensorUserDataValue(measuring_instrument_id, user_data_value);
                //update device disruptive status
                deviceService.updateDeviceMeasuringInstrumentStatus(measuring_instrument_id);
                break;
            }
            case "daintree": {
                if (oldAlert != newAlert) {

                    System.out.println("old = " + oldAlert + " new = " + newAlert);
//                    daintreeService.updateDaintreeAlert(daintree_device_id, daintree_point_id, newAlert);
                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    if (newAlert) {
                        try {
                            this.sendDaintreeAlertInfo(daintree_device_id, daintree_point_id, alert_message);
                        } catch (Exception e) {
                            System.out.println("Error sending daintree alert info " + e);
                            System.out.println(e);
                        }

                    }
                } else {
                    if (conditionType.equals("delete")) {
//                        daintreeService.updateDaintreeAlert(daintree_device_id, daintree_point_id, newAlert);
                    }
                }

//                daintreeService.updateDaintreeUserDataValue(daintree_device_id, daintree_point_id, user_data_value);
                //update device disruptive status
//                daintreeService.updateDaintreeDeviceAlert(daintree_device_id);
                deviceService.updateDeviceDainTreeStatus(daintree_device_id);
                break;
            }
            case "ecobee": {
                if (oldAlert != newAlert) {
//                    ecobeeService.updateEcobeeSensorAttributesAlert(ecobee_sensor_id, ecobee_sensor_attributes_name, newAlert);
                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    if (newAlert) {
                        try {
//                            this.sendEcobeeAlertInfo(ecobee_sensor_id, ecobee_sensor_attributes_name, alert_message);
                        } catch (Exception e) {
                            System.out.println("Error sending ecobee alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {

                    if (conditionType.equals("delete")) {
//                        ecobeeService.updateEcobeeSensorAttributesAlert(ecobee_sensor_id, ecobee_sensor_attributes_name, newAlert);
                    }
                }
//                ecobeeService.updateEcobeeSensorAttributesUserDataValue(ecobee_sensor_id, ecobee_sensor_attributes_name, user_data_value);
//                ecobeeService.updateEcobeeSensorAlert(ecobee_sensor_id);

                //update device ecobee status
                deviceService.updateDeviceEcobeeStatus(ecobee_sensor_id);
                break;
            }
            case "modbus": {
                System.out.println("inside old and new " + oldAlert + "      " + newAlert);
                if (oldAlert != newAlert) {
                    System.out.println("inside old and new mismatch " + newAlert);
//                    modbusService.updateModbusRegisterAlert(modbus_register_id, newAlert);

                    try {
//                        sockertService.sockertSensorAlertCount();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    if (newAlert) {
                        try {
//                            this.sendModbusAlertInfo(modbus_register_id, alert_message);//working on this

                        } catch (Exception e) {
                            System.out.println("Error sending modbus alert info " + e);
                            System.out.println(e);
                        }
                    }
                } else {
                    if (conditionType.equals("delete")) {
//                        modbusService.updateModbusRegisterAlert(modbus_register_id, newAlert);
                    }
                }
//                modbusService.updateModbusRegisterUserDataValue(modbus_register_id, user_data_value);

                //update device modbus status
                deviceService.updateDeviceModbusStatus(modbus_register_id);
                break;
            }
            default:
                return;
        }
    }


    public void deleteConditions(String username, String vdmsid, String dockername, String conditionGroup, Set<ConditionsDTO> conditions, HttpServletRequest httpServletRequest) {
        for (ConditionsDTO condition : conditions) {
            try {
                //			conditionsRepository.deleteConditionById(conditionId);
                if (condition != null) {
                    conditionsRepository.deleteById(condition.getId());
                    updateConditionAlertOnUpsert("delete", conditionGroup, condition);
                }
                log.info("endpoint: {}, deleteConditions,  Params: :condition:{} ", httpServletRequest.getRequestURI(), condition);
            } catch (Exception e) {
                log.error("Exception in Deleting Conditions,endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
            }
        }
    }

    public Set<ConditionsDTO> getConditions(String username, String vdmsid, String dockername, String conditionGroup, SensorDTO sensorData) {
        return getSensorConditions(conditionGroup, sensorData);
    }

    // new changes

    public Set<ConditionsDTO> getConditionsTS(String conditionGroup, SensorDTO sensorData) {
        return getSensorConditions(conditionGroup, sensorData);
    }

    public Set<ConditionsDTO> getSensorConditions(String conditionGroup, SensorDTO sensorData) {

        String bacnet_device_id = "null";
        String bacnet_object_id = "null";
        String lorawan_sensor_id = "null";
        String snmp_device_id = "null";
        String disruptive_sensor_id = "null";
        String my_devices_sensor_id = "null";
        String monnit_sensor_id = "null";
        String pelican_sensor_id = "null";
        String knx_group_address = "null";
        String knx_device_address = "null";
        String snmp_device_configuration_id = "null";
        String snmp_object_oid = "null";
        String measuring_instrument_id = "null";
        String daintree_device_id = "null";
        String ecobee_sensor_id = "null";
        String modbus_register_id = "null";

        String id = sensorData.getPrimary_id();
        String sub_id = sensorData.getSecondary_id();

        switch (conditionGroup) {
            case "bacnet": {
                bacnet_object_id = id;
                bacnet_device_id = sub_id;
                break;
            }
            case "lorawan": {
                lorawan_sensor_id = id;
                break;
            }
            case "snmp": {
                snmp_device_id = id;
                break;
            }
            case "disruptive": {
                disruptive_sensor_id = id;
                break;
            }
            case "my_devices": {
                my_devices_sensor_id = id;
                break;
            }
            case "monnit": {
                monnit_sensor_id = id;
                break;
            }
            case "pelican": {
                pelican_sensor_id = id;
                break;
            }
            case "knx": {
                knx_group_address = id;
                knx_device_address = sub_id;
                break;
            }
            case "snmp_object": {
                snmp_object_oid = id;
                snmp_device_configuration_id = sub_id;
                break;
            }
            case "measuring_instrument": {
                measuring_instrument_id = id;
                break;
            }
            case "daintree": {
                daintree_device_id = id;
                break;
            }
            case "ecobee": {
                ecobee_sensor_id = id;
                break;
            }
            case "modbus": {
                modbus_register_id = id;
                break;
            }

            default: {
                return null;
            }
        }

        Set<ConditionsDTO> conditions = conditionsRepository.getConditions(bacnet_object_id, bacnet_device_id, lorawan_sensor_id, snmp_device_id, disruptive_sensor_id, my_devices_sensor_id, monnit_sensor_id, pelican_sensor_id,
                knx_group_address, knx_device_address, snmp_device_configuration_id, snmp_object_oid, measuring_instrument_id, daintree_device_id, ecobee_sensor_id, modbus_register_id);
        for (ConditionsDTO condition : conditions) {
            if (condition.getAlert_profile_id() != null) {
                condition.setAlert_profile(alertProfileService.getAlertProfileDetailsById(null, null,
                        condition.getAlert_profile_id()));
            }
        }
        return conditions;
    }

    //remove after frontend upload
    public Set<ConditionsDTO> getConditionsFrontend(String username, String vdmsid, String dockername, String conditionGroup, String id, String sub_id) {
        String bacnet_device_id = "null";
        String bacnet_object_id = "null";
        String lorawan_sensor_id = "null";
        String snmp_device_id = "null";
        String disruptive_sensor_id = "null";
        String my_devices_sensor_id = "null";
        String monnit_sensor_id = "null";
        String pelican_sensor_id = "null";
        String knx_group_address = "null";
        String knx_device_address = "null";
        String snmp_device_configuration_id = "null";
        String snmp_object_oid = "null";
        String measuring_instrument_id = "null";
        String daintree_device_id = "null";
        String ecobee_sensor_id = "null";
        String modbus_register_id = "null";

        switch (conditionGroup) {
            case "bacnet": {
                bacnet_object_id = id;
                bacnet_device_id = sub_id;
                break;
            }
            case "lorawan": {
                lorawan_sensor_id = id;
                break;
            }
            case "snmp": {
                snmp_device_id = id;
                break;
            }
            case "disruptive": {
                disruptive_sensor_id = id;
                break;
            }
            case "my_devices": {
                my_devices_sensor_id = id;
                break;
            }
            case "monnit": {
                monnit_sensor_id = id;
                break;
            }
            case "pelican": {
                pelican_sensor_id = id;
                break;
            }
            case "knx": {
                knx_group_address = id;
                knx_device_address = sub_id;
                break;
            }
            case "snmp_object": {
                snmp_object_oid = id;
                snmp_device_configuration_id = sub_id;
                break;
            }
            case "measuring_instrument": {
                measuring_instrument_id = id;
                break;
            }
            case "daintree": {
                daintree_device_id = id;
                break;
            }
            case "ecobee": {
                ecobee_sensor_id = id;
                break;
            }
            case "modbus": {
                modbus_register_id = id;
                break;
            }
            default: {
                return null;
            }
        }

        return conditionsRepository.getConditions(bacnet_object_id, bacnet_device_id, lorawan_sensor_id, snmp_device_id, disruptive_sensor_id, my_devices_sensor_id, monnit_sensor_id, pelican_sensor_id,
                knx_group_address, knx_device_address, snmp_device_configuration_id, snmp_object_oid, measuring_instrument_id, daintree_device_id, ecobee_sensor_id, modbus_register_id);
    }


//    public void shareConditions(String username, String vdmsid, String dockername, String conditionGroup, ShareConditionsDTO shareConditions, HttpServletRequest httpServletRequest) {
//
//        Boolean checkCondition = true;
//
//        if (conditionGroup.equals("daintree")) {
//            Set<SensorDTO> sensors = this.updateDaintreeShareConditions(shareConditions);
//            if (sensors.size() > 0) {
////                shareConditions.setSensors(sensors);
//            }
//        }
//
//        for (SensorDTO sensor : shareConditions.getSensors()) {
//            try {
//                if (shareConditions.getCondition_method() != null && shareConditions.getCondition_method().equals("replace")) {
//
//                    for (ConditionsDTO condition : shareConditions.getConditions()) {
//
//                        if (conditionGroup.equals("daintree")) {
//                            String daintree_point_name = daintreeService.getDaintreePointNameById(condition.getDaintree_point_id(), condition.getDaintree_device_id());
//
//                            if (!daintree_point_name.equals(sensor.getName())) {
//                                checkCondition = false;
//                            }
//                        }
//                        if (checkCondition) {
//
//                            ConditionsDTO deleteCondition = updateShareConditionSensorIds("replace", conditionGroup, condition, sensor);
//
//                            Set<ConditionsDTO> deleteConditionsList = conditionsRepository.getConditionsById(deleteCondition.getBacnet_device_id(), deleteCondition.getBacnet_object_id(),
//                                    deleteCondition.getLorawan_sensor_id(), deleteCondition.getLorawan_sensor_attributes_name(), deleteCondition.getSnmp_device_id(),
//                                    deleteCondition.getDisruptive_sensor_id(), deleteCondition.getMy_devices_sensor_id(), deleteCondition.getMy_devices_sensor_attributes_name(),
//                                    deleteCondition.getMonnit_sensor_id(), deleteCondition.getPelican_sensor_id(), deleteCondition.getPelican_sensor_attributes_name(), deleteCondition.getKnx_group_address(),
//                                    deleteCondition.getKnx_device_address(), deleteCondition.getSnmp_device_configuration_id(), deleteCondition.getSnmp_object_oid(), deleteCondition.getMeasuring_instrument_id(),
//                                    deleteCondition.getDaintree_device_id(), deleteCondition.getDaintree_point_id(), deleteCondition.getEcobee_sensor_id(), deleteCondition.getEcobee_sensor_attributes_name(), deleteCondition.getModbus_register_id());
//
//                            System.out.println("Update Shared delete condition " + deleteConditionsList);
//
//                            if (deleteConditionsList != null) {
//                                deleteConditions(username, vdmsid, dockername, conditionGroup, deleteConditionsList, httpServletRequest);
//                            }
//                        }
//                    }
//                }
//                if (shareConditions.getCondition_method() != null && (shareConditions.getCondition_method().equals("add") || shareConditions.getCondition_method().equals("replace"))) {
//
//                    Set<ConditionsDTO> newConditionsList = new HashSet<>();
//
//                    for (ConditionsDTO condition : shareConditions.getConditions()) {
//                        if (conditionGroup.equals("daintree")) {
//                            String daintree_point_name = daintreeService.getDaintreePointNameById(condition.getDaintree_point_id(), condition.getDaintree_device_id());
//                            if (!daintree_point_name.equals(sensor.getName())) {
//                                checkCondition = false;
//                            }
//                        }
//                        if (checkCondition) {
//                            ConditionsDTO newCondition = updateShareConditionSensorIds("add", conditionGroup, condition, sensor);
//                            newConditionsList.add(newCondition);
//                        }
//                    }
//
//                    try {
//                        upsertConditions(username, vdmsid, dockername, conditionGroup, newConditionsList, httpServletRequest);
//                    } catch (Exception e) {
//                        System.out.println("Error in upsert the share the condition " + e);
//                        System.out.println(e);
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Exception in Share Conditions,endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
//            }
//        }
//
//    }

    public ConditionsDTO updateShareConditionSensorIds(String conditionMethod, String conditionGroup, ConditionsDTO condition, SensorDTO sensor) {


        String bacnet_device_id = null;
        String bacnet_object_id = null;
        String lorawan_sensor_id = null;
        String lorawan_sensor_attribute_name = null;
        String snmp_device_id = null;
        String disruptive_sensor_id = null;
        String my_devices_sensor_id = null;
        String my_devices_sensor_attributes_name = null;
        String monnit_sensor_id = null;
        String pelican_sensor_id = null;
        String pelican_sensor_attributes_name = null;
        String knx_group_address = null;
        String knx_device_address = null;
        String snmp_device_configuration_id = null;
        String snmp_object_oid = null;
        String measuring_instrument_id = null;
        String daintree_device_id = null;
        String daintree_point_id = null;
        String ecobee_sensor_id = null;
        String ecobee_sensor_attributes_name = null;
        String modbus_register_id = null;

        String current_value = null;

        if (conditionMethod != null && conditionMethod.equals("replace")) {
            bacnet_device_id = "null";
            bacnet_object_id = "null";
            lorawan_sensor_id = "null";
            lorawan_sensor_attribute_name = "null";
            snmp_device_id = "null";
            disruptive_sensor_id = "null";
            my_devices_sensor_id = "null";
            my_devices_sensor_attributes_name = "null";
            monnit_sensor_id = "null";
            pelican_sensor_id = "null";
            pelican_sensor_attributes_name = "null";
            knx_group_address = "null";
            knx_device_address = "null";
            snmp_device_configuration_id = "null";
            snmp_object_oid = "null";
            measuring_instrument_id = "null";
            daintree_device_id = "null";
            daintree_point_id = "null";
            ecobee_sensor_id = "null";
            ecobee_sensor_attributes_name = "null";
            modbus_register_id = "null";
        }

        switch (conditionGroup) {
            case "bacnet": {
                bacnet_device_id = sensor.getSecondary_id();
                bacnet_object_id = sensor.getPrimary_id();
//                current_value = bacnetService.getBacnetObjectCurrentValue(bacnet_device_id, bacnet_object_id);
                break;
            }
            case "lorawan": {
                lorawan_sensor_id = sensor.getPrimary_id();
                lorawan_sensor_attribute_name = condition.getLorawan_sensor_attributes_name();
//                current_value = lorawanService.getLorawanSensorAttributeCurrentValue(lorawan_sensor_id, lorawan_sensor_attribute_name);
                break;
            }
            case "snmp": {
                snmp_device_id = sensor.getPrimary_id();
//                current_value = snmpService.getSnmpDeviceCurrentValue(snmp_device_id);
                break;
            }
            case "disruptive": {
                disruptive_sensor_id = sensor.getPrimary_id();
//                current_value = disruptiveService.getDisruptiveSensorCurrentValue(disruptive_sensor_id);
                break;
            }
            case "my_devices": {
                my_devices_sensor_id = sensor.getPrimary_id();
                my_devices_sensor_attributes_name = condition.getMy_devices_sensor_attributes_name();
//                current_value = myDeviceService.getMyDevicesSensorAttributeCurrentValue(my_devices_sensor_id, my_devices_sensor_attributes_name);
                break;
            }
            case "monnit": {
                monnit_sensor_id = sensor.getPrimary_id();
//                current_value = monnitService.getMonnitSensorCurrentValue(monnit_sensor_id);
                break;
            }
            case "pelican": {
                pelican_sensor_id = sensor.getPrimary_id();
                pelican_sensor_attributes_name = condition.getPelican_sensor_attributes_name();
//                current_value = pelicanService.getPelicanSensorAttributeCurrentValue(pelican_sensor_id, pelican_sensor_attributes_name);
                break;
            }
            case "knx": {
                knx_group_address = sensor.getPrimary_id();
                knx_device_address = sensor.getSecondary_id();
//                current_value = knxService.getKNXCurrentValue(knx_group_address, knx_device_address);
                break;

            }
            case "snmp_object": {
                snmp_device_configuration_id = sensor.getSecondary_id();
                snmp_object_oid = sensor.getPrimary_id();
//                current_value = snmpService.getSnmpObjectCurrentValue(snmp_object_oid, snmp_device_configuration_id);
                break;
            }
            case "measuring_instrument": {
                measuring_instrument_id = sensor.getPrimary_id();
//                current_value = measuringInstrumentService.getMeasuringInstrumentSensorCurrentValue(measuring_instrument_id);
                break;
            }
            case "daintree": {
                daintree_point_id = sensor.getPrimary_id();
                daintree_device_id = sensor.getSecondary_id();
//                current_value = daintreeService.getDaintreePointCurrentValue(daintree_point_id, daintree_device_id);
            }
            case "ecobee": {
                ecobee_sensor_id = sensor.getPrimary_id();
                ecobee_sensor_attributes_name = condition.getEcobee_sensor_attributes_name();
//                current_value = ecobeeService.getEcobeeSensorAttributeCurrentValue(ecobee_sensor_id, ecobee_sensor_attributes_name);
                break;
            }
            case "modbus": {
                modbus_register_id = sensor.getPrimary_id();
//                current_value = modbusService.getModbusRegisterCurrentValue(modbus_register_id);
                break;

            }
        }

        ConditionsDTO updatedCondition = new ConditionsDTO(condition.getId(), condition.getName(), condition.getValue(), condition.getSecond_value(), condition.getAlert_message(),
                condition.getStart_time(), condition.getEnd_time(), condition.getSchedule(), condition.getSchedule_conditions(), condition.getAlert_count_enabled(), condition.getMax_alert_count(), condition.getAlert_condition(), condition.getAlert(),
                condition.getShow_alert(), condition.getShow_alert_message_as_value(), current_value, bacnet_device_id,
                bacnet_object_id, lorawan_sensor_id, lorawan_sensor_attribute_name, snmp_device_id, disruptive_sensor_id, my_devices_sensor_id,
                my_devices_sensor_attributes_name, monnit_sensor_id, pelican_sensor_id, pelican_sensor_attributes_name, knx_group_address, knx_device_address,
                snmp_device_configuration_id, snmp_object_oid, measuring_instrument_id, condition.getAlert_time(), daintree_device_id, daintree_point_id,
                condition.getAlert_profile_id(), ecobee_sensor_id, ecobee_sensor_attributes_name, modbus_register_id, condition.getPriority(), condition.getAlert_count_time(), condition.getEnable_threshold_line_onchart(), condition.getColor_of_threshold_line_onchart());

        return updatedCondition;
    }


    public void deleteAllSensorConditions(String username, String vdmsid, String dockername, String conditionGroup, Set<SensorDTO> sensorData, HttpServletRequest httpServletRequest) {
        for (SensorDTO sensor : sensorData) {
            deleteSensorConditions(username, vdmsid, dockername, conditionGroup, sensor, httpServletRequest);
        }
    }

    public void deleteSensorConditions(String username, String vdmsid, String dockername, String conditionGroup, SensorDTO sensor, HttpServletRequest httpServletRequest) {
        Set<ConditionsDTO> conditions = getConditions(username, vdmsid, dockername, conditionGroup, sensor);
        deleteConditions(username, vdmsid, dockername, conditionGroup, conditions, httpServletRequest);
    }

    //reset condition alert count
    public void resetConditions(String username, String vdmsid, String dockername, String conditionGroup, Set<ConditionsDTO> conditions) {
        resetConditionsAlerted(conditionGroup, conditions);
    }

    //reset condition alert count TS
    public void resetConditionsTS(String conditionGroup, Set<ConditionsDTO> conditions) {
        resetConditionsAlerted(conditionGroup, conditions);
    }

    // reset conditions

    public void resetConditionsAlerted(String conditionGroup, Set<ConditionsDTO> conditions) {
        for (ConditionsDTO conditionDTO : conditions) {
            String current_value = null;
            String primary_id = null;
            String secondary_id = null;
            conditionsRepository.updateConditionAlertCount(conditionDTO.getId(), 0);
            conditionsRepository.resetLastAlertById(conditionDTO.getId(), false); //when we click reset button we make last alerted as 0
            conditionsRepository.updateLastAlertedTimestamp(conditionDTO.getId());


            switch (conditionGroup) {
                case "bacnet": {

                    primary_id = conditionDTO.getBacnet_device_id();
                    secondary_id = conditionDTO.getBacnet_object_id();

//                    current_value = bacnetService.getBacnetObjectCurrentValue(primary_id, secondary_id);
                    break;
                }
                case "lorawan": {


                    primary_id = conditionDTO.getLorawan_sensor_id();
                    secondary_id = conditionDTO.getLorawan_sensor_attributes_name();

//                    current_value = lorawanService.getLorawanSensorAttributeCurrentValue(primary_id, secondary_id);
                    break;
                }
                case "snmp": { // not doing a call for it..

                    primary_id = conditionDTO.getSnmp_device_id();
//                    current_value = snmpService.getSnmpDeviceCurrentValue(primary_id);
                    break;
                }
                case "disruptive": {

                    primary_id = conditionDTO.getDisruptive_sensor_id();


//                    current_value = disruptiveService.getDisruptiveSensorCurrentValue(primary_id);
                    break;
                }
                case "my_devices": {

                    primary_id = conditionDTO.getMy_devices_sensor_id();
                    secondary_id = conditionDTO.getMy_devices_sensor_attributes_name();

//                    current_value = myDeviceService.getMyDevicesSensorAttributeCurrentValue(primary_id, secondary_id);
                    break;
                }

                case "monnit": {

                    primary_id = conditionDTO.getMonnit_sensor_id();

//                    current_value = monnitService.getMonnitSensorCurrentValue(primary_id);
                    break;
                }
                case "pelican": {

                    primary_id = conditionDTO.getPelican_sensor_id();
                    secondary_id = conditionDTO.getPelican_sensor_attributes_name();
//                    current_value = pelicanService.getPelicanSensorAttributeCurrentValue(primary_id, secondary_id);
                    break;
                }

                case "knx": {

                    primary_id = conditionDTO.getKnx_group_address();
                    secondary_id = conditionDTO.getKnx_device_address();
//                    current_value = knxService.getKNXCurrentValue(primary_id, secondary_id);
                    break;

                }

                case "snmp_object": {

                    primary_id = conditionDTO.getSnmp_object_oid();
                    secondary_id = conditionDTO.getSnmp_device_configuration_id();
//                    current_value = snmpService.getSnmpObjectCurrentValue(primary_id, secondary_id);
                    break;
                }
                case "measuring_instrument": {

                    primary_id = conditionDTO.getMeasuring_instrument_id();
                    current_value = measuringInstrumentService.getMeasuringInstrumentSensorCurrentValue(primary_id);
                    break;
                }
                case "daintree": {
                    primary_id = conditionDTO.getDaintree_point_id();
                    secondary_id = conditionDTO.getDaintree_device_id();
//                    current_value = daintreeService.getDaintreePointCurrentValue(primary_id, secondary_id);
                    break;
                }
                case "ecobee": {
                    primary_id = conditionDTO.getEcobee_sensor_id();
                    secondary_id = conditionDTO.getEcobee_sensor_attributes_name();
//                    current_value = ecobeeService.getEcobeeSensorAttributeCurrentValue(primary_id, secondary_id);
                    break;
                }
                case "modbus": {
                    primary_id = conditionDTO.getModbus_register_id();
//                    current_value = modbusService.getModbusRegisterCurrentValue(primary_id);
                    break;
                }

            }
            updateConditionAlert(conditionGroup, primary_id, secondary_id, current_value, "", "update");
        }
    }


    //send bacnet alert info for all required platforms
    public void sendBacnetAlertInfo(String bacnet_device_id, String bacnet_object_id, String alert_message) {
        try {
//            BacnetObjectDetailsDTO bacnetObjectDetails = bacnetService.getBacnetObjectDetailsById(bacnet_device_id, bacnet_object_id);

//            bacnetObjectDetails.setAlert_message(alert_message);

            //send bacnet alert socket event
//            sockertService.sockertBacnetAlert(bacnetObjectDetails);
            //insert bacnet alertinto history
//            historyService.insertBacnetAlertHistory(bacnetObjectDetails);
            //send bacnet alert email and sms
//            alertService.sendBacnetAlert(bacnetObjectDetails);
            //send bacnet alert info to rabbitmq
//            rabbitmqService.rabbitmqBacnetAlertData(bacnetObjectDetails);
        } catch (Exception e) {
            System.out.println("Error sending bacnet alert info " + e);
            System.out.println(e);
        }
    }

    //send lorawan alert info for all required platforms
    public void sendLorawanAlertInfo(String lorawan_sensor_id, String lorawan_sensor_attributes_name, String alert_message) {
        try {
//            LorawanSensorDetailsDTO lorawanSensorDetails = lorawanService.getLorawanSensorDetailsById(lorawan_sensor_id);

//            lorawanSensorDetails.setAlert_message(alert_message);

            //send lorawan alert socket event
//            sockertService.socketLorawanAlert(lorawanSensorDetails);
            //insert lorawan alertinto history
//            historyService.insertLorawanAlertHistory(lorawanSensorDetails, lorawan_sensor_attributes_name);
            //send lorawan alert email and sms
//            alertService.sendLorawanAlert(lorawanSensorDetails, lorawan_sensor_attributes_name);
            //send lorawan alert info to rabbitmq
//            rabbitmqService.rabbitmqLorawanAlertData(lorawanSensorDetails, lorawan_sensor_attributes_name);
        } catch (Exception e) {
            System.out.println("Error sending lorawan alert info " + e);
            System.out.println(e);
        }
    }

    //send disruptive alert info for all required platforms
    public void sendDisruptiveAlertInfo(String disruptive_sensor_id, String alert_message) {
        try {
//            DisruptiveSensorDetailsDTO disruptiveSensorDetails = disruptiveService.getDisruptiveSensorDetailsById(disruptive_sensor_id);
//
//            disruptiveSensorDetails.setAlert_message(alert_message);
//
//            //send disruptive alert socket event
//          //  sockertService.socketDisruptiveSensorAlert(disruptiveSensorDetails);
//            //insert disruptive alert into history
//            historyService.insertDisruptiveSensorAlertHistory(disruptiveSensorDetails);
//            //send disruptive alert email and sms
////            alertService.sendDisruptiveSensorAlert(disruptiveSensorDetails);
//            //send disruptive alert info to rabbitmq
//            rabbitmqService.rabbitmqDisruptiveAlertData(disruptiveSensorDetails);
        } catch (Exception e) {
            System.out.println("Error sending disruptive alert info " + e);
            System.out.println(e);
        }
    }

    //send mydevices alert info for all required platforms
    public void sendMyDevicesAlertInfo(String my_devices_sensor_id, String my_devices_sensor_attributes_name, String alert_message) {
        try {
//            MyDevicesSensorDetailsDTO myDevicesSensorDetails = myDeviceService.getMyDevicesSensorDetailsById(my_devices_sensor_id);

//            myDevicesSensorDetails.setAlert_message(alert_message);

            //send mydevices alert socket event
//            sockertService.socketMyDevicesAlert(myDevicesSensorDetails);
//            //insert mydevices alert into history
//            historyService.insertMyDevicesAlertHistory(myDevicesSensorDetails, my_devices_sensor_attributes_name);
//            //send mydevices alert email and sms
////            alertService.sendMyDevicesAlert(myDevicesSensorDetails, my_devices_sensor_attributes_name);
//            //send myedevices alert info to rabbitmq
//            rabbitmqService.rabbitmqMyDevicesAlertData(myDevicesSensorDetails, my_devices_sensor_attributes_name);
        } catch (Exception e) {
            System.out.println("Error sending mydevices alert info " + e);
            System.out.println(e);
        }
    }

    //send monnit alert info for all required platforms
    public void sendMonnitAlertInfo(String monnit_sensor_id, String alert_message) {
        try {
//            MonnitSensorDetailsDTO monnitSensorDetails = monnitService.getMonnitSensorDetailsById(monnit_sensor_id);
//
//            monnitSensorDetails.setAlert_message(alert_message);
//
//            //send monnit alert socket event
//            sockertService.socketMonnitSensorAlert(monnitSensorDetails);
//            //insert monnit alert into history
//            historyService.insertMonnitSensorAlertHistory(monnitSensorDetails);
//            //send monnit email and sms alert
////            alertService.sendMonnitSensorAlert(monnitSensorDetails);
//            //send monnit alert info to rabbitmq
//            rabbitmqService.rabbitmqMonnitAlertData(monnitSensorDetails);
        } catch (Exception e) {
            System.out.println("Error sending monnit alert info " + e);
            System.out.println(e);
        }
    }

    //send pelican alert info for all required platforms
    public void sendPelicanAlertInfo(String pelican_sensor_id, String pelican_sensor_attributes_name, String alert_message) {
        try {
//            PelicanSensorDetailsDTO pelicanSensorDetails = pelicanService.getPelicanSensorDetailsById(pelican_sensor_id);
//
//            pelicanSensorDetails.setAlert_message(alert_message);
//
//            //send pelican alert socket event
//            sockertService.socketPelicanAlert(pelicanSensorDetails);
//            //insert pelican alert into history
//            historyService.insertPelicanAlertHistory(pelicanSensorDetails, pelican_sensor_attributes_name);
//            //send pelican alert email and sms
////            alertService.sendPelicanAlert(pelicanSensorDetails, pelican_sensor_attributes_name);
//            //send pelican alert info to rabbitmq
//            rabbitmqService.rabbitmqPelicanAlertData(pelicanSensorDetails, pelican_sensor_attributes_name);
        } catch (Exception e) {
            System.out.println("Error sending pelican alert info " + e);
            System.out.println(e);
        }
    }

    //send knx alert info for all required platforms
    public void sendKNXAlertInfo(String knx_device_address, String knx_group_address, String alert_message) {
        try {
//            KNXGroupDetailsDTO knxGroupDetails = knxService.getKNXGroupDetailsByAddress(knx_device_address, knx_group_address);
//
//            knxGroupDetails.setAlert_message(alert_message);
//
//            //send knx alert socket event
//            sockertService.socketKNXAlert(knxGroupDetails);
//            //insert knx alertinto history
//            historyService.insertKNXAlertHistory(knxGroupDetails);
//            //send knx alert email and sms
////            alertService.sendKNXAlert(knxGroupDetails);
//            //send bacnet alert info to rabbitmq
//            rabbitmqService.rabbitmqKNXAlertData(knxGroupDetails);
        } catch (Exception e) {
            System.out.println("Error sending KNX alert info " + e);
            System.out.println(e);
        }
    }

    private void sendSnmpObjectAlertInfo(String snmp_device_configuration_id, String snmp_object_oid, String alert_message) {
//        SnmpObjectDetailsDTO snmpObjectDetails = snmpService.getSnmpObjectDetailsById(snmp_device_configuration_id, snmp_object_oid);
//
//        snmpObjectDetails.setAlert_message(alert_message);
//
//        //send snmp alert socket event
//        sockertService.socketSnmpObjectAlert(snmpObjectDetails);
//        //insert snmp alert into history
//        historyService.insertSnmpObjectAlertHistory(snmpObjectDetails);
//        //send snmp alert email and sms
////        alertService.sendSnmpObjectAlert(snmpObjectDetails);
//        //send bacnet alert info to rabbitmq
//        rabbitmqService.rabbitmqSnmpObjectAlertData(snmpObjectDetails);
    }

    //send measuring instrument alert info for all required platforms
    public void sendMeasuringInstrumentAlertInfo(String measuring_instrument_id, String alert_message) {
        try {
            MeasuringInstrumentDetailsDTO measuringInstrumentDetails = measuringInstrumentService.getMeasuringInstrumentSensorDetailsById(measuring_instrument_id);

            measuringInstrumentDetails.setAlert_message(alert_message);

            //send measuring instrument alert socket event
//            sockertService.socketMeasuringInstrumentSensorAlert(measuringInstrumentDetails);
//            //insert measuring instrument alert into history
//            historyService.insertMeasuringInstrumentSensorAlertHistory(measuringInstrumentDetails);
//            //send measuring instrument and sms alert
////            alertService.sendMeasuringInstrumentSensorAlert(measuringInstrumentDetails);
//            //send measuring instrument alert info to rabbitmq
//            rabbitmqService.rabbitmqMeasuringInstrumentAlertData(measuringInstrumentDetails);
        } catch (Exception e) {
            System.out.println("Error sending measuring instrument alert info " + e);
            System.out.println(e);
        }
    }

    private void sendDaintreeAlertInfo(String daintree_device_id, String daintree_point_id, String alert_message) {
        try {
//            DaintreeDeviceDetailsDTO daintreeDetails = daintreeService.getDaintreeDetailsById(daintree_device_id);
//            daintreeDetails.setAlert_message(alert_message);
//
//            //send daintree alert socket event
//            sockertService.socketDaintreeDevicesAlert(daintreeDetails);
//
//            //insert daintree alert into history
//            historyService.insertDaintreeDevicesAlertHistory(daintreeDetails, daintree_point_id);
//
//            //send daintree and sms alert
////            alertService.sendDaintreenDeviceAlert(daintreeDetails, daintree_point_id);
//
//            //send daintree alert info to rabbitmq
//            rabbitmqService.rabbitmqDaintreeDeviceAlertData(daintreeDetails, daintree_point_id);

        } catch (Exception e) {
            System.out.println("Error sending daintree alert info " + e);
            System.out.println(e);
        }
    }

    public void sendAlertInfo(String conditionGroup, ConditionsDTO condition, String alert_message, BigInteger schedule_job_created_time) {

//        SensorAlertDTO sensorAlert = null;
//        DeviceAlertDTO deviceAlert = null;
//        AlertProfileDTO alertProfile = null;
//
//
//        switch (conditionGroup) {
//            case "bacnet": {
//                //send bacnet alert email and sms
//                sensorAlert = bacnetService.getBacnetObjectAlertDetails(condition.getBacnet_device_id(), condition.getBacnet_object_id());
//                break;
//            }
//            case "lorawan": {
//                //send lorawan alert email and sms
//                sensorAlert = lorawanService.getLorawanSensorAttributesAlertDetails(condition.getLorawan_sensor_id(), condition.getLorawan_sensor_attributes_name());
//                break;
//            }
//            case "disruptive": {
//                //send disruptive alert email and sms
//                sensorAlert = disruptiveService.getDisruptiveSensorAlertDetails(condition.getDisruptive_sensor_id());
//                break;
//            }
//            case "my_devices": {
//                //send mydevices alert email and sms
//                sensorAlert = myDeviceService.getMyDevicesSensorAttributesAlertDetails(condition.getMy_devices_sensor_id(), condition.getMy_devices_sensor_attributes_name());
//                break;
//            }
//            case "monnit": {
//                //send monnit email and sms alert
//                sensorAlert = monnitService.getMonnitSensorAlertDetails(condition.getMonnit_sensor_id());
//                break;
//            }
//            case "pelican": {
//                //send pelican alert email and sms
//                sensorAlert = pelicanService.getPelicanSensorAttributesAlertDetails(condition.getPelican_sensor_id(), condition.getPelican_sensor_attributes_name());
//                break;
//            }
//            case "knx": {
//                //send knx alert email and sms
//                sensorAlert = knxService.getKNXGroupDetailsByAddressAlertDetails(condition.getKnx_device_address(), condition.getKnx_group_address());
//                break;
//            }
//            case "snmp_object": {
//                //send snmp alert email and sms
//                sensorAlert = snmpService.getSnmpObjectAlertDetails(condition.getSnmp_device_configuration_id(), condition.getSnmp_object_oid());
//                break;
//            }
//            case "measuring_instrument": {
//                //send measuring instrument and sms alert
//                sensorAlert = measuringInstrumentService.getMeasuringInstrumentAlertDetails(condition.getMeasuring_instrument_id());
//                break;
//            }
//            case "daintree": {
//                //send knx alert email and sms
//                sensorAlert = daintreeService.getDaintreePointAlertDetails(condition.getDaintree_device_id(), condition.getDaintree_point_id());
//                break;
//            }
//            case "ecobee": {
//                sensorAlert = ecobeeService.getEcobeeSensorAttributesAlertDetails(condition.getEcobee_sensor_id(), condition.getEcobee_sensor_attributes_name());
//                break;
//            }
//            case "modbus": {
//                sensorAlert = modbusService.getModbusAlertDetails(condition.getModbus_register_id());
//                break;
//            }
//            default: {
//                return;
//            }

//        }
//
//        sensorAlert.setAlert_message(alert_message);
//        //sensor alert changes
//        sensorAlert.setPriority(condition.getPriority()); // directly using the method from condition
//
//        if (sensorAlert.getDevice_id() != null) {
//            log.info("Checking AlertDownTimeState for DeviceId: {} and AlertProfileId: {}",sensorAlert.getDevice_id(), condition.getAlert_profile_id());
//            Boolean alertDowntimeState = alertDowntimeScheduleService.checkAlertDowntime(sensorAlert.getDevice_id(), condition.getAlert_profile_id());
//            log.info("AlertDownTimeState {}",alertDowntimeState);
//            System.out.println("Alert State " + alertDowntimeState);
//            if (!(alertDowntimeState)) {
//                deviceAlert = deviceService.getDeviceAlertInfoById(sensorAlert.getDevice_id());
//                alertProfile = alertProfileService.getAlertProfileById(condition.getAlert_profile_id());
//                if (alertProfile != null) {
//
//                    alertService.sendSensorAlertInfo(sensorAlert, deviceAlert, alertProfile, schedule_job_created_time);
//
//                    if (alertProfile.getIoc() != null && alertProfile.getIoc() == 1) {
//                        iocService.sendSensorAlertDataToIOC(condition, deviceAlert, sensorAlert, alertProfile, schedule_job_created_time);
//                    }
//                }
//            }
//        }

    }

//    public void updateAlertProfileId(String alert_profile_id) {
//        conditionsRepository.updateAlertProfileId(alert_profile_id);
//    }

//    private Set<SensorDTO> updateDaintreeShareConditions(ShareConditionsDTO shareConditions) {
//        Set<SensorDTO> sensors = new HashSet<>();
//        SensorDTO newSensorDTO;
//        for (ConditionsDTO conditionsDTO : shareConditions.getConditions()) {
//            String daintree_point_name = daintreeService.getDaintreePointNameById(conditionsDTO.getDaintree_point_id(), conditionsDTO.getDaintree_device_id());
//            for (SensorDTO sensorDTO : shareConditions.getSensors()) {
//                Set<String> daintree_point_ids = daintreeService.getDaintreePointIdsByName(daintree_point_name, sensorDTO.getPrimary_id());
//                for (String daintree_point_id : daintree_point_ids) {
//                    newSensorDTO = new SensorDTO(daintree_point_id, sensorDTO.getPrimary_id(), daintree_point_name);
//                    sensors.add(newSensorDTO);
//                }
//            }
//        }
//        return sensors;
//    }

    //send ecobee alert info for all required platforms
//    public void sendEcobeeAlertInfo(String ecobee_sensor_id, String ecobee_sensor_attributes_name, String alert_message) {
//        try {
//            EcobeeSensorDetailsDTO ecobeeSensorDetails = ecobeeService.getEcobeeSensorDetailsById(ecobee_sensor_id);
//
//            ecobeeSensorDetails.setAlert_message(alert_message);
//
//            //send ecobee alert socket event
//            sockertService.socketEcobeeAlert(ecobeeSensorDetails);
//
//            //insert ecobee alert into history
//            historyService.insertEcobeeAlertHistory(ecobeeSensorDetails, ecobee_sensor_attributes_name);
//
//            //send ecobee alert info to rabbitmq
//            rabbitmqService.rabbitmqEcobeeAlertData(ecobeeSensorDetails, ecobee_sensor_attributes_name);
//        } catch (Exception e) {
//            System.out.println("Error sending ecobee alert info " + e);
//            System.out.println(e);
//        }
//    }

//    private void sendModbusAlertInfo(String modbus_register_id, String alert_message) {
//        try {
//            ModbusRegisterDetailsDTO modbusRegisterDetails = modbusService.getModbusRegisterDetailsById(modbus_register_id);
//            modbusRegisterDetails.setAlert_message(alert_message);
//            //send Modbus alert socket event
//            sockertService.sockertModbusAlert(modbusRegisterDetails);
//            //insert Modbus alertinto history
//            historyService.insertModbusAlertHistory(modbusRegisterDetails);
//            //send Modbus alert info to rabbitmq
//            rabbitmqService.rabbitmqModbusAlertData(modbusRegisterDetails);
//        } catch (Exception e) {
//            System.out.println("Error sending modbus alert info " + e);
//            System.out.println(e);
//
//        }
//    }

    public void scheduleSensorAlertJob(String job_type, ConditionsDTO condition, String condition_group) {
        //api call to quartz to add the job
        ScheduledJobDTO jobSchedulerDTO = new ScheduledJobDTO();
        jobSchedulerDTO.setJob_type(job_type);
        jobSchedulerDTO.setTime_in_seconds(Long.valueOf(condition.getAlert_time()));
        System.out.println("adding into scheduler");
        String job_id = jobSchedulerService.createScheduledJob(jobSchedulerDTO);
        System.out.println("print job id  : " + job_id);
        //add a record to scheduled_job
        if (job_id != null) {
            ScheduledJobDTO scheduledJobDTO = new ScheduledJobDTO();
            scheduledJobDTO.setId(job_id);
            scheduledJobDTO.setCondition_id(condition.getId());
            scheduledJobDTO.setCondition_type("sensor");
            scheduledJobDTO.setCondition_group(condition_group);
//            System.out.println("print timestamp : " + scheduledJobDTO.getCreated_timestamp());
            jobSchedulerService.addScheduledJob(Set.of(scheduledJobDTO));
        }
    }

    public void replaceSensorAlertJob(String job_type, ConditionsDTO condition, String condition_group, String job_key) {
        //api call to quartz to add the job
        ScheduledJobDTO jobSchedulerDTO = new ScheduledJobDTO();
        jobSchedulerDTO.setJob_type(job_type);
        jobSchedulerDTO.setTime_in_seconds(Long.valueOf(condition.getAlert_time()));
        jobSchedulerDTO.setId(job_key);
        System.out.println("replace into scheduler");
        String job_id = jobSchedulerService.createScheduledJob(jobSchedulerDTO);
        System.out.println("print job iddd : " + job_id);
        //add a record to scheduled_job
        if (job_id != null) {
            jobSchedulerService.deleteScheduledJob(Set.of(job_key));

            ScheduledJobDTO scheduledJobDTO = new ScheduledJobDTO();
            scheduledJobDTO.setId(job_id);
            scheduledJobDTO.setCondition_id(condition.getId());
            scheduledJobDTO.setCondition_type("sensor");
            scheduledJobDTO.setCondition_group(condition_group);
            jobSchedulerService.addScheduledJob(Set.of(scheduledJobDTO));
        }
    }

    public ConditionsDTO getConditionByConditionId(String condition_id) {
        return conditionsRepository.getConditionByConditionId(condition_id);
    }

    public void deleteSensorAlertJob(String conditionId) {
        try {
            ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(conditionId);
            if (scheduledJobDTO != null) {
                //api call to quartz to delete the job
                ScheduledJobDTO jobSchedulerDTO = new ScheduledJobDTO();
                jobSchedulerDTO.setJob_type("delete");
                jobSchedulerDTO.setId(scheduledJobDTO.getId());

                System.out.println("deleting from scheduler");
                String job_id = jobSchedulerService.createScheduledJob(jobSchedulerDTO);
                //delete the record
                if (job_id != null) {
                    scheduledJobRepository.deleteByConditionId(conditionId);
                }
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    public List<ConditionsAdvanceExportExcelDto> getConditionsForAdvanceExcelExport(String username, String vdmsid, String device_id) {
        List<Map<String, Object>> rows = conditionsRepository.getConditionsForAdvanceExcelExport(device_id);

        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<ConditionsAdvanceExportExcelDto> conditionsAdvanceExportExcelDtos = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            ConditionsAdvanceExportExcelDto dto = new ConditionsAdvanceExportExcelDto();
            dto.setConditionId((String) row.get("condition_id"));
            dto.setConditionName((String) row.get("condition_name"));
            dto.setAlertCondition((String) row.get("alert_condition"));
            dto.setValueName((String) row.get("value_name"));
            dto.setAlertMessage((String) row.get("alert_message"));
            dto.setPriority((String) row.get("priority"));
            dto.setAlertProfileId((String) row.get("alert_profile_id"));
            dto.setAlertProfileName((String) row.get("alert_profile_name"));
            dto.setIoc((Integer) row.get("ioc"));
            dto.setMeasuringInstrumentId((String) row.get("measuring_instrument_id"));
            dto.setShowAlert((Boolean) row.get("show_alert"));
            dto.setShowAlertMessageAsValue((Boolean) row.get("show_alert_message_as_value"));
            dto.setEnableThresholdLineOnChart((Integer) row.get("enable_threshold_line_onchart"));
            dto.setColorOfThresholdLineOnChart((String) row.get("color_of_threshold_line_onchart"));
//            dto.setAlertAfter((Boolean) row.get("alert_after"));
            dto.setAlertAfterTime((Integer) row.get("alert_time"));
            dto.setScheduleAlert((Integer) row.get("schedule_alert"));
            dto.setScheduleStartTime((String) row.get("schedule_start_time"));
            dto.setScheduleEndTime((String) row.get("schedule_end_time"));
            dto.setScheduleConditions((String) row.get("schedule_conditions"));
            dto.setAlertCountEnable((Integer) row.get("alert_count_enable"));
            conditionsAdvanceExportExcelDtos.add(dto);

        }

        return conditionsAdvanceExportExcelDtos;

    }

}