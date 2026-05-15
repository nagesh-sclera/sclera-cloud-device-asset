package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyServiceResponseDTO {
    // Property Service Request Fields

    private String id;
    private String label;
    private String options;
    private String type;
    private String property_service_id;

    // Property Service Response Fields
    private Boolean alert;
    private BigInteger timestamp;
    private String value;
    private String property_service_request_id;
    private String property_qrcode_id;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getProperty_service_request_id() {
        return property_service_request_id;
    }

    public void setProperty_service_request_id(String property_service_request_id) {
        this.property_service_request_id = property_service_request_id;
    }

    public String getProperty_qrcode_id() {
        return property_qrcode_id;
    }

    public void setProperty_qrcode_id(String property_qrcode_id) {
        this.property_qrcode_id = property_qrcode_id;
    }

    public PropertyServiceResponseDTO() {
    }

    public PropertyServiceResponseDTO(String id, String label, String options, String type, String property_service_id, Boolean alert, BigInteger timestamp, String value, String property_service_request_id, String property_qrcode_id) {
        this.id = id;
        this.label = label;
        this.options = options;
        this.type = type;
        this.property_service_id = property_service_id;
        this.alert = alert;
        this.timestamp = timestamp;
        this.value = value;
        this.property_service_request_id = property_service_request_id;
        this.property_qrcode_id = property_qrcode_id;
    }

    public PropertyServiceResponseDTO(String id, String label, String options, String type) {
        this.id = id;
        this.label = label;
        this.options = options;
        this.type = type;
    }

    public PropertyServiceResponseDTO(String id, Boolean alert, BigInteger timestamp, String value, String property_qrcode_id, String property_service_request_id) {
        this.id = id;
        this.alert = alert;
        this.timestamp = timestamp;
        this.value = value;
        this.property_qrcode_id = property_qrcode_id;
        this.property_service_request_id = property_service_request_id;

    }

    @Override
    public String toString() {
        return "PropertyServiceResponseDTO{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", options='" + options + '\'' +
                ", type='" + type + '\'' +
                ", property_service_id='" + property_service_id + '\'' +
                ", alert=" + alert +
                ", timestamp=" + timestamp +
                ", value='" + value + '\'' +
                ", property_service_request_id='" + property_service_request_id + '\'' +
                ", property_qrcode_id='" + property_qrcode_id + '\'' +
                '}';
    }
}
