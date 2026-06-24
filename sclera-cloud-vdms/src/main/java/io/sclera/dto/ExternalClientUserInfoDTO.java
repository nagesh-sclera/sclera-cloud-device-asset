package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExternalClientUserInfoDTO {

    @JsonProperty("RequestPermId")
    private String RequestPermId;

    @JsonProperty("Email")
    private String Email;

    @JsonProperty("Upn")
    private String Upn;

    @JsonProperty("AdGuid")
    private String AdGuid;

    @JsonProperty("Permission")
    private String Permission;

    @JsonProperty("ExternalPermissionId")
    private String ExternalPermissionId;

    @JsonProperty("Action")
    private String Action;

    @JsonProperty("ApprovedBy")
    private String ApprovedBy;

    @JsonProperty("IsUserTerminated")
    private String IsUserTerminated;

    @JsonProperty("ClientId")
    private String ClientId;

    @JsonProperty("PermissionGroup")
    private String PermissionGroup;

    @JsonProperty("ClientName")
    private String ClientName;

    @JsonProperty("AppName")
    private String AppName;

    @JsonProperty("RequestedAt")
    private String RequestedAt;

    @JsonProperty("CustomerProviderName")
    private String CustomerProviderName;

    @JsonProperty("EmailVerificationToken")
    private String EmailVerificationToken;

    @JsonProperty("CreationTimeStamp")
    private BigInteger CreationTimeStamp;

    public ExternalClientUserAttributesDTO Attributes;

}
