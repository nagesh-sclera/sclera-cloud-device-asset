package io.sclera.service;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import io.sclera.Repository.MeasuringInstrumentAttributesRepository;
import io.sclera.dto.*;
import io.sclera.dto.touchscreen.SensorDTO;
import io.sclera.models.MeasuringInstrument;
import io.sclera.queryrepository.MeasuringInstrumentsQueryRepository;
import io.sclera.sockets.SocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.MeasuringInstrumentRepository;
import io.sclera.rabbitmq.RabbitmqService;
import io.sclera.utils.InstrumentFormula;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

@Service
public class MeasuringInstrumentService {
    private static final Logger log = LoggerFactory.getLogger(MeasuringInstrumentService.class);

    @Autowired
    InstrumentFormula instrumentFormula;

    @Autowired
    MeasuringInstrumentRepository measuingInstrumentRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    RabbitmqService rabbitmqService;

    @Autowired
    APICallService apiCallService;

    @Autowired
    ConditionsService conditionsService;

    @Autowired
    SocketService socketService;

    @Autowired
    DaintreeService daintreeService;

    @Autowired
    DataSource dataSource;

    @Autowired
    MeasuringInstrumentsQueryRepository measuringInstrumentsQueryRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserActionLogService userActionLogService;

    @Autowired
    MeasuringInstrumentAttributesRepository measuringInstrumentAttributesRepository;

    @Autowired
    LocationService locationService;

    @Autowired
    IOCService iocService;


    public void upsertInstrument(String username, String vdmsid, String share_method, Set<MeasuringInstrumentDTO> instruments, HttpServletRequest httpServletRequest) {

        if (share_method != null && share_method.equals("replace")) {
            Set<String> device_ids = new HashSet<>();

            for (MeasuringInstrumentDTO instrument : instruments) {
                device_ids.add(instrument.getDevice_id());
            }
            for (String device_id : device_ids) {
                this.deleteInstrumentsByDeviceId(username, vdmsid, device_id, httpServletRequest);
            }
        }

        if (share_method != null && (share_method.equals("add") || share_method.equals("replace"))) {
            for (MeasuringInstrumentDTO instrument : instruments) {
                try {
                    if (instrument.getMeasuring_entity() == null) {
                        instrument.setMeasuring_entity("device");
                    }
                    if (instrument.getMeasuring_instrument_attributes() != null && instrument.getMeasuring_instrument_attributes().size() > 0) {
                        if (instrument.getId() == null) {
                            String id = Generators.timeBasedGenerator().generate().toString();
                            instrument.setId(id);

                            measuingInstrumentRepository.upsertInstrument(instrument.getId(), instrument.getType(), instrument.getName(),
                                    instrument.getDescription(), instrument.getCalculation_type(), instrument.getAttribute(), instrument.getParameter(), instrument.getCategory(),
                                    instrument.getValue(), instrument.getUnit(), instrument.getTags(), instrument.getDevice_id(),
                                    instrument.getSensor_type(), instrument.getShow_on_map(), instrument.getShow_on_scan(), instrument.getMeasuring_entity(), instrument.getSub_category(), instrument.getDigital_twin_position(), instrument.getScale_type());
                            userActionLogService.addUserAction(username, "sensors", "ADD", " Added measuring instrument: " + instrument.getName() + " with  id: " + instrument.getId() + " for device with id: " + instrument.getDevice_id(), "success", "sensors_configuration", instrument.getId());
                            log.info("endpoint: {},upsertInstrument, params: instrument: {}", httpServletRequest.getRequestURI(), instrument);

                            upsertMeasuringInstrumentAttributes(instrument.getId(), instrument.getMeasuring_instrument_attributes());

                        } else {
                            measuingInstrumentRepository.upsertInstrument(instrument.getId(), instrument.getType(), instrument.getName(),
                                    instrument.getDescription(), instrument.getCalculation_type(), instrument.getAttribute(), instrument.getParameter(), instrument.getCategory(),
                                    instrument.getValue(), instrument.getUnit(), instrument.getTags(), instrument.getDevice_id(),
                                    instrument.getSensor_type(), instrument.getShow_on_map(), instrument.getShow_on_scan(), instrument.getMeasuring_entity(), instrument.getSub_category(), instrument.getDigital_twin_position(), instrument.getScale_type());

                            upsertMeasuringInstrumentAttributes(instrument.getId(), instrument.getMeasuring_instrument_attributes());

                            userActionLogService.addUserAction(username, "sensors", "UPDATE", " Update measuring instrument: " + instrument.getName() + " with  id: " + instrument.getId() + " for device with id: " + instrument.getDevice_id(), "success", "sensors_configuration", instrument.getId());
                            log.info("endpoint: {},upsertInstrument, params: instrument: {}", httpServletRequest.getRequestURI(), instrument);


                        }

                        updateMeasuringInstrumentValueByFormula(instrument.getId(), instrument.getValue_changed_status());

                    }
                    deviceService.updateDeviceMeasureCountByDeviceId(instrument.getDevice_id());
                } catch (Exception e) {
                    userActionLogService.addUserAction(username, "sensors", "ADD", " Unable to add measuring instrument: " + instrument.getName() + " with  id: " + instrument.getId() + " for device with id: " + instrument.getDevice_id(), "failed", "sensors_configuration", instrument.getId());
                    log.error("Exception in UPDATE/ADD instrument, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
                }

            }
        }


    }


    //delete instruments tagged to a device
    public void deleteInstrumentsByDeviceId(String username, String vdmsid, String device_id,HttpServletRequest httpServletRequest) {
        Set<MeasuringInstrumentDTO> delete_instruments = measuingInstrumentRepository.getInstrumentByDeviceId(device_id);
        this.deleteInstrumentById(username, vdmsid, delete_instruments,httpServletRequest);
    }

    public Set<MeasuringInstrumentDTO> getInstrumentByDeviceId(String username, String vdmsid, String device_id) {
        // TODO Auto-generated method stub
        updateMultipleInstrumentValueByFormula(device_id);
        Set<MeasuringInstrumentDTO> measuringInstruments = measuingInstrumentRepository.getInstrumentByDeviceId(device_id);
        for (MeasuringInstrumentDTO measuringInstrument : measuringInstruments) {
            measuringInstrument.setMeasuring_instrument_attributes(getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuringInstrument.getId()));

        }
        return measuringInstruments;
    }


