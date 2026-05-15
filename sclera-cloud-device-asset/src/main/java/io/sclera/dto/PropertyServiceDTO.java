package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyServiceDTO {
    private String id;
    private String name;
    private String vdms_id;
    private Set<PropertyServiceRequestDTO> property_service_requests;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVdms_id() {
        return vdms_id;
    }

    public void setVdms_id(String vdms_id) {
        this.vdms_id = vdms_id;
    }

    public Set<PropertyServiceRequestDTO> getProperty_service_requests() {
        return property_service_requests;
    }

    public void setProperty_service_requests(Set<PropertyServiceRequestDTO> property_service_requests) {
        this.property_service_requests = property_service_requests;
    }

    public PropertyServiceDTO() {
    }

    public PropertyServiceDTO(String id, String name, String vdms_id) {
        this.id = id;
        this.name = name;
        this.vdms_id = vdms_id;
    }
}
