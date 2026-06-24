package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertScheduleDTO {

    private String id;
    private String schedule;
    private Boolean emailMute;
    private  Boolean smsMute;

    private String email;

    public AlertScheduleDTO(Boolean emailMute, Boolean smsMute) {
        this.emailMute = emailMute;
        this.smsMute = smsMute;
    }
}
