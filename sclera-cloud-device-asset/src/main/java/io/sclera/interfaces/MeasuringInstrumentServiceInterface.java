package io.sclera.interfaces;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.AnalyticSensorDTO;
import io.sclera.dto.CategorySensorDTO;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import io.sclera.dto.MeasuringInstrumentDTO;
import io.sclera.dto.MeasuringInstrumentDetailsDTO;
import io.sclera.dto.SensorAlertDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import io.sclera.models.MeasuringInstrument;
import jakarta.servlet.http.HttpServletRequest;

/** Service contract for {@link io.sclera.service.MeasuringInstrumentService}. */
public interface MeasuringInstrumentServiceInterface {

    void upsertInstrument(String username, String vdmsid, String share_method, Set<MeasuringInstrumentDTO> instruments, HttpServletRequest httpServletRequest);

    void deleteInstrumentsByDeviceId(String username, String vdmsid, String device_id, HttpServletRequest httpServletRequest);

    Set<MeasuringInstrumentDTO> getInstrumentByDeviceId(String username, String vdmsid, String device_id);

    Set<MeasuringInstrumentDTO> getInstrumentsByDeviceId(String username, String vdmsid, String device_id);

    Integer getNoOfParametersFromString(String input);

    void deleteInstrumentById(String username, String vdmsid, Set<MeasuringInstrumentDTO> measuring_instruments, HttpServletRequest httpServletRequest);

    List<MeasuringInstrumentDTO> getDaintreeMeasuringIntruments();

    void updateMultipleInstrumentValueByFormula(String device_id);

    void updateInstrumentValueById(String measuingInstrument_id, String value, BigInteger timestamp, String sensor_type);

    Integer getInstrumentCountByDeviceId(String device_id);

    Set<String> getUniqueSensorCategoryByFloor(String floorid);

    Set<CategorySensorDTO> getSensorCategoryByFloor(String floorid, String category);

    Integer getSensorCategoryByFloorCount(String floorid, String category);

    List<CategorySensorDTO> getSensorCategoryByFloorPagination(String floorid, String category, Integer pagesize, Integer offset);

    void updateMeasuringInstrumentSensorAlert(String measuring_instrument_id, Boolean newAlert);

    Map<String, Integer> getMeasuringInstrumentsAlertsCount();

    MeasuringInstrumentDetailsDTO getMeasuringInstrumentSensorDetailsById(String measuring_instrument_id);

    void updateMeasuringinstrumentSensorUserDataValue(String measuring_instrument_id, String user_data_value);

    String getDeviceIdByMeasuringInstrumentSensorId(String measuring_instrument_id);

    Boolean getMeasuringInstrumentAlertStatusByDeviceId(String device_id);

    String getMeasuringInstrumentSensorCurrentValue(String measuring_instrument_id);

    MeasuringInstrumentDTO getMeasuringInstrumentSensorById(String username, String vdmsid, String measuring_instrument_id);

    List<MeasuringInstrumentDTO> getAllMeasuringInstrumentDeviceByPagination(String username, String vdmsid, String searchkey, Integer pageno, Integer pagesize);

    List<SensorDTO> getMeasuringInstrumentSensorsByDeviceId(String device_id);

    Set<AnalyticSensorDTO> getAnalyticsMeasuringInstruments(String category, String searchkey, Integer pageno, Integer pagesize, String report_template_id);

    List<ConditionsDTO> listMeasuringIntrumentDevicesAlertMessagesByDeviceIds(List<String> ids);

    SensorAlertDTO getMeasuringInstrumentAlertDetails(String measuring_instrument_id);

    Set<SensorDTO> getSensorByDeviceId(String deviceid);

    Set<SensorDTO> getSensorByLocationId(String locationid);

    List<MeasuringInstrument> getMeasuringInstruments();

    void updateAllInstrumentValue(List<MeasuringInstrument> measuringInstruments);

    List<CategorySensorDTO> getSensorCategoryByLocationPagination(String locationid, String category, Integer pagesize, Integer offset);

    Integer getSensorCategoryByLocationCount(String locationid, String category);

    AnalyticSensorDTO getMeasuringInstrumentsByTemplateId(String measuring_instrument_id, String searchkey, String report_attribute_id);

    void deleteMeasuringIntrumentLocationsByLocationId(String location_id);

    void upsertMeasuringInstrumentLocations(String username, String vdmsid, Set<MeasuringInstrumentDTO> measuringInstrumentDTOS, HttpServletRequest httpServletRequest);

    int checkMeasuringInstrumentsExists(String measuring_instrument_id, String location_id);

    void untagLocationsFromMeasuringInstruments(String username, String vdmsid, List<MeasuringInstrumentDTO> measuringInstruments, HttpServletRequest httpServletRequest);

    Set<SensorDTO> getIntegrationSensorByLocationId(String locationid);

    JSONArray getMeasuringInstrumentsAttributes(String id);

    JSONArray processData(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributes, BigInteger timestamp);

    List<MeasuringInstrumentDTO> getSiemensMeasuringInstruments();

    List<MeasuringInstrumentDTO> getMeasuringInstrumentsByDeviceId(String deviceId);

    void updateMeasuringInstrumentDeviceId(String device_id, String existing_device_id, Set<String> retainDevices);

    void updateInstrumentValueAndAttributeById(String id, String value, BigInteger timeStamp, String attributes);

    void upsertMeasuringInstrument(MeasuringInstrumentDTO instrument);

    void updateDeviceMeasureCountByDeviceIds(Set<String> device_ids);

    void deleteDigitalTwinPositions(String device_id);

    void updateBacnetMeasuringIntrumentParametersByIds(String protocol, String primary_id, String secondary_id, String value, List<Map<String, Object>> measuringInstrumentList);

    void upsertMeasuringInstrumentAttribute(MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO);

    MeasuringInstrumentAttributesDTO getMeasuringInstrumentAttributeById(String id);

    List<MeasuringInstrumentAttributesDTO> getAllMeasuringInstrumentAttributes();

    List<MeasuringInstrumentAttributesDTO> getMeasuringInstrumentAttributesByMeasuringInstrumentId(String measuringInstrumentId);

    void updateMeasuringInstrumentValueByFormula(String id, Integer value_changed_status);

    Set<String> getMeasuringInstrumentIdsByProtocolAndPrimaryIds(String protocol, Set<String> primaryIds);

    Integer getMeasuringInstrumentCountByType(String type);

    void upsertMeasuringInstrumentForMultiDigitaltwin(MeasuringInstrumentDTO instrument, String measuring_instrument_id);

    void upsertMeasuringInstrumentAttributeForMultiDigitaltwin(MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO);
}
