package io.sclera.model.compositeclass;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class P2PRemoteSessionIds implements Serializable {

	private static final long serialVersionUID = 1L;
	private String vdms_id;
	private String vendor_email;

}
