package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProfileUserDTO {

	private String profile_user_id;
	private String name;
	private String email;
	private String value;
	private String phone;
	private String profile_id;

}
