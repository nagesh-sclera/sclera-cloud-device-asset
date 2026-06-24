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
public class Zoho_CustomerDTO {

	private String display_name;
	private String email;
	private String first_name;
	private String phone;
	private String website;
	private AddressDTO billing_address;
	private AddressDTO shipping_address;

}
