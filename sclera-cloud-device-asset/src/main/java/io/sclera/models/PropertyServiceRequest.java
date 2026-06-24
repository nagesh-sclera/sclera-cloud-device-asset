package io.sclera.models;

import io.sclera.dto.PropertyServiceRequestDTO;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Represents a single input field (question) within a property-service inspection form.
 * Belongs to a PropertyService; its submitted answers are stored as PropertyServiceResponse children.
 */
@SqlResultSetMapping(
        name = "propertyservicerequestmapping",
        classes = {
                @ConstructorResult(
                        targetClass = PropertyServiceRequestDTO.class,
                        columns = {
                                @ColumnResult(name = "id",                  type = String.class),
                                @ColumnResult(name = "label",               type = String.class),
                                @ColumnResult(name = "options",             type = String.class),
                                @ColumnResult(name = "type",                type = String.class),
                                @ColumnResult(name = "property_service_id", type = String.class)
                        })
        }
)

@NamedNativeQuery(
        name = "PropertyServiceRequest.getPropertyServiceRequestsByServiceId",
        query = "SELECT psr.id, psr.label, psr.options, psr.type, psr.property_service_id"
                + " FROM property_service_request psr"
                + " WHERE psr.property_service_id = ?1",
        resultSetMapping = "propertyservicerequestmapping")

@Entity
public class PropertyServiceRequest {

    @Id
    private String id;

    private String label;

    private String type;

    private String options;

    @ManyToOne
    private PropertyService property_service;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "property_service_request")
    private Set<PropertyServiceResponse> property_service_response;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public PropertyService getProperty_service() {
        return property_service;
    }

    public void setProperty_service(PropertyService property_service) {
        this.property_service = property_service;
    }

    public Set<PropertyServiceResponse> getProperty_service_response() {
        return property_service_response;
    }

    public void setProperty_service_response(Set<PropertyServiceResponse> property_service_response) {
        this.property_service_response = property_service_response;
    }
}
