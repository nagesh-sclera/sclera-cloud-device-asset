package io.sclera.models;

import io.sclera.dto.PropertyServiceDTO;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Represents a property-service definition (e.g. cleaning, maintenance) scoped to a vdms tenant.
 * Owns the set of request fields and the QR codes bound to it.
 */
@SqlResultSetMapping(
        name = "propertyservicemapping",
        classes = {
                @ConstructorResult(
                        targetClass = PropertyServiceDTO.class,
                        columns = {
                                @ColumnResult(name = "id",   type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class)
                        })
        }
)

@NamedNativeQuery(
        name = "PropertyService.getPropertyServices",
        query = "SELECT ps.id, ps.name, ps.vdms_id"
                + " FROM property_service ps",
        resultSetMapping = "propertyservicemapping")

@NamedNativeQuery(
        name = "PropertyService.getPropertyServicesById",
        query = "SELECT ps.id, ps.name, ps.vdms_id"
                + " FROM property_service ps"
                + " WHERE ps.vdms_id = ?1 AND ps.id = ?2",
        resultSetMapping = "propertyservicemapping")

@Entity
public class PropertyService {

    @Id
    private String id;

    private String name;

    /**
     * Tenant identifier — replaces the @ManyToOne Vdms FK from the edge monolith.
     * device-asset has no cross-service Vdms entity join at this layer.
     */
    private String vdms_id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "property_service")
    private Set<PropertyQrcode> property_qrcode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "property_service")
    private Set<PropertyServiceRequest> property_service_request;

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

    public Set<PropertyQrcode> getProperty_qrcode() {
        return property_qrcode;
    }

    public void setProperty_qrcode(Set<PropertyQrcode> property_qrcode) {
        this.property_qrcode = property_qrcode;
    }

    public Set<PropertyServiceRequest> getProperty_service_request() {
        return property_service_request;
    }

    public void setProperty_service_request(Set<PropertyServiceRequest> property_service_request) {
        this.property_service_request = property_service_request;
    }
}
