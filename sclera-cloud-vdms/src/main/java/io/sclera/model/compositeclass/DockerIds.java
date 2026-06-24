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
public class DockerIds implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private Vdms vdms;

}