    public Set<MeasuringInstrumentDTO> getInstrumentsByDeviceId(String username, String vdmsid, String device_id) {
        Set<MeasuringInstrumentDTO> measuringInstruments = measuingInstrumentRepository.getInstrumentByDeviceId(device_id);
        for (MeasuringInstrumentDTO measuringInstrument : measuringInstruments) {
            measuringInstrument.setMeasuring_instrument_attributes(getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuringInstrument.getId()));
            measuringInstrument.setLocations(locationService.getTaggedMeasuringInstrumentLocations("", "", measuringInstrument.getId()));
        }
        return measuringInstruments;
    }

    // Updating from sensor
//    public void updateMeasuringIntrumentParametersByIds(String protocol, String primary_id, String secondary_id, String value) {
//        measuingInstrumentRepository.updateMeasuringIntrumentParametersByIds(protocol, primary_id, secondary_id, value);
//
//        List<String> ids = measuingInstrumentRepository.listIdByMeasuringParameter(protocol, primary_id, secondary_id);
//        for (String id : ids) {
//            updateInstrumentValueByFormula(id, 1);
//        }
//    }

//    public void updateMeasuringIntrumentParametersByIds(String protocol, String primary_id, String secondary_id, String value) {
//        try {
//            System.out.println("protocol : " + protocol + "    || primary_id : " + primary_id + "  || secondary_id : " + secondary_id + "  || value : " + value);
//            StringBuilder query = new StringBuilder();
//            query.append("SELECT mi.id , mi.type, mi.name, mi.description, mi.calculation_type, mi.attribute, mi.parameter , mi.category , mi.parameter , mi.value , mi.unit, mi.tags , mi.device_id, mi.timestamp, mi.sensor_type , mi.alert, mi.user_data_name, mi.user_data_value, mi.show_on_map, mi.show_on_scan , mi.measuring_entity, b.name as building, f.name as floor, l.name as location, l.id as location_id, mi.sub_category  " +
//                    " FROM measuring_instrument mi " +
//                    " LEFT JOIN device d ON mi.device_id = d.id " +
//                    " LEFT JOIN location l ON d.location_id = l.id " +
//                    " LEFT JOIN floor f ON l.floor_id = f.id " +
//                    " LEFT JOIN building b ON f.building_id = b.id " +
//                    " WHERE ");
//            for (int i = 1; i <= 50; i++) {
//                query.append(" (JSON_EXTRACT(`attribute`,\"$.parameter_").append(i).append("_protocol\") = '").append(protocol).append("' AND JSON_EXTRACT(`attribute`, \"$.parameter_").append(i).append("_primary_id\") = '").append(primary_id).append("' AND JSON_EXTRACT(`attribute`, \"$.parameter_").append(i).append("_secondary_id\") = '").append(secondary_id).append("' ) OR");
//            }
//            query.setLength(query.length() - 2);
//            List<Map<String, Object>> measuringInstrumentList = jdbcTemplate.queryForList(query.toString());
//            for (Map<String, Object> measuringInstrument : measuringInstrumentList) {
//                if (measuringInstrument.containsKey("attribute") && measuringInstrument.get("attribute") != null) {
//                    com.alibaba.fastjson.JSONObject attribute = com.alibaba.fastjson.JSONObject.parseObject(measuringInstrument.get("attribute").toString());
//                    System.out.println("---------- attribute before update ----------" + attribute);
//                    Integer parameters_size = getNoOfParametersFromString(attribute.toString());
//                    for (int i = 1; i <= parameters_size; i++) {
//                        if (attribute.getString("parameter_" + i + "_protocol") != null && attribute.getString("parameter_" + i + "_primary_id") != null && attribute.getString("parameter_" + i + "_secondary_id") != null) {
//                            if (attribute.getString("parameter_" + i + "_protocol").equals(protocol) && attribute.getString("parameter_" + i + "_primary_id").equals(primary_id) && attribute.getString("parameter_" + i + "_secondary_id").equals(secondary_id)) {
//                                Integer rows_affested = measuingInstrumentRepository.updateMeasuringInstrumentParametersValuesByIds(i, value, measuringInstrument.get("id").toString());
//                                System.out.println("------rows_affested-----" + rows_affested + "  || id : " + measuringInstrument.get("id").toString());
//                            }
//                        }
//                    }
//                    updateInstrumentValueByFormula(measuringInstrument.get("id").toString(), 1);
//                }
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }

    public Integer getNoOfParametersFromString(String input) {
        Integer maxNumber = 0;
        Pattern pattern = Pattern.compile("parameter_\\d+");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String match = matcher.group();
            int lastIndex = match.lastIndexOf('_');
            int number = Integer.parseInt(match.substring(lastIndex + 1));
            maxNumber = Math.max(maxNumber, number);
        }

        return maxNumber;
    }


    public void deleteInstrumentById(String username, String vdmsid, Set<MeasuringInstrumentDTO> measuring_instruments, HttpServletRequest httpServletRequest) {
        // TODO Auto-generated method stub

        for (MeasuringInstrumentDTO measuring_instrument : measuring_instruments) {
            try {

                // delete attributes by measuringInstrumentsId
//                deleteMeasuringInstrumentAttributeByMeasuringInstrumentId(measuring_instrument.getId());

                measuingInstrumentRepository.deleteById(measuring_instrument.getId());
                userActionLogService.addUserAction(username, "sensors", "DELETE", "Measuring Instrument with id:" + measuring_instrument.getId() + " deleted successfully tagged to device with id: " + measuring_instrument.getDevice_id(), "success", "sensors_configuration", measuring_instrument.getId());

                deviceService.updateDeviceMeasureCountByDeviceId(measuring_instrument.getDevice_id());
                log.info("endpoint: {}, deleteInstrumentById,  params: measuring_instrument: {} ", httpServletRequest.getRequestURI(), measuring_instrument);
                //update device measuring instrument alert status
                deviceService.updateDeviceMeasuringInstrumentStatusByDeviceId(measuring_instrument.getDevice_id());
            } catch (Exception e) {
                userActionLogService.addUserAction(username, "sensors", "DELETE", "Unable to delete Measuring Instrument with id:" + measuring_instrument.getId() + " tagged to device with id: " + measuring_instrument.getDevice_id(), "failed", "sensors_configuration", measuring_instrument.getId());
                log.error("Exception in Deleting Instrument By Id,endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
            }
        }
    }

//    public void updateInstrumentValueByFormula(String id, Integer value_changed_status) {
//        try {
//            MeasuringInstrumentDTO measuingInstrument = measuingInstrumentRepository.getInstrumentByInstrumentId(id);
//            String presentValue = instrumentFormula.getValuebyMeasuringParameter(measuingInstrument.getCalculation_type(), measuingInstrument.getType(), measuingInstrument.getValue(),
//                    measuingInstrument.getUnit(), measuingInstrument.getAttribute());
//            System.out.println("PRESENT VALUE :" + presentValue + " OLD: " + measuingInstrument.getValue());
//            if (presentValue != null && ((value_changed_status != null && value_changed_status.equals(1)) || !presentValue.equals(measuingInstrument.getValue()))) {
//                BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
//                updateInstrumentValueById(measuingInstrument.getId(), presentValue, timestamp, measuingInstrument.getSensor_type()); // NEW CHANGE
//
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }

