package io.sclera.models;

import io.sclera.dto.PropertyQrcodeDTO;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Represents a QR code bound to a specific property service and physical location.
 * Holds the scanned inspection responses as a cascade-all child set.
 */
@SqlResultSetMapping(
        name = "propertyqrcodeemapping",
        classes = {
                @ConstructorResult(
                        targetClass = PropertyQrcodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id",                   type = String.class),
                                @ColumnResult(name = "image_url",            type = String.class),
                                @ColumnResult(name = "location_id",          type = String.class),
                                @ColumnResult(name = "property_service_id",  type = String.class),
                                @ColumnResult(name = "location_name",        type = String.class),
                                @ColumnResult(name = "floor_name",           type = String.class),
                                @ColumnResult(name = "building_name",        type = String.class),
                                @ColumnResult(name = "property_service_name",type = String.class)
                        })
        }
)

// PG-port: no changes required — queries contain no MySQL-specific syntax.
@NamedNativeQuery(
        name = "PropertyQrcode.getPropertyServiceLocations",
        query = "SELECT pq.id, pq.image_url, pq.location_id, pq.property_service_id,"
                + " l.name as location_name, f.name as floor_name, b.name as building_name, ps.name as property_service_name"
                + " FROM property_qrcode pq"
                + " LEFT JOIN property_service ps ON ps.id = pq.property_service_id"
                + " LEFT JOIN location l ON l.id = pq.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE pq.property_service_id = ?1",
        resultSetMapping = "propertyqrcodeemapping")

@NamedNativeQuery(
        name = "PropertyQrcode.getPropertyQrcode",
        query = "SELECT pq.id, pq.image_url, pq.location_id, pq.property_service_id,"
                + " l.name as location_name, f.name as floor_name, b.name as building_name, ps.name as property_service_name"
                + " FROM property_qrcode pq"
                + " LEFT JOIN property_service ps ON ps.id = pq.property_service_id"
                + " LEFT JOIN location l ON l.id = pq.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE pq.property_service_id = ?1 AND pq.location_id = ?2",
        resultSetMapping = "propertyqrcodeemapping")

@NamedNativeQuery(
        name = "PropertyQrcode.getPropertyQrcodeByFloor",
        query = "SELECT pq.id, pq.image_url, pq.location_id, pq.property_service_id,"
                + " l.name as location_name, f.name as floor_name, b.name as building_name, ps.name as property_service_name"
                + " FROM property_qrcode pq"
                + " LEFT JOIN property_service ps ON ps.id = pq.property_service_id"
                + " LEFT JOIN location l ON l.id = pq.location_id"
                + " LEFT JOIN floor f ON f.id = l.floor_id"
                + " LEFT JOIN building b ON b.id = f.building_id"
                + " WHERE (?1 = 'null' OR b.id = ?1)"
                + " AND (?2 = 'null' OR f.id = ?2)"
                + " AND (?3 = 'null' OR pq.location_id = ?3)"
                + " AND pq.property_service_id = ?4",
        resultSetMapping = "propertyqrcodeemapping")

@Entity
public class PropertyQrcode {

    @Id
    private String id;

    private String image_url;

    @ManyToOne
    private PropertyService property_service;

    @ManyToOne
    private Location location;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "property_qrcode")
    private Set<PropertyServiceResponse> property_service_response;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public PropertyService getProperty_service() {
        return property_service;
    }

    public void setProperty_service(PropertyService property_service) {
        this.property_service = property_service;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Set<PropertyServiceResponse> getProperty_service_response() {
        return property_service_response;
    }

    public void setProperty_service_response(Set<PropertyServiceResponse> property_service_response) {
        this.property_service_response = property_service_response;
    }
}
