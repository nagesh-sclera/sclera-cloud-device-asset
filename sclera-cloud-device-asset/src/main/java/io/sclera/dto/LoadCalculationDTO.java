
package io.sclera.dto;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class
LoadCalculationDTO {

    private String id;
    private String key_name;
    private String device_id;

    private JSONObject power;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey_name() {
        return key_name;
    }

    public void setKey_name(String key_name) {
        this.key_name = key_name;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public JSONObject getPower() {
        return power;
    }

    public void setPower(JSONObject power) {
        this.power = power;
    }


    public LoadCalculationDTO() {
    }


    public LoadCalculationDTO(String id, String key_name, String device_id, JSONObject power) {
        this.id = id;
        this.key_name = key_name;
        this.device_id = device_id;
        this.power = power;
    }
}