//    public void updateInstrumentValueByIdForDaintree(List<DaintreePointsDTO> daintreeDevicePointValuesList) {
//        try (Connection connection = dataSource.getConnection();) {
//            PreparedStatement preparedStatement = connection.prepareStatement(measuringInstrumentsQueryRepository.getQueryForUpdateInstrumentValueAndAttributeById());
//            int batchCounter = 0;
//            int maxBatchLimit = 100;
//            List<MeasuringInstrumentDTO> updatedMeasuringInstruments = new ArrayList<>();
//
//            List<MeasuringInstrumentDTO> measuringIntruments = this.getDaintreeMeasuringIntruments();
//
//            Multimap<String, MeasuringInstrumentDTO> measuringInstrumentsMap = ArrayListMultimap.create();
//            for (MeasuringInstrumentDTO measuringInstrument : measuringIntruments) {
//                JSONObject attribute = new JSONObject(measuringInstrument.getAttribute());
//                if (attribute != null && attribute.getString("parameter_1_protocol").equals("daintree") && attribute.has("parameter_1_primary_id") && attribute.has("parameter_1_secondary_id")) {
//                    String primaryId = attribute.getString("parameter_1_primary_id");
//                    String secondaryId = attribute.getString("parameter_1_secondary_id");
//                    measuringInstrumentsMap.put(primaryId + "," + secondaryId, measuringInstrument);
//                }
//            }
//
//            List<DaintreePointsDTO> daintreePointsList = this.fetchDaintreePoints(daintreeDevicePointValuesList);
//            System.out.println("\n\nMI Points list size :" + daintreePointsList.size());
//            for (DaintreePointsDTO daintreePoint : daintreePointsList) {
//                if (daintreePoint != null) {
//                    try {
//                        if (daintreePoint.getVal() != null) {
//                            daintreePoint.setValue(daintreePoint.getVal());
//                        }
//                        Collection<MeasuringInstrumentDTO> measuringInstruments = new ArrayList<>();
//                        if (measuringInstrumentsMap.containsKey(daintreePoint.getId() + "," + daintreePoint.getDaintree_device_id())) {
//                            measuringInstruments = measuringInstrumentsMap.get(daintreePoint.getId() + "," + daintreePoint.getDaintree_device_id());
//                            for (MeasuringInstrumentDTO measuringInstrument : measuringInstruments) {
//                                try {
//                                    JSONObject attribute = new JSONObject(measuringInstrument.getAttribute());
//                                    BigInteger timestamp = null;
//                                    if (attribute != null && attribute.getString("parameter_1_protocol").equals("daintree") && attribute.getString("parameter_1_primary_id").equals(daintreePoint.getId()) && attribute.getString("parameter_1_secondary_id").equals(daintreePoint.getDaintree_device_id())) {
//
//                                        attribute.put("parameter_1_value", daintreePoint.getValue());
//
//                                        measuringInstrument.setAttribute(attribute.toString());
//                                        timestamp = BigInteger.valueOf(System.currentTimeMillis());
//
//                                        String presentValue = instrumentFormula.getValuebyMeasuringParameter(measuringInstrument.getCalculation_type(), measuringInstrument.getType(), daintreePoint.getValue(), measuringInstrument.getUnit(), measuringInstrument.getAttribute());
//                                        System.out.println("\n\nHaystack point id : " + daintreePoint.getHaystack_point_id() + "      Value : " + daintreePoint.getValue() + "                   Device id : " + measuringInstrument.getDevice_id() + "                pointsid : " + daintreePoint.getId() + "                         measuringInstrumentId  :" + measuringInstrument.getId());
//                                        System.out.println("--presentValue-- : " + presentValue);
//                                        measuringInstrument.setPrevious_value(presentValue);
//                                        measuringInstrument.setValue(presentValue);
//                                        measuringInstrument.setTimestamp(timestamp);
//                                        updatedMeasuringInstruments.add(measuringInstrument);
//
//                                        preparedStatement.setString(4, measuringInstrument.getId());
//                                        preparedStatement.setString(1, presentValue);
//                                        preparedStatement.setString(2, String.valueOf(timestamp));
//                                        preparedStatement.setString(3, measuringInstrument.getAttribute());
//
//                                        preparedStatement.addBatch();
//                                        batchCounter++;
//                                        if (batchCounter == maxBatchLimit) {
//                                            preparedStatement.executeBatch();
//                                            System.out.println("preparedStatement executed for 100 data   ");
//                                            preparedStatement.clearBatch();
//                                            batchCounter = 0;
//                                        }
//                                    }
//                                } catch (Exception e) {
//                                    System.out.println("DAINTREE SYNC UPADATE MEASURING INSTRUMENTS ERROR");
//                                    System.out.println(e);
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                        System.out.println("DAINTREE SYNC UPADATE MEASURING INSTRUMENTS POINTS ERROR");
//                        System.out.println(e);
//                    }
//                }
//
//            }
//
//            if (batchCounter > 0) {
//                preparedStatement.executeBatch();
//                System.out.println("executed batch MeasuringInstruments");
//            }
//            preparedStatement.close();
//            connection.close();
//
//            for (MeasuringInstrumentDTO measuringInstrument : updatedMeasuringInstruments) {
//                //Update alert status
//                if ((measuringInstrument.getPrevious_value() == null && measuringInstrument.getValue() != null) || (measuringInstrument.getValue() != null && !measuringInstrument.getPrevious_value().equals(measuringInstrument.getValue()))) {
//                    try {
//                        conditionsService.updateConditionAlert("measuring_instrument", measuringInstrument.getId(), "", measuringInstrument.getValue(), "", "sync");
//                        socketService.socketMeasuringInstrumentSensorValueUpdate(measuringInstrument.getId());
//                    } catch (Exception e) {
//                        System.out.println("error updating measuring_instrument condition status " + e);
//                        System.out.println(e);
//                    }
//                }
//                try {
//                    rabbitmqService.rabbitmqMeasuringInstrumentData(measuringInstrument.getId(), measuringInstrument.getValue(), measuringInstrument.getTimestamp(), measuringInstrument.getSensor_type());
//                } catch (Exception e) {
//                    System.out.println("error updating MeasuringInstrument rabbitmq value update " + e);
//                    System.out.println(e);
//                }
//            }
//            System.out.println("Measuring Instruments for daintree Updated Sussfully");
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }

    public List<MeasuringInstrumentDTO> getDaintreeMeasuringIntruments() {
        List<MeasuringInstrumentDTO> measuringInstruments = measuingInstrumentRepository.getDaintreeMeasuringInstruments();
        for (MeasuringInstrumentDTO measuringInstrument : measuringInstruments) {
            measuringInstrument.setMeasuring_instrument_attributes(getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuringInstrument.getId()));
        }
        return measuringInstruments;
    }

    public void updateMultipleInstrumentValueByFormula(String device_id) // NEW CHANGE
    {
        try {
            Set<MeasuringInstrumentDTO> measuingInstruments = measuingInstrumentRepository.getInstrumentByDeviceId(device_id);
            for (MeasuringInstrumentDTO measuingInstrument : measuingInstruments) {
                List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributes = getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuingInstrument.getId());
                String value = instrumentFormula.getValuebyMeasuringParameter(measuingInstrument.getCalculation_type(), measuingInstrument.getType(), measuingInstrument.getValue(),
                        measuingInstrument.getUnit(), measuringInstrumentAttributes);
                if (value != null && !value.equals(measuingInstrument.getValue())) {
                    BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
                    updateInstrumentValueById(measuingInstrument.getId(), value, timestamp, measuingInstrument.getSensor_type());
                }
            }

        } catch (Exception e) {
            log.error("Exception in update MultipleInstrument Value By Formula, Error message : ",e);
        }
    }

    public void updateInstrumentValueById(String measuingInstrument_id, String value, BigInteger timestamp, String sensor_type) {
        measuingInstrumentRepository.updateInstrumentValueById(measuingInstrument_id, value, timestamp);

        //Update alert status
        try {
            conditionsService.updateConditionAlert("measuring_instrument", measuingInstrument_id, "", value, "", "sync");
            // Send value data to IOC for IAQ asset group
            iocService.sendSensorValueDataToIOC(measuingInstrument_id,timestamp);

            socketService.socketMeasuringInstrumentSensorValueUpdate(measuingInstrument_id);
        } catch (Exception e) {
            log.error("error updating measuring_instrument condition status: ", e);
        }

        try {

            if (value != null && (!(value.equals("")))) {
                rabbitmqService.rabbitmqMeasuringInstrumentData(measuingInstrument_id, value, timestamp, sensor_type);
            }

        } catch (Exception e) {
            log.error("error updating MeasuringInstrument rabbitmq value update: ", e);
        }
    }


    public Integer getInstrumentCountByDeviceId(String device_id) {
        return measuingInstrumentRepository.getInstrumentCountByDeviceId(device_id);
    }


    //used for backend syncing
