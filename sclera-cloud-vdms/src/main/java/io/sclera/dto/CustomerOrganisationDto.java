package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.sclera.model.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrganisationDto {

    private String id;
    private String company_name;
    private Integer is_enterprise;
    private Set<User> users;
    private Set<Vdms> vdms;
    private Set<Profile> profiles;
    private Set<ProxyProfile> proxy_profiles;
    private Set<Ioc> ioc;
    private String organisation_image_url;
    private String organisation_fav_icon_url;
    private String organisation_title;


    public CustomerOrganisationDto(String id, String company_name) {
        this.id = id;
        this.company_name = company_name;
    }
}
