package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class P2PRemoteSessionDTO {

	private String vdms_id;
	private String vendor_email;
	private String admin_email;
	private Integer session_id;
	private Integer port;
	private String devuid;
	private BigInteger last_seen;
	private String udi_status;


	public P2PRemoteSessionDTO(String vdms_id, String vendor_email, Integer session_id, Integer port) {
		this.vdms_id = vdms_id;
		this.vendor_email = vendor_email;
		this.session_id = session_id;
		this.port = port;
	}


	public P2PRemoteSessionDTO(String admin_email, Integer session_id, Integer port, String devuid) {
		this.admin_email = admin_email;
		this.session_id = session_id;
		this.port = port;
		this.devuid = devuid;
	}





}
