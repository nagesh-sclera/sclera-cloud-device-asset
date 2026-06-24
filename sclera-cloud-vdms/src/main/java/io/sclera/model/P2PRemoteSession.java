package io.sclera.model;

import io.sclera.dto.P2PRemoteSessionDTO;
import io.sclera.model.compositeclass.P2PRemoteSessionIds;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@IdClass(P2PRemoteSessionIds.class)

@SqlResultSetMapping(
		name = "p2premotesessionmapping",
		classes = {
				@ConstructorResult(
						targetClass = P2PRemoteSessionDTO.class,
						columns = {
								@ColumnResult(name = "vdms_id" , type = String.class),
								@ColumnResult(name = "vendor_email" , type = String.class),
								@ColumnResult(name = "session_id" , type = Integer.class),
								@ColumnResult(name = "port" , type = Integer.class)
								
								}
						)	
				}
		)


@NamedNativeQuery(
        name = "P2PRemoteSession.getP2PRemoteSessionByVdmsIdAndVendorEmail",
        		query = "SELECT vdms_id ,vendor_email ,session_id ,port FROM p2premote_session  WHERE vdms_id = ?1 AND vendor_email = ?2",
        		resultSetMapping = "p2premotesessionmapping"
		)


@Getter
@Setter
public class P2PRemoteSession {

	@Id
	private String vdms_id;
	
	@Id
	private String vendor_email;
	
	private Integer session_id;
	private Integer port;

}
