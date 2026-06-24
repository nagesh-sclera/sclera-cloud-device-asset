package io.sclera.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
public class DockerSyncDTO {

	private String name;
	private Integer vendor_sync;
	private Integer vendor_transfer;

}
