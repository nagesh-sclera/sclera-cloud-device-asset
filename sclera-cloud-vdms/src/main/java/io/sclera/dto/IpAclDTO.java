package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class IpAclDTO {

    private String ipRange;
    private Boolean isAllowed;

    private String country;
    private String state;
    private String city;

    public IpAclDTO(String ipRange, Boolean isAllowed) {
        this.ipRange = ipRange;
        this.isAllowed = isAllowed;
    }

    public IpAclDTO(String country, String state, String city) {
        this.country = country;
        this.state = state;
        this.city = city;
    }
}
