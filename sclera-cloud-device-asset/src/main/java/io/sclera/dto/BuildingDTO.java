package io.sclera.dto;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuildingDTO {

	private String building_id;
	private String name;
	private String vdms_id;
	private  String code;
	private Set<FloorDTO> floors;
	JSONObject counts;

	private String id;
	private BigInteger updated_timestamp;

	public String getId() { return id;}
	public void setId(String id) { this.id = id;}
	public BigInteger getUpdated_timestamp() { return updated_timestamp;}
	public void setUpdated_timestamp(BigInteger updated_timestamp) { this.updated_timestamp = updated_timestamp;}
	public String getBuilding_id() {
		return building_id;
	}
	public void setBuilding_id(String building_id) {
		this.building_id = building_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<FloorDTO> getFloors() {
		return floors;
	}
	public void setFloors(Set<FloorDTO> floors) {
		this.floors = floors;
	}
	public String getVdms_id() {
		return vdms_id;
	}
	public void setVdms_id(String vdms_id) {
		this.vdms_id = vdms_id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	public JSONObject getCounts() {
		return counts;
	}

	public void setCounts(JSONObject counts) {
		this.counts = counts;
	}

	public BuildingDTO() {}

	public BuildingDTO(String building_id, String name , String vdms_id, String code) {
		super();
		this.building_id = building_id;
		this.name = name;
		this.vdms_id = vdms_id;
		this.code = code;
	}

	public BuildingDTO(String id, String name , String vdms_id, String code,String building_id) {
		super();
		this.id = id;
		this.name = name;
		this.vdms_id = vdms_id;
		this.code = code;
		this.building_id = building_id;
	}

	@Override
	public String toString() {
		return "BuildingDTO{" +
				"building_id='" + building_id + '\'' +
				", name='" + name + '\'' +
				", vdms_id='" + vdms_id + '\'' +
				", code='" + code + '\'' +
				", floors=" + floors +
				", counts=" + counts +
				'}';
	}
}
