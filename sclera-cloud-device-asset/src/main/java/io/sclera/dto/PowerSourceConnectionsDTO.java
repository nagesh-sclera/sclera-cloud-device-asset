
package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PowerSourceConnectionsDTO {

    private String source_device_id;
    private String target_device_id;
    private String source_specifications_name;
    private String target_specifications_name;
    private String source_specifications_id;
    private String target_specifications_id;


    public String getSource_device_id() {
        return source_device_id;
    }

    public void setSource_device_id(String source_device_id) {
        this.source_device_id = source_device_id;
    }

    public String getTarget_device_id() {
        return target_device_id;
    }

    public void setTarget_device_id(String target_device_id) {
        this.target_device_id = target_device_id;
    }

    public String getSource_specifications_name() {
        return source_specifications_name;
    }

    public void setSource_specifications_name(String source_specifications_name) {
        this.source_specifications_name = source_specifications_name;
    }

    public String getTarget_specifications_name() {
        return target_specifications_name;
    }

    public void setTarget_specifications_name(String target_specifications_name) {
        this.target_specifications_name = target_specifications_name;
    }

    public String getSource_specifications_id() {
        return source_specifications_id;
    }

    public void setSource_specifications_id(String source_specifications_id) {
        this.source_specifications_id = source_specifications_id;
    }

    public String getTarget_specifications_id() {
        return target_specifications_id;
    }

    public void setTarget_specifications_id(String target_specifications_id) {
        this.target_specifications_id = target_specifications_id;
    }


    public PowerSourceConnectionsDTO() {
    }

    public PowerSourceConnectionsDTO(String source_device_id, String target_device_id, String source_specifications_name, String target_specifications_name, String source_specifications_id, String target_specifications_id) {
        this.source_device_id = source_device_id;
        this.target_device_id = target_device_id;
        this.source_specifications_name = source_specifications_name;
        this.target_specifications_name = target_specifications_name;
        this.source_specifications_id = source_specifications_id;
        this.target_specifications_id = target_specifications_id;
    }


    @Override
    public String toString() {
        return "PowerSourceConnectionsDTO{" +
                "source_device_id='" + source_device_id + '\'' +
                ", target_device_id='" + target_device_id + '\'' +
                ", source_specifications_name='" + source_specifications_name + '\'' +
                ", target_specifications_name='" + target_specifications_name + '\'' +
                ", source_specifications_id='" + source_specifications_id + '\'' +
                ", target_specifications_id='" + target_specifications_id + '\'' +
                '}';
    }


}
