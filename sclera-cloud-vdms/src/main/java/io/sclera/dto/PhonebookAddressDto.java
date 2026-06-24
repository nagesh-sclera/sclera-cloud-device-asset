package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhonebookAddressDto {

    private String id;
    private String account_number;
    private String vendor_name;
    private String email;
    private String phone;
    private String phone_type;
    private String value;
    private String company_name;
    private String website;
    private String address;
    private String city;
    private String country;
    private String state;
    private String street;
    private String zip;
}
