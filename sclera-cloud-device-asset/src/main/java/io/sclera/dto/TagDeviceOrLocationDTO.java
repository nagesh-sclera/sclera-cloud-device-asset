package io.sclera.dto;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagDeviceOrLocationDTO {
    private JSONObject filter_object;
    private JSONObject general_object;
    private String group;
    private Integer select_all_status;

    public JSONObject getFilter_object() {
        return filter_object;
    }

    public void setFilter_object(JSONObject filter_object) {
        this.filter_object = filter_object;
    }

    public JSONObject getGeneral_object() {
        return general_object;
    }

    public void setGeneral_object(JSONObject general_object) {
        this.general_object = general_object;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getSelect_all_status() {
        return select_all_status;
    }

    public void setSelect_all_status(Integer select_all_status) {
        this.select_all_status = select_all_status;
    }

    @Override
    public String toString() {
        return "TagDeviceOrLocationDTO{" +
                "filter_object=" + filter_object +
                ", general_object=" + general_object +
                ", group='" + group + '\'' +
                ", select_all_status='" + select_all_status + '\'' +
                '}';
    }
}