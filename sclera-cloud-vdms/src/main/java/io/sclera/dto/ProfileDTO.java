package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProfileDTO {

	private String profile_id;
	private String name;
	private Boolean is_primary;
	private String customer_org_id;
	private String vendor_org_id;
	private String vdms_id;
	private Set<ProfileUserDTO> profile_users;

	public ProfileDTO(String profile_id, String name, Boolean is_primary, String customer_org_id) {
		super();
		this.profile_id = profile_id;
		this.name = name;
		this.is_primary = is_primary;
		this.customer_org_id = customer_org_id;
	}

	public ProfileDTO(String profile_id, String name, String vendor_org_id, Boolean is_primary) {
		super();
		this.profile_id = profile_id;
		this.name = name;
		this.vendor_org_id = vendor_org_id;
		this.is_primary = is_primary;
	}

}
