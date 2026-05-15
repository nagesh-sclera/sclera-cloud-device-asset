
package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PowerSourceTopologyDTO {

    List<DeviceDTO> devices;
    List<PowerSourceConnectionsDTO> connections;

    public List<DeviceDTO> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceDTO> devices) {
        this.devices = devices;
    }

    public List<PowerSourceConnectionsDTO> getConnections() {
        return connections;
    }

    public void setConnections(List<PowerSourceConnectionsDTO> connections) {
        this.connections = connections;
    }

    public PowerSourceTopologyDTO() {
    }

    @Override
    public String toString() {
        return "PowerSourceTopologyDTO{" +
                "devices=" + devices +
                ", connections=" + connections +
                '}';
    }
}