//    public void syncInstrument(String username, String vdmsid, String device_id, Integer updateAttributes) {
//        Set<MeasuringInstrumentDTO> measuringInstrument = measuingInstrumentRepository.getInstrumentByDeviceId(device_id);
//        if (measuringInstrument != null && measuringInstrument.size() > 0) {
//            List<MeasuringInstrumentDTO> fetchInstrument = apiCallService.fetchMeasuringInstruments();
//            if (fetchInstrument != null && fetchInstrument.size() > 0) {
//                for (MeasuringInstrumentDTO inputInstrument : measuringInstrument) {
//                    for (MeasuringInstrumentDTO fetchedInstruments : fetchInstrument) {
//                        if (inputInstrument.getType().equals(fetchedInstruments.getType())) {
//                            if (updateAttributes == 1) {
//                                Integer updateStatus = measuingInstrumentRepository.syncMeasuringInstrument(fetchedInstruments.getName(), fetchedInstruments.getDescription(), fetchedInstruments.getCalculation_type(), fetchedInstruments.getAttribute(), fetchedInstruments.getParameter(), fetchedInstruments.getCategory(), fetchedInstruments.getValue(), fetchedInstruments.getUnit(), fetchedInstruments.getTags(), fetchedInstruments.getTimestamp(), fetchedInstruments.getType(), inputInstrument.getId());
//                                if (updateStatus == 1) {
//                                    updateMeasuringInstrumentValueByFormula(inputInstrument.getId(), 0);
//                                }
//                                break;
//                            } else {
//                                measuingInstrumentRepository.syncMeasuringInstrumentExceptAttributes(fetchedInstruments.getCalculation_type(), fetchedInstruments.getParameter(), fetchedInstruments.getCategory(), fetchedInstruments.getUnit(), fetchedInstruments.getTags(), fetchedInstruments.getType(), inputInstrument.getId());
//                                break;
//
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    public Set<String> getUniqueSensorCategoryByFloor(String floorid) {
        return measuingInstrumentRepository.getUniqueSensorCategoryByFloor(floorid);
    }

    public Set<CategorySensorDTO> getSensorCategoryByFloor(String floorid, String category) {
        return measuingInstrumentRepository.getSensorCategoryByFloor(floorid, category);
    }

    public Integer getSensorCategoryByFloorCount(String floorid, String category) {
        return measuingInstrumentRepository.getSensorCategoryByFloorCount(floorid, category);
    }

    public List<CategorySensorDTO> getSensorCategoryByFloorPagination(String floorid, String category, Integer pagesize, Integer offset) {
        return measuingInstrumentRepository.getSensorCategoryByFloorPagination(floorid, category, pagesize, offset);
    }


