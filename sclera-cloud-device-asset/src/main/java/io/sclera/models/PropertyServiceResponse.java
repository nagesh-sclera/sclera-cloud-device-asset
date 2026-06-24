package io.sclera.models;

import io.sclera.dto.PropertyServiceResponseDTO;

import jakarta.persistence.*;
import java.math.BigInteger;

/**
 * Captures a submitted answer to a property-service request field during a QR code scan.
 * Links back to the originating PropertyQrcode and the specific PropertyServiceRequest question.
 *
 * Named queries that filter by ps.vdms_id (edge: getPropertyServiceResponses) have been adapted
 * to plain SQL; the vdms_id column is now a scalar on property_service, not a FK join.
 */
@SqlResultSetMapping(
        name = "propertyserviceresponsetmapping",
        classes = {
                @ConstructorResult(
                        targetClass = PropertyServiceResponseDTO.class,
                        columns = {
                                @ColumnResult(name = "id",                         type = String.class),
                                @ColumnResult(name = "label",                      type = String.class),
                                @ColumnResult(name = "options",                    type = String.class),
                                @ColumnResult(name = "type",                       type = String.class),
                                @ColumnResult(name = "property_service_id",        type = String.class),
                                @ColumnResult(name = "alert",                      type = Boolean.class),
                                @ColumnResult(name = "timestamp",                  type = BigInteger.class),
                                @ColumnResult(name = "value",                      type = String.class),
                                @ColumnResult(name = "property_service_request_id",type = String.class),
                                @ColumnResult(name = "property_qrcode_id",         type = String.class)
                        })
        }
)

// PG-port: no backticks; vdms_id is now a scalar column on property_service (not a joined FK table).
@NamedNativeQuery(
        name = "PropertyServiceResponse.getPropertyServiceRequestResponsesByServiceId",
        query = "SELECT psr.id, psr.label, psr.options, psr.type, psr.property_service_id,"
                + " psre.alert, psre.timestamp, psre.value,"
                + " psre.property_service_request_id, psre.property_qrcode_id"
                + " FROM property_service_request psr"
                + " LEFT JOIN property_service_response psre ON psr.id = psre.property_service_request_id"
                + " WHERE psr.property_service_id = ?1",
        resultSetMapping = "propertyserviceresponsetmapping")

@NamedNativeQuery(
        name = "PropertyServiceResponse.getPropertyServiceRequestResponsesByIds",
        query = "SELECT psr.id, psr.label, psr.options, psr.type, psr.property_service_id,"
                + " psre.alert, psre.timestamp, psre.value,"
                + " psre.property_service_request_id, psre.property_qrcode_id"
                + " FROM property_service_request psr"
                + " LEFT JOIN property_service_response psre ON psr.id = psre.property_service_request_id"
                + " WHERE psr.property_service_id = ?1 AND psre.property_qrcode_id = ?2",
        resultSetMapping = "propertyserviceresponsetmapping")


@SqlResultSetMapping(
        name = "propertyserviceresponsemapping",
        classes = {
                @ConstructorResult(
                        targetClass = PropertyServiceResponseDTO.class,
                        columns = {
                                @ColumnResult(name = "id",                          type = String.class),
                                @ColumnResult(name = "alert",                       type = Boolean.class),
                                @ColumnResult(name = "timestamp",                   type = BigInteger.class),
                                @ColumnResult(name = "value",                       type = String.class),
                                @ColumnResult(name = "property_qrcode_id",          type = String.class),
                                @ColumnResult(name = "property_service_request_id", type = String.class)
                        })
        }
)

// PG-port: edge query filtered ps.vdms_id via a Vdms entity join; here vdms_id is a plain column on property_service.
@NamedNativeQuery(
        name = "PropertyServiceResponse.getPropertyServiceResponses",
        query = "SELECT psre.id, psre.alert, psre.timestamp, psre.value,"
                + " psre.property_qrcode_id, psre.property_service_request_id"
                + " FROM property_service_response psre"
                + " LEFT JOIN property_service_request psr ON psr.id = psre.property_service_request_id"
                + " LEFT JOIN property_service ps ON ps.id = psr.property_service_id"
                + " WHERE ps.vdms_id = ?1",
        resultSetMapping = "propertyserviceresponsemapping")

@NamedNativeQuery(
        name = "PropertyServiceResponse.getPropertyServiceResponseById",
        query = "SELECT psre.id, psre.alert, psre.timestamp, psre.value,"
                + " psre.property_qrcode_id, psre.property_service_request_id"
                + " FROM property_service_response psre"
                + " WHERE psre.property_qrcode_id = ?1 AND psre.property_service_request_id = ?2",
        resultSetMapping = "propertyserviceresponsemapping")

@Entity
public class PropertyServiceResponse {

    @Id
    private String id;

    private String value;

    @Column(columnDefinition = "boolean default false")
    private Boolean alert;

    private BigInteger timestamp;

    @ManyToOne
    private PropertyQrcode property_qrcode;

    @ManyToOne
    private PropertyServiceRequest property_service_request;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public PropertyQrcode getProperty_qrcode() {
        return property_qrcode;
    }

    public void setProperty_qrcode(PropertyQrcode property_qrcode) {
        this.property_qrcode = property_qrcode;
    }

    public PropertyServiceRequest getProperty_service_request() {
        return property_service_request;
    }

    public void setProperty_service_request(PropertyServiceRequest property_service_request) {
        this.property_service_request = property_service_request;
    }
}
