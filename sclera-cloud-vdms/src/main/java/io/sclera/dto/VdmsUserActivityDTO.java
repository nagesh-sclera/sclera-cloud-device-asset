package io.sclera.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VdmsUserActivityDTO {
    private String user_id;
    private String vdms_id;
}