//	public void multiUpdateInstrument(String username, String vdmsid, Set<MeasuringInstrumentDTO> instruments) {
//
//		for(MeasuringInstrumentDTO instrument : instruments)
//		{
//			updateInstrumentValueById(instrument.getId(),instrument.getValue());
//		}
//
//	}


    public void updateMeasuringInstrumentSensorAlert(String measuring_instrument_id, Boolean newAlert) {
        measuingInstrumentRepository.updateMeasuringInstrumentSensorAlert(measuring_instrument_id, newAlert);
    }

    public Map<String, Integer> getMeasuringInstrumentsAlertsCount() {
        Map<String, Integer> measuringinstrumentAlertCount = new HashMap<>();
        measuringinstrumentAlertCount.put("measuring_instrument_with_alert_count", measuingInstrumentRepository.getMeasuringInstrumentAlertSensorCount(true));
        measuringinstrumentAlertCount.put("measuring_instrument_without_alert_count", measuingInstrumentRepository.getMeasuringInstrumentAlertSensorCount(false));

        return measuringinstrumentAlertCount;
    }


    //get measuring instrument sensor by id for all required platforms
    public MeasuringInstrumentDetailsDTO getMeasuringInstrumentSensorDetailsById(String measuring_instrument_id) {
        MeasuringInstrumentDetailsDTO measuringInstrumentDetails = measuingInstrumentRepository.getMeasuringInstrumentSensorDetailsById(measuring_instrument_id);
        measuringInstrumentDetails.setMeasuring_instrument_attributes(getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuringInstrumentDetails.getId()));
        return measuringInstrumentDetails;
    }


    //update measuring instrument user data value
    public void updateMeasuringinstrumentSensorUserDataValue(String measuring_instrument_id, String user_data_value) {
        measuingInstrumentRepository.updateMeasuringinstrumentSensorUserDataValue(measuring_instrument_id, user_data_value);
    }

    // get device id by measuring instrument sensor id
    public String getDeviceIdByMeasuringInstrumentSensorId(String measuring_instrument_id) {
        return measuingInstrumentRepository.getDeviceIdByMeasuringInstrumentSensorId(measuring_instrument_id);
    }

    // get measuring instrument alert status by device id
    public Boolean getMeasuringInstrumentAlertStatusByDeviceId(String device_id) {
        Integer measuring_instrument_alert_count = measuingInstrumentRepository.getMeasuringInstrumentAlertCountDeviceId(device_id, true);

        if (measuring_instrument_alert_count > 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getMeasuringInstrumentSensorCurrentValue(String measuring_instrument_id) {
        return measuingInstrumentRepository.getMeasuringInstrumentSensorCurrentValue(measuring_instrument_id);
    }

    public MeasuringInstrumentDTO getMeasuringInstrumentSensorById(String username, String vdmsid, String measuring_instrument_id) {
        MeasuringInstrumentDTO measuringInstrument = measuingInstrumentRepository.getMeasuringInstrumentSensorById(measuring_instrument_id);
        measuringInstrument.setMeasuring_instrument_attributes(getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuringInstrument.getId()));
        return measuringInstrument;
    }

    public List<MeasuringInstrumentDTO> getAllMeasuringInstrumentDeviceByPagination(String username, String vdmsid, String searchkey, Integer pageno, Integer pagesize) {
        Integer offset = pagesize * (pageno - 1);
        List<MeasuringInstrumentDTO> measuringInstruments = measuingInstrumentRepository.getAllMeasuringInstrumentDeviceByPagination(searchkey, pagesize, offset);
        for (MeasuringInstrumentDTO measuringInstrument : measuringInstruments) {
            measuringInstrument.setMeasuring_instrument_attributes(getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuringInstrument.getId()));
        }
        return measuringInstruments;
    }

    public List<SensorDTO> getMeasuringInstrumentSensorsByDeviceId(String device_id) {

        return measuingInstrumentRepository.getmeasuringInstrumentsByDeviceId(device_id);

    }

    public Set<AnalyticSensorDTO> getAnalyticsMeasuringInstruments(String category, String searchkey, Integer pageno, Integer pagesize, String report_template_id) {
        Integer offset = pagesize * (pageno - 1);
        return measuingInstrumentRepository.getAnalyticsMeasuringInstruments(category, searchkey, pagesize, offset, report_template_id);
    }

    public List<ConditionsDTO> listMeasuringIntrumentDevicesAlertMessagesByDeviceIds(List<String> ids) {
        return measuingInstrumentRepository.listMeasuringIntrumentDevicesAlertMessagesByDevice(ids);
    }

    public SensorAlertDTO getMeasuringInstrumentAlertDetails(String measuring_instrument_id) {
        return measuingInstrumentRepository.getMeasuringInstrumentAlertDetails(measuring_instrument_id);

    }

    public Set<SensorDTO> getSensorByDeviceId(String deviceid) {
        return measuingInstrumentRepository.getSensorByDeviceId(deviceid);
    }

    public Set<SensorDTO> getSensorByLocationId(String locationid) {
        return measuingInstrumentRepository.getSensorByLocationId(locationid);
    }

    public List<MeasuringInstrument> getMeasuringInstruments() {
        return measuingInstrumentRepository.findAll();
    }

    public void updateAllInstrumentValue(List<MeasuringInstrument> measuringInstruments) {
        if (measuringInstruments != null && measuringInstruments.size() > 0) {
            int batchSize = 500;
            int index = 0;
            for (int i = 0; i < measuringInstruments.size(); i = i + batchSize) {
                if (i + batchSize <= measuringInstruments.size()) {
                    index = i + batchSize - 1;
                } else {
                    index = measuringInstruments.size() - 1;
                }
                System.out.println("index: " + index);
                List<MeasuringInstrument> batchInstruments = measuringInstruments.subList(i, index);
                System.out.println(batchInstruments.toString());
                measuingInstrumentRepository.saveAll(batchInstruments);
            }
        }

    }

    public List<CategorySensorDTO> getSensorCategoryByLocationPagination(String locationid, String category, Integer pagesize, Integer offset) {
        return measuingInstrumentRepository.getSensorCategoryByLocationPagination(locationid, category, pagesize, offset);
    }

    public Integer getSensorCategoryByLocationCount(String locationid, String category) {
        return measuingInstrumentRepository.getSensorCategoryByLocationCount(locationid, category);
    }

    public AnalyticSensorDTO getMeasuringInstrumentsByTemplateId(String measuring_instrument_id, String searchkey, String report_attribute_id) {
        return measuingInstrumentRepository.getMeasuringInstrumentsByTemplateId(measuring_instrument_id, searchkey, report_attribute_id);
    }

    public void deleteMeasuringIntrumentLocationsByLocationId(String location_id) {
        measuingInstrumentRepository.deleteMeasuringIntrumentLocationsByLocationId(location_id);
    }


    public void upsertMeasuringInstrumentLocations(String username, String vdmsid, Set<MeasuringInstrumentDTO> measuringInstrumentDTOS, HttpServletRequest httpServletRequest) {
        for (MeasuringInstrumentDTO measuringInstrumentDTO : measuringInstrumentDTOS) {
            try {
                int instrument_location_count = this.checkMeasuringInstrumentsExists(measuringInstrumentDTO.getId(), measuringInstrumentDTO.getLocation_id());
                log.info("count : " + instrument_location_count);
                if (instrument_location_count == 0) {
                    measuingInstrumentRepository.upsertMeasuringInstrumentLocations(measuringInstrumentDTO.getId(), measuringInstrumentDTO.getLocation_id());
                    userActionLogService.addUserAction(username, "sensors", "ADD", " Added location with id: " + measuringInstrumentDTO.getLocation_id() + " to measuring instrument with id: " + measuringInstrumentDTO.getId(), "success", "sensors_info", measuringInstrumentDTO.getId());
                    log.info("endpoint: {}, upsertMeasuringInstrumentLocations,  params: measuringInstrumentDTO: {} ", httpServletRequest.getRequestURI(), measuringInstrumentDTO);                }
            } catch (Exception e) {
                userActionLogService.addUserAction(username, "sensors", "ADD", " Unable to add location with id: " + measuringInstrumentDTO.getLocation_id() + " to measuring instrument id: " + measuringInstrumentDTO.getId(), "failed", "sensors_info", measuringInstrumentDTO.getId());
                log.error("Exception in ADD Measuring Instrument Locations, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
            }
        }

    }

    public int checkMeasuringInstrumentsExists(String measuring_instrument_id, String location_id) {
        return measuingInstrumentRepository.checkMeasuringInstrumentsExists(measuring_instrument_id, location_id);
    }

    public void untagLocationsFromMeasuringInstruments(String username, String vdmsid, List<MeasuringInstrumentDTO> measuringInstruments,HttpServletRequest httpServletRequest) {

        for (MeasuringInstrumentDTO measuringInstrumentDTO : measuringInstruments) {
            try {
                measuingInstrumentRepository.untagLocationsFromMeasuringInstruments(measuringInstrumentDTO.getId(), measuringInstrumentDTO.getLocation_id());
                userActionLogService.addUserAction(username, "sensors", "UPDATE", " Untagged location with id:" + measuringInstrumentDTO.getLocation_id() + " from measuring instrument with id: " + measuringInstrumentDTO.getId(), "success", "sensors_info", measuringInstrumentDTO.getId());
                log.info("endpoint: {}, untagLocationsFromMeasuringInstruments,  params: measuringInstrumentDTO: {} ", httpServletRequest.getRequestURI(), measuringInstrumentDTO);
            } catch (Exception e) {
                userActionLogService.addUserAction(username, "sensors", "UPDATE", " Unable to untag location with id:" + measuringInstrumentDTO.getLocation_id() + " from measuring instrument with id: " + measuringInstrumentDTO.getId(), "failed", "sensors_info", measuringInstrumentDTO.getId());
                log.error("Exception in untag Locations From MeasuringInstruments, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
            }
        }
    }


//    public List<DaintreePointsDTO> fetchDaintreePoints(List<DaintreePointsDTO> daintreeDevicePoints) {
//        try {
//            if (daintreeDevicePoints.size() > 0) {
//                return daintreeDevicePoints;
//            } else {
//                List<DaintreePointsDTO> daintreePointsList = new ArrayList<>();
//                List<DaintreeConfigurationDTO> daintreeConfigurations = daintreeService.getDaintreeConfigurations("null");
//                for (DaintreeConfigurationDTO daintreeConfiguration : daintreeConfigurations) {
//                    try {
//                        List<DaintreeDeviceDTO> daintreeDeviceList = daintreeService.getDaintreeDevices(daintreeConfiguration.getId(), null);
//                        if (daintreeDeviceList != null) {
//                            for (DaintreeDeviceDTO daintreeDevice : daintreeDeviceList) {
//                                try {
//                                    List<DaintreePointsDTO> daintreePoints = daintreeService.getDaintreePointsByDaintreeDeviceId(daintreeDevice.getId());
//                                    daintreePointsList.addAll(daintreePoints);
//
//                                } catch (Exception e) {
//                                    System.out.println(e);
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                        System.out.println("DAINTREE SYNC FETCH DAINTREE POINTS ERROR");
//                        System.out.println(e);
//                    }
//                }
//                return daintreePointsList;
//            }
//        } catch (Exception e) {
//            System.out.println("DAINTREE SYNC FETCH DAINTREE POINTS ERROR" + e);
//            return null;
//        }
//    }

    public Set<SensorDTO> getIntegrationSensorByLocationId(String locationid) {
        return measuingInstrumentRepository.getIntegrationSensorByLocationId(locationid);
    }

    public JSONArray getMeasuringInstrumentsAttributes(String id) {
        MeasuringInstrumentDTO measuringInstrumentDTO = measuingInstrumentRepository.getMeasuringInstrumentSensorById(id);
        List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributes = this.getMeasuringInstrumentAttributesByMeasuringInstrumentId(id);
        JSONArray processedData = processData(measuringInstrumentAttributes, measuringInstrumentDTO.getTimestamp());
        if (processedData.isEmpty()) {
            com.alibaba.fastjson.JSONObject jsonObject1 = new com.alibaba.fastjson.JSONObject();
            jsonObject1.put("value", measuringInstrumentDTO.getValue());
            jsonObject1.put("primary_id", measuringInstrumentDTO.getId());
            jsonObject1.put("secondary_id", null);
            jsonObject1.put("tertiary_id", null);
            jsonObject1.put("category", measuringInstrumentDTO.getSensor_type());
            jsonObject1.put("protocol", "measuring_instrument");
            jsonObject1.put("timestamp", measuringInstrumentDTO.getTimestamp());
            processedData.add(jsonObject1);
        }
        return processedData;
    }


    public JSONArray processData(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributes, BigInteger timestamp) {
        JSONArray jsonArray = new JSONArray();
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributes) {
            try {
                log.info(measuringInstrumentAttributesDTO.getType());
                if (measuringInstrumentAttributesDTO.getType().equals("sensor")) {
                    com.alibaba.fastjson.JSONObject jsonObject1 = new com.alibaba.fastjson.JSONObject();
                    jsonObject1.put("value", measuringInstrumentAttributesDTO.getValue());
                    jsonObject1.put("primary_id", measuringInstrumentAttributesDTO.getPrimary_id());
                    jsonObject1.put("secondary_id", (measuringInstrumentAttributesDTO.getSecondary_id().equals("")) ? null : measuringInstrumentAttributesDTO.getSecondary_id());
                    jsonObject1.put("tertiary_id", (measuringInstrumentAttributesDTO.getTertiary_id().equals("")) ? null : measuringInstrumentAttributesDTO.getTertiary_id());
                    jsonObject1.put("category", measuringInstrumentAttributesDTO.getCategory());
                    jsonObject1.put("protocol", measuringInstrumentAttributesDTO.getProtocol());
                    jsonObject1.put("timestamp", timestamp);
                    jsonArray.add(jsonObject1);
                }
            } catch (Exception e) {
                log.error("Exception in process Data: ", e);
            }
        }
        return jsonArray;
    }

    public List<MeasuringInstrumentDTO> getSiemensMeasuringInstruments() {
        List<MeasuringInstrumentDTO> measuringInstruments = measuingInstrumentRepository.getSiemensMeasuringInstruments();
        for (MeasuringInstrumentDTO measuringInstrument : measuringInstruments) {
            measuringInstrument.setMeasuring_instrument_attributes(getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuringInstrument.getId()));
        }
        return measuringInstruments;
    }

    public List<MeasuringInstrumentDTO> getMeasuringInstrumentsByDeviceId(String deviceId) {
        List<MeasuringInstrumentDTO> measuringInstruments = measuingInstrumentRepository.getMeasuringInstrumentsByDeviceId(deviceId);
        for (MeasuringInstrumentDTO measuringInstrument : measuringInstruments) {
            measuringInstrument.setMeasuring_instrument_attributes(getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuringInstrument.getId()));
        }
        return measuringInstruments;
    }


    public void updateMeasuringInstrumentDeviceId(String device_id, String existing_device_id, Set<String> retainDevices) {
        measuingInstrumentRepository.updateMeasuringInstrumentDeviceId(device_id, existing_device_id);
        deviceService.updateDeviceMeasureCountByDeviceId(device_id);
        //update device measuring instrument alert status
        deviceService.updateDeviceMeasuringInstrumentStatusByDeviceId(device_id);
        if (!retainDevices.isEmpty() && retainDevices.contains(existing_device_id)) {
            deviceService.updateDeviceMeasureCountByDeviceId(existing_device_id);
            deviceService.updateDeviceMeasuringInstrumentStatusByDeviceId(existing_device_id);
        }

    }

    public void updateInstrumentValueAndAttributeById(String id, String value, BigInteger timeStamp, String attributes) {
        measuingInstrumentRepository.updateInstrumentValueAndAttributeById(id, value, timeStamp, attributes);
    }

    public void upsertMeasuringInstrument(MeasuringInstrumentDTO instrument) {

        if (instrument.getMeasuring_instrument_attributes() != null && instrument.getMeasuring_instrument_attributes().size() > 0) {
            measuingInstrumentRepository.upsertInstrument(instrument.getId(), instrument.getType(), instrument.getName(),
                    instrument.getDescription(), instrument.getCalculation_type(), instrument.getAttribute(), instrument.getParameter(), instrument.getCategory(),
                    instrument.getValue(), instrument.getUnit(), instrument.getTags(), instrument.getDevice_id(),
                    instrument.getSensor_type(), instrument.getShow_on_map(), instrument.getShow_on_scan(),
                    instrument.getMeasuring_entity(), instrument.getSub_category(), instrument.getDigital_twin_position(), instrument.getScale_type());

            upsertMeasuringInstrumentAttributes(instrument.getId(), instrument.getMeasuring_instrument_attributes());

        }

    }

    public void updateDeviceMeasureCountByDeviceIds(Set<String> device_ids) {
        for (String device : device_ids) {
            deviceService.updateDeviceMeasureCountByDeviceId(device);

        }
    }

    public void deleteDigitalTwinPositions(String device_id) {
        measuingInstrumentRepository.deleteDigitalTwinPositions(device_id);
    }

    public void updateBacnetMeasuringIntrumentParametersByIds(String protocol, String primary_id, String secondary_id, String value, List<Map<String, Object>> measuringInstrumentList) {
        try {
            for (Map<String, Object> measuringInstrument : measuringInstrumentList) {
                try {
                    if (measuringInstrument.containsKey("id") && measuringInstrument.get("id") != null && measuringInstrument.containsKey("measuring_instrument_attributes") && measuringInstrument.get("measuring_instrument_attributes") != null) {
                        JSONArray attributesArray = JSONArray.parseArray(measuringInstrument.get("measuring_instrument_attributes").toString());

                        for (int i = 0; i < attributesArray.size(); i++) {
                            try {
                                com.alibaba.fastjson.JSONObject attribute = attributesArray.getJSONObject(i);
                                log.info("Object " + i + ": " + attribute);
                                if (attribute.getString("protocol") != null && attribute.getString("primary_id") != null && attribute.getString("secondary_id") != null) {
                                    if (attribute.getString("protocol").equals(protocol) && attribute.getString("primary_id").equals(primary_id) && attribute.getString("secondary_id").equals(secondary_id)) {
//                                        measuringInstrumentAttributesRepository.updateAttributeValueById(value, measuringInstrument.get("id").toString());

                                    }
                                }
                            } catch (Exception e) {
                                log.error("Exception in update Bacnet MeasuringIntrument Parameters ByIds: ", e);
                            }
                        }
                        updateMeasuringInstrumentValueByFormula(measuringInstrument.get("id").toString(), 1);
                    }
                } catch (Exception e) {
                    log.error("Exception in update Bacnet MeasuringIntrument Parameters ByIds: ", e);
                }
            }
        } catch (Exception e) {
            log.error("Exception in update Bacnet MeasuringIntrument Parameters ByIds: ", e);
        }
    }

    public void upsertMeasuringInstrumentAttribute(MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO) {
        if (measuringInstrumentAttributesDTO.getId() == null || measuringInstrumentAttributesDTO.getId().equals("")) {
            measuringInstrumentAttributesDTO.setId(Generators.timeBasedGenerator().generate().toString());
        }
        measuringInstrumentAttributesRepository.upsertMeasuringInstrumentAttribute(measuringInstrumentAttributesDTO.getId(), measuringInstrumentAttributesDTO.getName(), measuringInstrumentAttributesDTO.getType(), measuringInstrumentAttributesDTO.getUnit(), measuringInstrumentAttributesDTO.getValue(), measuringInstrumentAttributesDTO.getProtocol(), measuringInstrumentAttributesDTO.getCategory(), measuringInstrumentAttributesDTO.getPrimary_id(), measuringInstrumentAttributesDTO.getSecondary_id(), measuringInstrumentAttributesDTO.getTertiary_id(), measuringInstrumentAttributesDTO.getMeasuring_instrument_id(), measuringInstrumentAttributesDTO.getAttribute_index());
    }

    private void upsertMeasuringInstrumentAttributes(String measuringInstrumentId, List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributes) {
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttribute : measuringInstrumentAttributes) {
            measuringInstrumentAttribute.setMeasuring_instrument_id(measuringInstrumentId);
            upsertMeasuringInstrumentAttribute(measuringInstrumentAttribute);
        }
    }

    public MeasuringInstrumentAttributesDTO getMeasuringInstrumentAttributeById(String id) {
        return measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributeById(id);
    }

    public List<MeasuringInstrumentAttributesDTO> getAllMeasuringInstrumentAttributes() {
        return measuringInstrumentAttributesRepository.getAllMeasuringInstrumentAttributes();
    }

    public List<MeasuringInstrumentAttributesDTO> getMeasuringInstrumentAttributesByMeasuringInstrumentId(String measuringInstrumentId) {
        return measuringInstrumentAttributesRepository.getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuringInstrumentId);
    }

