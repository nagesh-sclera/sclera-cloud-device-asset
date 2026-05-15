package io.sclera.dto;

public class MeasuringInstrumentAttributesDTO {
    private String id;
    private String name;
    private String type;
    private String unit;
    private String value;
    private String protocol;
    private String category;
    private String primary_id;
    private String secondary_id;
    private String tertiary_id;
    private String measuring_instrument_id;
    private Integer attribute_index;

    public MeasuringInstrumentAttributesDTO() {
    }

    public MeasuringInstrumentAttributesDTO(String id, String name, String type, String unit, String value, String protocol, String category, String primary_id, String secondary_id, String tertiary_id, String measuring_instrument_id, Integer attribute_index) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.unit = unit;
        this.value = value;
        this.protocol = protocol;
        this.category = category;
        this.primary_id = primary_id;
        this.secondary_id = secondary_id;
        this.tertiary_id = tertiary_id;
        this.measuring_instrument_id = measuring_instrument_id;
        this.attribute_index = attribute_index;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrimary_id() {
        return primary_id;
    }

    public void setPrimary_id(String primary_id) {
        this.primary_id = primary_id;
    }

    public String getSecondary_id() {
        return secondary_id;
    }

    public void setSecondary_id(String secondary_id) {
        this.secondary_id = secondary_id;
    }

    public String getTertiary_id() {
        return tertiary_id;
    }

    public void setTertiary_id(String tertiary_id) {
        this.tertiary_id = tertiary_id;
    }

    public String getMeasuring_instrument_id() {
        return measuring_instrument_id;
    }

    public void setMeasuring_instrument_id(String measuring_instrument_id) {
        this.measuring_instrument_id = measuring_instrument_id;
    }

    public Integer getAttribute_index() {
        return attribute_index;
    }

    public void setAttribute_index(Integer attribute_index) {
        this.attribute_index = attribute_index;
    }

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", unit='" + unit + '\'' +
                ", value='" + value + '\'' +
                ", protocol='" + protocol + '\'' +
                ", category='" + category + '\'' +
                ", primary_id='" + primary_id + '\'' +
                ", secondary_id='" + secondary_id + '\'' +
                ", tertiary_id='" + tertiary_id + '\'' +
                ", measuring_instrument_id='" + measuring_instrument_id + '\'' +
                ", attribute_index=" + attribute_index +
                '}';
    }
}
