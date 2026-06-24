package io.sclera.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TenantDTO {


    private String id;
    private String issuer;
    private String jwksUri;
    private String providerId;

    public TenantDTO(String issuer, String jwksUri) {
        this.issuer = issuer;
        this.jwksUri = jwksUri;
    }
}
