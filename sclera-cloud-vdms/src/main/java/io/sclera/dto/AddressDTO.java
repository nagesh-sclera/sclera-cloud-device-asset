package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AddressDTO {

	private String id;
	private String address;
	private String city;
	private String state;
	private String zip;
	private String country;

	public AddressDTO(String id, String address, String city, String state, String zip, String country) {
		super();
		this.id = id;
		this.address = address;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.country = country;
	}

}
