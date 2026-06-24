package io.sclera.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CorrigoUserSettingsDTO {

    private String id;
    private String client_id;
    private String client_secret;
    private String credential_type;
    private String organisation_global_integration_id;
    private String user_id;
    private String corrigo_user_id;
}
