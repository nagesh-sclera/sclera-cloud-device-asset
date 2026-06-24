package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExternalClientUserAttributesDTO {

    @JsonProperty("Timezone")
    private String Timezone;

    @JsonProperty("RoleId")
    private String RoleId;

    @JsonProperty("VdmsIds")
    private List<String> VdmsIds;

    @JsonProperty("LanguageId")
    private String LanguageId;

    @JsonProperty("TimezoneId")
    private String TimezoneId;

    @JsonProperty("ProviderId")
    private String ProviderId;

    @JsonProperty("CustomerProviderName")
    private String CustomerProviderName;

    @JsonProperty("RoleName")
    private String RoleName;

    @JsonProperty("UserId")
    private String UserId;

    @JsonProperty("Address")
    private String Address;

    @JsonProperty("City")
    private String City;

    @JsonProperty("Country")
    private String Country;

    @JsonProperty("State")
    private String State;

    @JsonProperty("Zip")
    private Integer Zip;

    @JsonProperty("Contact")
    private String Contact;

    @JsonProperty("Name")
    private String Name;

    @JsonProperty("Website")
    private String Website;

    @JsonProperty("LanguageName")
    private String LanguageName = "EN";

}
