package io.sclera.model;

import io.sclera.dto.ExternalClientUserDTO;
import io.sclera.dto.VdmsDTO;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

@SqlResultSetMapping(
		name = "vdmsvisibilitymapping",
		classes = {
				@ConstructorResult(
						targetClass = VdmsDTO.class,
						columns = {
								@ColumnResult(name = "vdms_id" , type = String.class),
								@ColumnResult(name = "property_name" , type = String.class),
								@ColumnResult(name = "image_url" , type = String.class)
								}
						)	
				}
		)





@NamedNativeQuery(
        name = "Vdms_Visibility.getVisibleVdmsByEmail",
        		query = "SELECT vv.vdms_id , vv.property_name ,v.image_url FROM vdms_visibility vv " +
						"LEFT JOIN vdms v ON v.id = vv.vdms_id WHERE vv.email = ?1 ",
        		resultSetMapping = "vdmsvisibilitymapping"
		)


@SqlResultSetMapping(
		name = "externalVdmsVisibilityMapping",
		classes = {
				@ConstructorResult(
						targetClass = ExternalClientUserDTO.class,
						columns = {
								@ColumnResult(name = "vdmsId", type = String.class),
								@ColumnResult(name = "propertyName", type = String.class)
						}
				)
		}
)

@SqlResultSetMapping(
		name = "vdmsVisibilityListMapping",
		classes = {
				@ConstructorResult(
						targetClass = ExternalClientUserDTO.class,
						columns = {
								@ColumnResult(name = "email", type = String.class),
								@ColumnResult(name = "vdmsId", type = String.class),
								@ColumnResult(name = "propertyName", type = String.class)

						}
				)
		}
)

@NamedNativeQuery(
		name = "Vdms_Visibility.getVisibleVdmsInfoByEmail",
		query = "SELECT vv.vdms_id AS vdmsId , vv.property_name AS propertyName FROM vdms_visibility vv " +
				"LEFT JOIN vdms v ON v.id = vv.vdms_id WHERE vv.email = ?1 ",
		resultSetMapping = "externalVdmsVisibilityMapping"
)
@NamedNativeQuery(
		name = "Vdms_Visibility.getVisibleVdmsByEmailList",
		query = "SELECT CONCAT(vv.property_name, ' (', vv.vdms_id, ')') AS vdmsId , vv.email, vv.property_name AS propertyName FROM vdms_visibility vv " +
				"LEFT JOIN vdms v ON v.id = vv.vdms_id WHERE vv.email IN ?1 ",
		resultSetMapping = "vdmsVisibilityListMapping"
)
public class Vdms_Visibility {

	@Id
	private String id;
	
	@Column(length = 64)
	private String vdms_id;
	
	@Column(length = 64)
	private String property_name;
	
	@Column(length = 64)
	private String email;
	@Column(columnDefinition = "varchar(8) DEFAULT '0'")
	private Boolean full_access;

}