//    public void deleteMeasuringInstrumentAttributeById(String id) {
//        measuringInstrumentAttributesRepository.deleteMeasuringInstrumentAttributeById(id);
//    }
//
//    public void deleteMeasuringInstrumentAttributeByMeasuringInstrumentId(String measuringInstrumentId) {
//        measuringInstrumentAttributesRepository.deleteMeasuringInstrumentAttributeByMeasuringInstrumentId(measuringInstrumentId);
//    }
//
//    public void updateMeasuringIntrumentParametersByIds(String protocol, String primary_id, String secondary_id, String value) {
//        log.info("protocol : " + protocol + "    || primary_id : " + primary_id + "  || secondary_id : " + secondary_id + "  || value : " + value);
//        measuringInstrumentAttributesRepository.updateAttributeValue(protocol, primary_id, secondary_id, value);
//        Set<String> measuringInstrumentIds = measuringInstrumentAttributesRepository.getMeasuringInstrumentIdByIds(protocol, primary_id, secondary_id);
//        for (String measuringInstrumentId : measuringInstrumentIds) {
//            updateMeasuringInstrumentValueByFormula(measuringInstrumentId, 1);
//        }
//    }

    public void updateMeasuringInstrumentValueByFormula(String id, Integer value_changed_status) {
        try {
            MeasuringInstrumentDTO measuingInstrument = measuingInstrumentRepository.getInstrumentByInstrumentId(id);
            List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributes = getMeasuringInstrumentAttributesByMeasuringInstrumentId(measuingInstrument.getId());

            String presentValue = instrumentFormula.getValuebyMeasuringParameter(measuingInstrument.getCalculation_type(), measuingInstrument.getType(), measuingInstrument.getValue(),
                    measuingInstrument.getUnit(), measuringInstrumentAttributes);
            log.info("PRESENT VALUE :" + presentValue + " OLD: " + measuingInstrument.getValue());
            if (presentValue != null && ((value_changed_status != null && value_changed_status.equals(1)) || !presentValue.equals(measuingInstrument.getValue()))) {
                BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
                updateInstrumentValueById(measuingInstrument.getId(), presentValue, timestamp, measuingInstrument.getSensor_type());

            }
        } catch (Exception e) {
            log.error("Exception in update MeasuringInstrument Value By Formula: ", e);
        }
    }

    // MIA changes
    public Set<String> getMeasuringInstrumentIdsByProtocolAndPrimaryIds(String protocol, Set<String> primaryIds) {
        return measuingInstrumentRepository.getMeasuringInstrumentIdsByProtocolAndPrimaryIds(protocol, primaryIds);
    }

    public Integer getMeasuringInstrumentCountByType(String type) {
        return measuingInstrumentRepository.getMeasuringInstrumentCountByType(type);
    }

    public void upsertMeasuringInstrumentForMultiDigitaltwin(MeasuringInstrumentDTO instrument, String measuring_instrument_id) {

        if (instrument.getMeasuring_instrument_attributes() != null && instrument.getMeasuring_instrument_attributes().size() > 0) {

            measuingInstrumentRepository.upsertInstrument(measuring_instrument_id, instrument.getType(), instrument.getName(),
                    instrument.getDescription(), instrument.getCalculation_type(), instrument.getAttribute(), instrument.getParameter(), instrument.getCategory(),
                    instrument.getValue(), instrument.getUnit(), instrument.getTags(), instrument.getDevice_id(),
                    instrument.getSensor_type(), instrument.getShow_on_map(), instrument.getShow_on_scan(),
                    instrument.getMeasuring_entity(), instrument.getSub_category(), instrument.getDigital_twin_position(), instrument.getScale_type());

            upsertMeasuringInstrumentAttributesForMultiDigitaltwin(measuring_instrument_id, instrument.getMeasuring_instrument_attributes());

        }

    }

    private void upsertMeasuringInstrumentAttributesForMultiDigitaltwin(String measuringInstrumentId, List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributes) {
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttribute : measuringInstrumentAttributes) {
            measuringInstrumentAttribute.setMeasuring_instrument_id(measuringInstrumentId);
            upsertMeasuringInstrumentAttributeForMultiDigitaltwin(measuringInstrumentAttribute);
        }
    }

    public void upsertMeasuringInstrumentAttributeForMultiDigitaltwin(MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO) {
        String measuring_instrumnet_attribute_id = measuringInstrumentAttributesDTO.getId();
        if (measuringInstrumentAttributesDTO.getId() == null || measuringInstrumentAttributesDTO.getId().equals("")) {
            measuring_instrumnet_attribute_id = Generators.timeBasedGenerator().generate().toString();
        }
        measuringInstrumentAttributesRepository.upsertMeasuringInstrumentAttribute(measuring_instrumnet_attribute_id, measuringInstrumentAttributesDTO.getName(), measuringInstrumentAttributesDTO.getType(), measuringInstrumentAttributesDTO.getUnit(), measuringInstrumentAttributesDTO.getValue(), measuringInstrumentAttributesDTO.getProtocol(), measuringInstrumentAttributesDTO.getCategory(), measuringInstrumentAttributesDTO.getPrimary_id(), measuringInstrumentAttributesDTO.getSecondary_id(), measuringInstrumentAttributesDTO.getTertiary_id(), measuringInstrumentAttributesDTO.getMeasuring_instrument_id(), measuringInstrumentAttributesDTO.getAttribute_index());
    }
}
