package io.sclera.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationDTO {

    private String id;

    private String name;

    private String organisation_image_url;

    private String organisation_light_theme_url;

}
