package io.sclera.dto.touchscreen.settings;

public class NetworkBoundaryConditionsDTO {
    private String network_name;
    private String interface_name;
    private Boolean isTagged;
    private Integer vlan_id;
    private Boolean host;

    public NetworkBoundaryConditionsDTO(){};

    public NetworkBoundaryConditionsDTO(String network_name, String interface_name, Boolean isTagged, Integer vlan_id,Boolean host) {
        this.network_name = network_name;
        this.interface_name = interface_name;
        this.isTagged = isTagged;
        this.vlan_id = vlan_id;
        this.host = host;
    }

    public Boolean getTagged() {
        return isTagged;
    }

    public void setTagged(Boolean tagged) {
        isTagged = tagged;
    }

    public String getNetwork_name() {
        return network_name;
    }

    public void setNetwork_name(String network_name) {
        this.network_name = network_name;
    }

    public String getInterface_name() {
        return interface_name;
    }

    public void setInterface_name(String interface_name) {
        this.interface_name = interface_name;
    }

    public Boolean getHost() {
        return host;
    }

    public void setHost(Boolean host) {
        this.host = host;
    }

    public Integer getVlan_id() {
        return vlan_id;
    }

    public void setVlan_id(Integer vlan_id) {
        this.vlan_id = vlan_id;
    }

    @Override
    public String toString() {
        return "NetworkBoundaryConditionsDTO{" +
                "network_name='" + network_name + '\'' +
                ", interface_name='" + interface_name + '\'' +
                ", isTagged=" + isTagged +
                ", vlan_id=" + vlan_id +
                ", host=" + host +
                '}';
    }
}
