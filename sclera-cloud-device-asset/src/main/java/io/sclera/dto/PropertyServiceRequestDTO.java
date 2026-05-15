package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyServiceRequestDTO {
    private String id;
    private String label;
    private String options;
    private String type;
    private String property_service_id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProperty_service_id() {
        return property_service_id;
    }

    public void setProperty_service_id(String property_service_id) {
        this.property_service_id = property_service_id;
    }



    public PropertyServiceRequestDTO() {
    }

    public PropertyServiceRequestDTO(String id, String label, String options, String type, String property_service_id) {
        this.id = id;
        this.label = label;
        this.options = options;
        this.type = type;
        this.property_service_id = property_service_id;
    }

    @Override
    public String toString() {
        return "PropertyServiceRequestDTO{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", options='" + options + '\'' +
                ", type='" + type + '\'' +
                ", property_service_id='" + property_service_id + '\'' +
                '}';
    }
}
