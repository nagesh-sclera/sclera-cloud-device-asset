package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VdmsIntegrationDTO {

    private String id;
    private String helper_manual_name;
    private String name;
    private String vdms_id;
    private String image_url;
    private String description;
    private Integer active;
    private String helperId;
    private Integer type;
    private List<String> helperIds;

    public VdmsIntegrationDTO(String id, String vdms_id, Integer active, String helperId) {
        this.id = id;
        this.vdms_id = vdms_id;
        this.active = active;
        this.helperId = helperId;
    }

    public VdmsIntegrationDTO(String id, String name, String vdms_id, String image_url,
                              String description, Integer active) {
        this.id = id;
        this.name = name;
        this.vdms_id = vdms_id;
        this.image_url = image_url;
        this.description = description;
        this.active = active;
    }
}
