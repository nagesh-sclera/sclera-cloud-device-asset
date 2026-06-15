package io.sclera.models;

import java.math.BigInteger;
import java.util.Set;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.ColumnDefault;

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
