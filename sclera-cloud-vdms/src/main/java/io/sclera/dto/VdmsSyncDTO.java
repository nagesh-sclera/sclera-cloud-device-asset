package io.sclera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VdmsSyncDTO {

	private String id;
	private Integer user_sync;
	private Integer image_sync;
	private Integer proxy_server_host_sync;
	private Integer proxy_client_sync;
	private Integer service_value_sync;
	private Set<DockerSyncDTO> dockers;
	private Integer vdms_transfer;
	private Integer qr_sync;
	private Integer nfc_sync;
	private Integer barcode_sync;

    private Integer skill_profiles_sync;
    private Integer sclera_agent_permission_sync;
    private Integer managed_software_user_sync;
    private Integer managed_software_sync;
	private Integer corrigo_sync;

	public VdmsSyncDTO(String id, Integer user_sync, Integer image_sync, Integer proxy_server_host_sync,
					   Integer proxy_client_sync, Integer service_value_sync, Integer vdms_transfer, Integer qr_sync, Integer nfc_sync, Integer barcode_sync, Integer corrigo_sync) {
		super();
		this.id = id;
		this.user_sync = user_sync;
		this.image_sync = image_sync;
		this.proxy_server_host_sync = proxy_server_host_sync;
		this.proxy_client_sync = proxy_client_sync;
		this.service_value_sync = service_value_sync;
		this.vdms_transfer = vdms_transfer;
		this.qr_sync = qr_sync;
		this.nfc_sync = nfc_sync;
		this.barcode_sync = barcode_sync;
		this.corrigo_sync = corrigo_sync;
	}


}
