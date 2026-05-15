package io.sclera.dto;

import java.util.Set;

public class ProductDTO {

    private String id;
    private String manufacturer;
    private String url_1;
    private String url_2;
    private String url_3;
    private String brand;
    private String category;
    private String contact_link;
    private String current_max;
    private String current_min;
    private String description;
    private String device_name;
    private String device_type;
    private Boolean end_of_life;
    private String extension_1;
    private String extension_2;
    private String extension_3;
    private String humidity_max;
    private String humidity_min;
    public String image_url_1;
    public String image_url_2;
    public String image_url_3;
    private String model;
    private Float msrp;
    private String network_layer;
    private String part_number;
    private String power_max;
    private String power_min;
    private String product_link;
    private String software_link;
    private String sub_category;
    private String system;
    private String temperature_max;
    private String temperature_min;
    private String type;
    private String voltage_max;
    private String voltage_min;
    public String global_vendor_id;
    public String local_vendor_id;
    public String other_vendor_1_id;
    public String other_vendor_2_id;
    public String other_vendor_3_id;
    private String base64image_1;
    private String base64image_2;
    private String base64image_3;
    private String vendor_org_id;
    private Set<Product_SnmpDTO> product_snmps;
    private Set<Product_PortsDTO> product_ports;
    private Set<Product_NotesDTO> product_notes;
    private Set<PhonebookAddressDto> vendors;


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getManufacturer() {
        return manufacturer;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getContact_link() {
        return contact_link;
    }
    public void setContact_link(String contact_link) {
        this.contact_link = contact_link;
    }
    public String getCurrent_max() {
        return current_max;
    }
    public void setCurrent_max(String current_max) {
        this.current_max = current_max;
    }
    public String getCurrent_min() {
        return current_min;
    }
    public void setCurrent_min(String current_min) {
        this.current_min = current_min;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDevice_name() {
        return device_name;
    }
    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }
    public String getDevice_type() {
        return device_type;
    }
    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }
    public Boolean getEnd_of_life() {
        return end_of_life;
    }
    public void setEnd_of_life(Boolean end_of_life) {
        this.end_of_life = end_of_life;
    }
    public String getHumidity_max() {
        return humidity_max;
    }
    public void setHumidity_max(String humidity_max) {
        this.humidity_max = humidity_max;
    }
    public String getHumidity_min() {
        return humidity_min;
    }
    public void setHumidity_min(String humidity_min) {
        this.humidity_min = humidity_min;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public Float getMsrp() {
        return msrp;
    }
    public void setMsrp(Float msrp) {
        this.msrp = msrp;
    }
    public String getNetwork_layer() {
        return network_layer;
    }
    public void setNetwork_layer(String network_layer) {
        this.network_layer = network_layer;
    }
    public String getPart_number() {
        return part_number;
    }
    public void setPart_number(String part_number) {
        this.part_number = part_number;
    }
    public String getPower_max() {
        return power_max;
    }
    public void setPower_max(String power_max) {
        this.power_max = power_max;
    }
    public String getPower_min() {
        return power_min;
    }
    public void setPower_min(String power_min) {
        this.power_min = power_min;
    }
    public String getProduct_link() {
        return product_link;
    }
    public void setProduct_link(String product_link) {
        this.product_link = product_link;
    }
    public String getSoftware_link() {
        return software_link;
    }
    public void setSoftware_link(String software_link) {
        this.software_link = software_link;
    }
    public String getSub_category() {
        return sub_category;
    }
    public void setSub_category(String sub_category) {
        this.sub_category = sub_category;
    }
    public String getSystem() {
        return system;
    }
    public void setSystem(String system) {
        this.system = system;
    }
    public String getTemperature_max() {
        return temperature_max;
    }
    public void setTemperature_max(String temperature_max) {
        this.temperature_max = temperature_max;
    }
    public String getTemperature_min() {
        return temperature_min;
    }
    public void setTemperature_min(String temperature_min) {
        this.temperature_min = temperature_min;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getVoltage_max() {
        return voltage_max;
    }
    public void setVoltage_max(String voltage_max) {
        this.voltage_max = voltage_max;
    }
    public String getVoltage_min() {
        return voltage_min;
    }
    public void setVoltage_min(String voltage_min) {
        this.voltage_min = voltage_min;
    }
    public String getGlobal_vendor_id() {
        return global_vendor_id;
    }
    public void setGlobal_vendor_id(String global_vendor_id) {
        this.global_vendor_id = global_vendor_id;
    }
    public String getLocal_vendor_id() {
        return local_vendor_id;
    }
    public void setLocal_vendor_id(String local_vendor_id) {
        this.local_vendor_id = local_vendor_id;
    }
    public String getOther_vendor_1_id() {
        return other_vendor_1_id;
    }
    public void setOther_vendor_1_id(String other_vendor_1_id) {
        this.other_vendor_1_id = other_vendor_1_id;
    }
    public String getOther_vendor_2_id() {
        return other_vendor_2_id;
    }
    public void setOther_vendor_2_id(String other_vendor_2_id) {
        this.other_vendor_2_id = other_vendor_2_id;
    }
    public String getOther_vendor_3_id() {
        return other_vendor_3_id;
    }
    public void setOther_vendor_3_id(String other_vendor_3_id) {
        this.other_vendor_3_id = other_vendor_3_id;
    }
    public String getVendor_org_id() {
        return vendor_org_id;
    }
    public void setVendor_org_id(String vendor_org_id) {
        this.vendor_org_id = vendor_org_id;
    }
    public Set<Product_SnmpDTO> getProduct_snmps() {
        return product_snmps;
    }
    public void setProduct_snmps(Set<Product_SnmpDTO> product_snmps) {
        this.product_snmps = product_snmps;
    }
    public Set<Product_PortsDTO> getProduct_ports() {
        return product_ports;
    }
    public void setProduct_ports(Set<Product_PortsDTO> product_ports) {
        this.product_ports = product_ports;
    }
    public Set<Product_NotesDTO> getProduct_notes() {
        return product_notes;
    }
    public void setProduct_notes(Set<Product_NotesDTO> product_notes) {
        this.product_notes = product_notes;
    }
    public String getUrl_1() {
        return url_1;
    }
    public void setUrl_1(String url_1) {
        this.url_1 = url_1;
    }
    public String getUrl_2() {
        return url_2;
    }
    public void setUrl_2(String url_2) {
        this.url_2 = url_2;
    }
    public String getUrl_3() {
        return url_3;
    }
    public void setUrl_3(String url_3) {
        this.url_3 = url_3;
    }
    public String getExtension_1() {
        return extension_1;
    }
    public void setExtension_1(String extension_1) {
        this.extension_1 = extension_1;
    }
    public String getExtension_2() {
        return extension_2;
    }
    public void setExtension_2(String extension_2) {
        this.extension_2 = extension_2;
    }
    public String getExtension_3() {
        return extension_3;
    }
    public void setExtension_3(String extension_3) {
        this.extension_3 = extension_3;
    }
    public String getImage_url_1() {
        return image_url_1;
    }
    public void setImage_url_1(String image_url_1) {
        this.image_url_1 = image_url_1;
    }
    public String getImage_url_2() {
        return image_url_2;
    }
    public void setImage_url_2(String image_url_2) {
        this.image_url_2 = image_url_2;
    }
    public String getImage_url_3() {
        return image_url_3;
    }
    public void setImage_url_3(String image_url_3) {
        this.image_url_3 = image_url_3;
    }
    public String getBase64image_1() {
        return base64image_1;
    }
    public void setBase64image_1(String base64image_1) {
        this.base64image_1 = base64image_1;
    }
    public String getBase64image_2() {
        return base64image_2;
    }
    public void setBase64image_2(String base64image_2) {
        this.base64image_2 = base64image_2;
    }
    public String getBase64image_3() {
        return base64image_3;
    }
    public void setBase64image_3(String base64image_3) {
        this.base64image_3 = base64image_3;
    }
    public Set<PhonebookAddressDto> getVendors() {
        return vendors;
    }
    public void setVendors(Set<PhonebookAddressDto> vendors) {
        this.vendors = vendors;
    }

    public ProductDTO() {}

    public ProductDTO(String image_url_1, String image_url_2, String image_url_3) {
        this.image_url_1 = image_url_1;
        this.image_url_2 = image_url_2;
        this.image_url_3 = image_url_3;
    }

    @Override
    public String toString() {
        return "ProductDTO [id=" + id + ", manufacturer=" + manufacturer + ", url_1=" + url_1 + ", url_2=" + url_2
                + ", url_3=" + url_3 + ", brand=" + brand + ", category=" + category + ", contact_link=" + contact_link
                + ", current_max=" + current_max + ", current_min=" + current_min + ", description=" + description
                + ", device_name=" + device_name + ", device_type=" + device_type + ", end_of_life=" + end_of_life
                + ", extension_1=" + extension_1 + ", extension_2=" + extension_2 + ", extension_3=" + extension_3
                + ", humidity_max=" + humidity_max + ", humidity_min=" + humidity_min + ", image_url_1=" + image_url_1
                + ", image_url_2=" + image_url_2 + ", image_url_3=" + image_url_3 + ", model=" + model + ", msrp="
                + msrp + ", network_layer=" + network_layer + ", part_number=" + part_number + ", power_max="
                + power_max + ", power_min=" + power_min + ", product_link=" + product_link + ", software_link="
                + software_link + ", sub_category=" + sub_category + ", system=" + system + ", temperature_max="
                + temperature_max + ", temperature_min=" + temperature_min + ", type=" + type + ", voltage_max="
                + voltage_max + ", voltage_min=" + voltage_min + ", global_vendor_id=" + global_vendor_id
                + ", local_vendor_id=" + local_vendor_id + ", other_vendor_1_id=" + other_vendor_1_id
                + ", other_vendor_2_id=" + other_vendor_2_id + ", other_vendor_3_id=" + other_vendor_3_id
                + ", base64image_1=" + base64image_1 + ", base64image_2=" + base64image_2 + ", base64image_3="
                + base64image_3 + ", vendor_org_id=" + vendor_org_id + ", product_snmps=" + product_snmps
                + ", product_ports=" + product_ports + ", product_notes=" + product_notes + ", vendors=" + vendors
                + "]";
    }




}
