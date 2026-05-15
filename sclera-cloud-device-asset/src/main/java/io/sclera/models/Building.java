package io.sclera.models;

import java.math.BigInteger;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.BuildingDTO;
import org.hibernate.annotations.ColumnDefault;

/**************************** new Building changes **********************/

@SqlResultSetMapping(
		name = "buildingmapping",
		classes = {
				@ConstructorResult(
						targetClass = BuildingDTO.class,
						columns = {
								@ColumnResult(name = "building_id" , type = String.class),
								@ColumnResult(name = "name" , type = String.class),
								@ColumnResult(name = "vdms_id" , type = String.class),
								@ColumnResult(name = "code", type = String.class)

						}
				)
		}
)

@NamedNativeQuery(
		name = "Building.getBuildingByFloorId",
		query = "SELECT b.id AS building_id , b.name ,b.vdms_id, b.code FROM building b LEFT JOIN floor f ON f.building_id = b.id WHERE f.id = ?1",
		resultSetMapping = "buildingmapping"
)


@NamedNativeQuery(
		name = "Building.getBuildingsByVdmsId",
		query = "SELECT b.id AS building_id , b.name ,b.vdms_id, b.code  FROM building b WHERE b.vdms_id = ?1  ",
		resultSetMapping = "buildingmapping"
)


@NamedNativeQuery(
		name = "Building.getBuildingDetailsByBuildingId",
		query = "SELECT b.id AS building_id , b.name ,b.vdms_id, b.code  FROM building b WHERE b.id = ?1  ",
		resultSetMapping = "buildingmapping"
)

@NamedNativeQuery(
		name = "Building.getBatchBuildingsByPagination",
		query = "SELECT b.id AS building_id , b.name ,b.vdms_id, b.code  FROM building b WHERE b.id IN ?1 LIMIT ?2 OFFSET ?3 ",
		resultSetMapping = "buildingmapping"
)

@SqlResultSetMapping(
		name = "buildingadcmapping",
		classes = {
				@ConstructorResult(
						targetClass = BuildingDTO.class,
						columns = {
								@ColumnResult(name = "id" , type = String.class),
								@ColumnResult(name = "name" , type = String.class),
								@ColumnResult(name = "vdms_id" , type = String.class),
								@ColumnResult(name = "code", type = String.class),
								@ColumnResult(name = "building_id", type = String.class),

						}
				)
		}
)

@NamedNativeQuery(
		name = "Building.getBuildingsByVdmsIdADC",
		query = "SELECT b.id AS id , b.name ,b.vdms_id, b.code,b.id AS building_id FROM building b WHERE b.vdms_id = ?1  ",
		resultSetMapping = "buildingadcmapping"
)


/**************************** new Building changes **********************/

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class , property = "id" , scope = Building.class)
public class Building {
	
	@Id
	private String id;
	
	@Column(length = 128)
	private String name;

	@Column(length = 128)
	private String code;

	@Column(length = 128)
	private BigInteger updated_timestamp;

	@Column
	@ColumnDefault("'vdms'")
	private String source_type;

	@ManyToOne
	private Vdms vdms;
	
	@OneToMany(cascade = CascadeType.ALL,mappedBy = "building")
	private Set<Floor> floor;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Vdms getVdms() {
		return vdms;
	}

	public void setVdms(Vdms vdms) {
		this.vdms = vdms;
	}

	public Set<Floor> getFloor() {
		return floor;
	}

	public void setFloor(Set<Floor> floor) {
		this.floor = floor;
		floor.forEach(((temp)-> {temp.setBuilding(this);}));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
