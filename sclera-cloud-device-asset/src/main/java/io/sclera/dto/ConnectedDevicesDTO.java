
package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectedDevicesDTO {

    private String id;
    private String connected_specifications_id;
    private String specifications_id;
    private String key_name;
    private String device_id;
    private String connected_device;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConnected_specifications_id() {
        return connected_specifications_id;
    }

    public void setConnected_specifications_id(String connected_specifications_id) {
        this.connected_specifications_id = connected_specifications_id;
    }


    public String getSpecifications_id() {
        return specifications_id;
    }

    public void setSpecifications_id(String specifications_id) {
        this.specifications_id = specifications_id;
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

    public String getConnected_device() {
        return connected_device;
    }

    public void setConnected_device(String connected_device) {
        this.connected_device = connected_device;
    }

    public ConnectedDevicesDTO() {
    }

    public ConnectedDevicesDTO(String id, String connected_specifications_id, String specifications_id) {
        this.id = id;
        this.connected_specifications_id = connected_specifications_id;
        this.specifications_id = specifications_id;
    }

    public ConnectedDevicesDTO(String id, String connected_specifications_id, String key_name, String device_id, String connected_device) {
        this.id = id;
        this.connected_specifications_id = connected_specifications_id;
        this.key_name = key_name;
        this.device_id = device_id;
        this.connected_device = connected_device;
    }


    public ConnectedDevicesDTO(String key_name, String device_id) {
        this.key_name = key_name;
        this.device_id = device_id;
    }

}
