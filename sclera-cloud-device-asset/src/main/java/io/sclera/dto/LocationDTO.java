package io.sclera.dto;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationDTO {

    private String location_id;
    private String name;
    private String position;
    private String floor_id;
    private String area;

    private Integer z_index;

    private String floor_name;
    private String building_name;

    private Integer is_added;
    private String status;
    private String global_qrcode_id;
    private String record_checklist_status;
    private Integer record_checklist_count;
    private String nfc_id;

    private String clean_status;
    private String occupancy_status;
    private String building_id;

    List<CategorySensorDTO> sensors;
    JSONObject counts;

    private String measuring_instrument_id;

    private String type;

    private String code;

    private String building_code;
    private String barcode_id;

    private String id;
    private String floorId;

    private BigInteger updatedTimestamp;

    public BigInteger getUpdatedTimestamp() {return updatedTimestamp;}
    public void setUpdatedTimestamp(BigInteger updatedTimestamp) {this.updatedTimestamp = updatedTimestamp;}

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(String floor_id) {
        this.floor_id = floor_id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getZ_index() {
        return z_index;
    }

    public void setZ_index(Integer z_index) {
        this.z_index = z_index;
    }

    public String getFloor_name() {
        return floor_name;
    }

    public void setFloor_name(String floor_name) {
        this.floor_name = floor_name;
    }

    public String getBuilding_name() {
        return building_name;
    }

    public void setBuilding_name(String building_name) {
        this.building_name = building_name;
    }

    public List<CategorySensorDTO> getSensors() {
        return sensors;
    }

    public void setSensors(List<CategorySensorDTO> sensors) {
        this.sensors = sensors;
    }

    public Integer getIs_added() {
        return is_added;
    }

    public void setIs_added(Integer is_added) {
        this.is_added = is_added;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGlobal_qrcode_id() {
        return global_qrcode_id;
    }

    public void setGlobal_qrcode_id(String global_qrcode_id) {
        this.global_qrcode_id = global_qrcode_id;
    }

    public String getRecord_checklist_status() {
        return record_checklist_status;
    }

    public void setRecord_checklist_status(String record_checklist_status) {
        this.record_checklist_status = record_checklist_status;
    }

    public Integer getRecord_checklist_count() {
        return record_checklist_count;
    }

    public void setRecord_checklist_count(Integer record_checklist_count) {
        this.record_checklist_count = record_checklist_count;
    }

    public String getNfc_id() {
        return nfc_id;
    }

    public void setNfc_id(String nfc_id) {
        this.nfc_id = nfc_id;
    }

    public String getClean_status() {
        return clean_status;
    }

    public void setClean_status(String clean_status) {
        this.clean_status = clean_status;

    }

    public String getOccupancy_status() {
        return occupancy_status;
    }

    public void setOccupancy_status(String occupancy_status) {
        this.occupancy_status = occupancy_status;
    }

    public JSONObject getCounts() {
        return counts;
    }

    public void setCounts(JSONObject counts) {
        this.counts = counts;
    }

    public String getBuilding_id() {
        return building_id;
    }

    public void setBuilding_id(String building_id) {
        this.building_id = building_id;
    }

    public String getMeasuring_instrument_id() {
        return measuring_instrument_id;
    }

    public void setMeasuring_instrument_id(String measuring_instrument_id) {
        this.measuring_instrument_id = measuring_instrument_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getBuilding_code() {
        return building_code;
    }

    public void setBuilding_code(String building_code) {
        this.building_code = building_code;
    }

    public String getBarcode_id() {
        return barcode_id;
    }

    public void setBarcode_id(String barcode_id) {
        this.barcode_id = barcode_id;
    }

    public LocationDTO() {
    }


    //locationmapping
    public LocationDTO(String location_id, String name, String position, String area, String floor_id, String status, Integer z_index,
                       Integer record_checklist_count, String record_checklist_status, String type, String floor_name, String building_id, String building_name, String code, String building_code) {
        this.location_id = location_id;
        this.name = name;
        this.position = position;
        this.area = area;
        this.floor_id = floor_id;
        this.status = status;
        this.z_index = z_index;
        this.record_checklist_count = record_checklist_count;
        this.record_checklist_status = record_checklist_status;
        this.type = type;
        this.floor_name = floor_name;
        this.building_id = building_id;
        this.building_name = building_name;
        this.code = code;
        this.building_code = building_code;
    }

    //locationdetailsmapping
    public LocationDTO(String location_id, String name, String floor_name, String building_name, String type, String code) {
        this.location_id = location_id;
        this.name = name;
        this.floor_name = floor_name;
        this.building_name = building_name;
        this.type = type;
        this.code = code;

    }


    //locationsmapping
    public LocationDTO(String location_id, String name, String type, String code) {
        this.location_id = location_id;
        this.name = name;
        this.type = type;
        this.code = code;
    }

    //locationdetailsbylocationidmapping
    public LocationDTO(String location_id, String name, String position, String area, String floor_id, Integer z_index,
                       String floor_name, String building_name, Integer record_checklist_count, String record_checklist_status, String building_id, String type, String code) {
        this.location_id = location_id;
        this.name = name;
        this.position = position;
        this.area = area;
        this.floor_id = floor_id;
        this.z_index = z_index;
        this.floor_name = floor_name;
        this.building_name = building_name;
        this.record_checklist_count = record_checklist_count;
        this.record_checklist_status = record_checklist_status;
        this.building_id = building_id;
        this.type = type;
        this.code = code;

    }

    //locationdetailsbymeasuringinstrumentid
    public LocationDTO(String location_id, String name, String position, String area, String floor_id, Integer z_index,
                       String floor_name, String building_name, String measuring_instrument_id, String type) {
        this.location_id = location_id;
        this.name = name;
        this.position = position;
        this.area = area;
        this.floor_id = floor_id;
        this.z_index = z_index;
        this.floor_name = floor_name;
        this.building_name = building_name;
        this.measuring_instrument_id = measuring_instrument_id;
        this.type = type;
    }


    //isaddedlocationsmapping
    public LocationDTO(String location_id, String name, Integer is_added, String building_name, String floor_name, String type) {
        super();
        this.location_id = location_id;
        this.name = name;
        this.is_added = is_added;
        this.building_name = building_name;
        this.floor_name = floor_name;
        this.type = type;
    }


    //exportlocationmapping
    public LocationDTO(String location_id, String name, String floor_id, String floor_name, String building_name, String building_id, String type,
                       String code, String building_code) {
        this.location_id = location_id;
        this.name = name;
        this.floor_id = floor_id;
        this.floor_name = floor_name;
        this.building_name = building_name;
        this.building_id = building_id;
        this.type = type;
        this.code = code;
        this.building_code = building_code;
    }

    //recordchecklistcountbylocationtypemapping
    public LocationDTO(String record_checklist_status, Integer record_checklist_count, String location_id) {
        this.record_checklist_status = record_checklist_status;
        this.record_checklist_count = record_checklist_count;
        this.location_id = location_id;
    }

    public LocationDTO(String id, String name, String type, String code, String floorId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.code = code;
        this.floorId = floorId;
    }

    @Override
    public String toString() {
        return "LocationDTO{" +
                "location_id='" + location_id + '\'' +
                ", name='" + name + '\'' +
                ", position='" + position + '\'' +
                ", floor_id='" + floor_id + '\'' +
                ", area='" + area + '\'' +
                ", z_index=" + z_index +
                ", floor_name='" + floor_name + '\'' +
                ", building_name='" + building_name + '\'' +
                ", is_added=" + is_added +
                ", status='" + status + '\'' +
                ", global_qrcode_id='" + global_qrcode_id + '\'' +
                ", record_checklist_status='" + record_checklist_status + '\'' +
                ", record_checklist_count=" + record_checklist_count +
                ", nfc_id='" + nfc_id + '\'' +
                ", clean_status='" + clean_status + '\'' +
                ", occupancy_status='" + occupancy_status + '\'' +
                ", building_id='" + building_id + '\'' +
                ", sensors=" + sensors +
                ", counts=" + counts +
                ", measuring_instrument_id='" + measuring_instrument_id + '\'' +
                ", type='" + type + '\'' +
                ", code='" + code + '\'' +
                ", building_code='" + building_code + '\'' +
                '}';
    }
}