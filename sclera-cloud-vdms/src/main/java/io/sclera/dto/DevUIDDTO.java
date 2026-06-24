package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DevUIDDTO {

    private String vdms_id;
    private String property_name;
    private String devuid;
    private String devuid_status;
    private BigInteger last_seen;

    public DevUIDDTO(String vdms_id, String property_name, String devuid, BigInteger last_seen) {
        this.vdms_id = vdms_id;
        this.property_name = property_name;
        this.devuid = devuid;
        this.last_seen = last_seen;
    }
}
