package io.sclera.dto;

import lombok.Data;

@Data
public class TouchscreenDTO {

    private String email;
    private String activation_status;
    private String vdms_id;
    private String vdmsPassword;
    private String password;
    private String refreshToken;
    private Integer status_code;
    private String organisation_id;

    private int isValid;

    private String accessToken;
    private String publicKey;
    private String timezone;
    private VdmsDTO vdmsDTO;
}
