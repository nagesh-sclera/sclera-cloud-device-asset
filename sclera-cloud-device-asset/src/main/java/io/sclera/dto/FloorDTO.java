package io.sclera.dto;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FloorDTO {

    private String floor_id;
    private String name;
    private String initial_position;
    private String image_url;
    private String building_id;
    private Integer angle;

    private String path;
    private boolean updateAllFloors;

    private String topleft_latitude;
    private String topleft_longitude;
    private String bottomright_latitude;
    private String bottomright_longitude;


    private String min_zoom;
    private String max_zoom;

    private String local_image_url;

    private Set<LocationDTO> locations;

    JSONObject counts;

    private String id;
    private String buildingId;
    private BigInteger updatedTimestamp;

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(String floor_id) {
        this.floor_id = floor_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<LocationDTO> getLocations() {
        return locations;
    }

    public void setLocations(Set<LocationDTO> locations) {
        this.locations = locations;
    }

    public String getBuilding_id() {
        return building_id;
    }

    public void setBuilding_id(String building_id) {
        this.building_id = building_id;
    }

    public String getInitial_position() {
        return initial_position;
    }

    public void setInitial_position(String initial_position) {
        this.initial_position = initial_position;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Integer getAngle() {
        return angle;
    }

    public void setAngle(Integer angle) {
        this.angle = angle;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isUpdateAllFloors() {
        return updateAllFloors;
    }

    public void setUpdateAllFloors(boolean updateAllFloors) {
        this.updateAllFloors = updateAllFloors;
    }

    public String getTopleft_latitude() {
        return topleft_latitude;
    }

    public void setTopleft_latitude(String topleft_latitude) {
        this.topleft_latitude = topleft_latitude;
    }

    public String getTopleft_longitude() {
        return topleft_longitude;
    }

    public void setTopleft_longitude(String topleft_longitude) {
        this.topleft_longitude = topleft_longitude;
    }

    public String getBottomright_latitude() {
        return bottomright_latitude;
    }

    public void setBottomright_latitude(String bottomright_latitude) {
        this.bottomright_latitude = bottomright_latitude;
    }

    public String getBottomright_longitude() {
        return bottomright_longitude;
    }

    public void setBottomright_longitude(String bottomright_longitude) {
        this.bottomright_longitude = bottomright_longitude;
    }

    public String getMin_zoom() {
        return min_zoom;
    }

    public void setMin_zoom(String min_zoom) {
        this.min_zoom = min_zoom;
    }

    public String getMax_zoom() {
        return max_zoom;
    }

    public void setMax_zoom(String max_zoom) {
        this.max_zoom = max_zoom;
    }

    public String getLocal_image_url() {
        return local_image_url;
    }

    public void setLocal_image_url(String local_image_url) {
        this.local_image_url = local_image_url;
    }

    public JSONObject getCounts() {
        return counts;
    }

    public void setCounts(JSONObject counts) {
        this.counts = counts;
    }

    public FloorDTO() {
    }

    public FloorDTO(String id, String name, String buildingId) {
        super();
        this.id = id;
        this.name = name;
        this.buildingId = buildingId;
    }

    public FloorDTO(String floor_id, String name, String initial_position, String image_url, String building_id, Integer angle,
                    String min_zoom, String max_zoom, String local_image_url) {
        this.floor_id = floor_id;
        this.name = name;
        this.initial_position = initial_position;
        this.image_url = image_url;
        this.building_id = building_id;
        this.angle = angle;
        this.min_zoom = min_zoom;
        this.max_zoom = max_zoom;
        this.local_image_url = local_image_url;
    }


    public FloorDTO(String floor_id, String name) {
        this.floor_id = floor_id;
        this.name = name;
    }

    public FloorDTO(String id, String name, String building_id, String floor_id) {
        super();
        this.id = id;
        this.name = name;
        this.building_id = building_id;
        this.floor_id = floor_id;
    }

    @Override
    public String toString() {
        return "FloorDTO{" +
                "floor_id='" + floor_id + '\'' +
                ", name='" + name + '\'' +
                ", initial_position='" + initial_position + '\'' +
                ", image_url='" + image_url + '\'' +
                ", building_id='" + building_id + '\'' +
                ", angle=" + angle +
                ", path='" + path + '\'' +
                ", updateAllFloors=" + updateAllFloors +
                ", topleft_latitude='" + topleft_latitude + '\'' +
                ", topleft_longitude='" + topleft_longitude + '\'' +
                ", bottomright_latitude='" + bottomright_latitude + '\'' +
                ", bottomright_longitude='" + bottomright_longitude + '\'' +
                ", min_zoom='" + min_zoom + '\'' +
                ", max_zoom='" + max_zoom + '\'' +
                ", local_image_url='" + local_image_url + '\'' +
                ", locations=" + locations +
                ", counts=" + counts +
                '}';
    }
}
