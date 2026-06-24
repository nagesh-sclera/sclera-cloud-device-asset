package io.sclera.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VersionDTO {

	private String version;
	private String link;

}
