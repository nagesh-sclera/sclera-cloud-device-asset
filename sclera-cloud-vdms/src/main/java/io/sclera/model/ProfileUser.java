package io.sclera.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.ProfileUserDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class , property = "id" , scope = ProfileUser.class)

@SqlResultSetMapping(
		name = "profileusermapping",
		classes = {
				@ConstructorResult(
						targetClass = ProfileUserDTO.class,
						columns = {
								@ColumnResult(name = "profile_user_id" , type = String.class),
								@ColumnResult(name = "name" , type = String.class),
								@ColumnResult(name = "email" , type = String.class),
								@ColumnResult(name = "value" , type = String.class),
								@ColumnResult(name = "phone" , type = String.class),
								@ColumnResult(name = "profile_id" , type = String.class)
								}
						)	
				}
		)



@NamedNativeQuery(
        name = "ProfileUser.getProfileUsersByProfileId",
        		query = "SELECT id AS profile_user_id ,name ,email ,value ,phone ,profile_id FROM profile_user WHERE profile_id  = ?1",
        		resultSetMapping = "profileusermapping"
		)


@Getter
@Setter
public class ProfileUser {

	@Id
	@Column(length = 64)
	private String id;
	
	
	@Column(length = 64)
	private String name;
	
	@Column(length = 64)
	private String email;
	
	@Column(length = 8)
	private String value;

	@Column(length = 18)
	private String phone;
	
	@ManyToOne
	private Profile profile;

	
	
}
