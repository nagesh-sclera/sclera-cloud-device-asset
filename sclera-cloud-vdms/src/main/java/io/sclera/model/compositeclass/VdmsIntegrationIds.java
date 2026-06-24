package io.sclera.model.compositeclass;

import io.sclera.model.Vdms;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VdmsIntegrationIds implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private Vdms vdms;
}
