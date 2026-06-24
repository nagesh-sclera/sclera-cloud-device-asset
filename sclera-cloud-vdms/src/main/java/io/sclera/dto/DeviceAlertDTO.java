package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceAlertDTO {

    private String id;
    private String name;
    private String docker_name;
    private String docker_system_type;
    private String building;
    private String floor;
    private String location;
    private String product_id;
    private String image_url;
    private PhonebookAddressDto local_vendor;
    private String template_type;
    private BigInteger alert_time;
    private String alert_message;
    private String type;
}
