package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExternalClientUserDTO {

    private String userId;
    private String email;
    private String name;
    private String providerId;
    private String timezoneId;
    private String roleId;
    private String address;
    private String city;
    private String country;
    private String state;
    private String zip;
    private String contact;
    private String languageId;
    private List<String> profileIds;
    private List<String> vdmsIds;

    private BigInteger creationTimeStamp;
    private String roleName;


    private String vdmsId;
    private String propertyName;
    private String providerName;

    public ExternalClientUserDTO(String vdmsId, String propertyName) {
        this.vdmsId = vdmsId;
        this.propertyName = propertyName;
    }
    public ExternalClientUserDTO(String email, String vdmsId, String propertyName) {
        this.email = email;
        this.vdmsId = vdmsId;
        this.propertyName = propertyName;
    }
}
