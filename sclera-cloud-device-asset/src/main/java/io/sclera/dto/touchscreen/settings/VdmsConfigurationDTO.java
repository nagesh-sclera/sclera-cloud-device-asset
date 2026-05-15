package io.sclera.dto.touchscreen.settings;

public class VdmsConfigurationDTO {

    private String id;
    private String interface_id;
    private Integer cidr;
    private String gateway;
    private Boolean isConfigured;
    private Boolean isTagged;
    private Boolean isStatic;
    private String primary_dns;
    private String secondary_dns;
    private Integer vlan_id;
    private String private_ip;
    private String mac_address;
    private String sclera_agent_permission;

    public VdmsConfigurationDTO() {
    }

    public VdmsConfigurationDTO(String id, String interface_id, Integer cidr, String gateway, Boolean isConfigured, Boolean isTagged, Boolean isStatic, String primary_dns, String secondary_dns, Integer vlan_id, String private_ip, String mac_address) {
        this.id = id;
        this.interface_id = interface_id;
        this.cidr = cidr;
        this.gateway = gateway;
        this.isConfigured = isConfigured;
        this.isTagged = isTagged;
        this.isStatic = isStatic;
        this.primary_dns = primary_dns;
        this.secondary_dns = secondary_dns;
        this.vlan_id = vlan_id;
        this.private_ip = private_ip;
        this.mac_address = mac_address;
    }

    public VdmsConfigurationDTO(String id, String interface_id, Integer cidr, String gateway, Boolean isConfigured, Boolean isTagged, Boolean isStatic, String primary_dns, String secondary_dns, Integer vlan_id, String private_ip, String mac_address, String sclera_agent_permission) {
        this.id = id;
        this.interface_id = interface_id;
        this.cidr = cidr;
        this.gateway = gateway;
        this.isConfigured = isConfigured;
        this.isTagged = isTagged;
        this.isStatic = isStatic;
        this.primary_dns = primary_dns;
        this.secondary_dns = secondary_dns;
        this.vlan_id = vlan_id;
        this.private_ip = private_ip;
        this.mac_address = mac_address;
        this.sclera_agent_permission = sclera_agent_permission;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getPrivate_ip() {
        return private_ip;
    }

    public void setPrivate_ip(String private_ip) {
        this.private_ip = private_ip;
    }

    public String getInterface_id() {
        return interface_id;
    }

    public void setInterface_id(String interface_id) {
        this.interface_id = interface_id;
    }

    public Boolean getConfigured() {
        return isConfigured;
    }

    public void setConfigured(Boolean configured) {
        isConfigured = configured;
    }

    public Boolean getStatic() {
        return isStatic;
    }

    public void setStatic(Boolean aStatic) {
        isStatic = aStatic;
    }

    public Boolean getTagged() {
        return isTagged;
    }

    public void setTagged(Boolean tagged) {
        isTagged = tagged;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCidr() {
        return cidr;
    }

    public void setCidr(Integer cidr) {
        this.cidr = cidr;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getPrimary_dns() {
        return primary_dns;
    }

    public void setPrimary_dns(String primary_dns) {
        this.primary_dns = primary_dns;
    }

    public String getSecondary_dns() {
        return secondary_dns;
    }

    public void setSecondary_dns(String secondary_dns) {
        this.secondary_dns = secondary_dns;
    }

    public Integer getVlan_id() {
        return vlan_id;
    }

    public void setVlan_id(Integer vlan_id) {
        this.vlan_id = vlan_id;
    }

    public String getSclera_agent_permission() {
        return sclera_agent_permission;
    }

    public void setSclera_agent_permission(String sclera_agent_permission) {
        this.sclera_agent_permission = sclera_agent_permission;
    }

    @Override
    public String toString() {
        return "VdmsConfigurationDTO{" +
                "id='" + id + '\'' +
                ", interface_id='" + interface_id + '\'' +
                ", cidr=" + cidr +
                ", gateway='" + gateway + '\'' +
                ", isConfigured=" + isConfigured +
                ", isTagged=" + isTagged +
                ", isStatic=" + isStatic +
                ", primary_dns='" + primary_dns + '\'' +
                ", secondary_dns='" + secondary_dns + '\'' +
                ", vlan_id=" + vlan_id +
                ", private_ip='" + private_ip + '\'' +
                ", mac_address='" + mac_address + '\'' +
                '}';
    }
}
