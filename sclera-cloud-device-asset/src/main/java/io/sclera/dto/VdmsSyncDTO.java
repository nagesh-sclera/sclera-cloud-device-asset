package io.sclera.dto;
import java.util.List;
/** STUB: non-AP-C1 sync DTO */
public class VdmsSyncDTO {
    private String devuid;
    private Integer corrigo_sync;
    private Integer user_sync;
    private Integer skill_profiles_sync;
    private Integer managed_software_user_sync;
    private Integer managed_software_sync;
    private Integer vdms_transfer;
    private Integer proxy_server_host_sync;
    private Integer proxy_client_sync;
    private InventoryDeviceSyncDTO inventory_device_sync;
    private Integer sclera_agent_permission_sync;
    private Integer qr_sync;
    private Integer nfc_sync;
    private Integer barcode_sync;
    private List<io.sclera.dto.dockerSyncDTO> dockers;

    public String getDevuid() { return devuid; }
    public void setDevuid(String devuid) { this.devuid = devuid; }
    public Integer getCorrigo_sync() { return corrigo_sync; }
    public void setCorrigo_sync(Integer v) { this.corrigo_sync = v; }
    public Integer getUser_sync() { return user_sync; }
    public void setUser_sync(Integer v) { this.user_sync = v; }
    public Integer getSkill_profiles_sync() { return skill_profiles_sync; }
    public void setSkill_profiles_sync(Integer v) { this.skill_profiles_sync = v; }
    public Integer getManaged_software_user_sync() { return managed_software_user_sync; }
    public void setManaged_software_user_sync(Integer v) { this.managed_software_user_sync = v; }
    public Integer getManaged_software_sync() { return managed_software_sync; }
    public void setManaged_software_sync(Integer v) { this.managed_software_sync = v; }
    public Integer getVdms_transfer() { return vdms_transfer; }
    public void setVdms_transfer(Integer v) { this.vdms_transfer = v; }
    public Integer getProxy_server_host_sync() { return proxy_server_host_sync; }
    public void setProxy_server_host_sync(Integer v) { this.proxy_server_host_sync = v; }
    public Integer getProxy_client_sync() { return proxy_client_sync; }
    public void setProxy_client_sync(Integer v) { this.proxy_client_sync = v; }
    public InventoryDeviceSyncDTO getInventory_device_sync() { return inventory_device_sync; }
    public void setInventory_device_sync(InventoryDeviceSyncDTO v) { this.inventory_device_sync = v; }
    public Integer getSclera_agent_permission_sync() { return sclera_agent_permission_sync; }
    public void setSclera_agent_permission_sync(Integer v) { this.sclera_agent_permission_sync = v; }
    public Integer getQr_sync() { return qr_sync; }
    public void setQr_sync(Integer v) { this.qr_sync = v; }
    public Integer getNfc_sync() { return nfc_sync; }
    public void setNfc_sync(Integer v) { this.nfc_sync = v; }
    public Integer getBarcode_sync() { return barcode_sync; }
    public void setBarcode_sync(Integer v) { this.barcode_sync = v; }
    public List<io.sclera.dto.dockerSyncDTO> getDockers() { return dockers; }
    public void setDockers(List<io.sclera.dto.dockerSyncDTO> v) { this.dockers = v; }
}
