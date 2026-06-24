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
public class PhonebookDTO {

	private String id;
	private String account_number;
	private String vendor_name;
	private String email;
	private String phone;
	private String phone_type;
	private String value;
	private String address;
	private String city;
	private String country;
	private String state;
	private String zip;

}
