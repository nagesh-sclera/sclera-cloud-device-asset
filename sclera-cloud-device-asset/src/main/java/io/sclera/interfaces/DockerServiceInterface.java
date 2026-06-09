package io.sclera.interfaces;

import io.sclera.dto.touchscreen.settings.DockerDTO;

/** Service contract for {@link io.sclera.service.DockerService}. */
public interface DockerServiceInterface {
    DockerDTO checkIfHostNetworkPresentByNetworkOrigin(Integer networkOrigin);

    String getDockerInternalIp(String dockerName);

    String getGatewayIp(String dockerName);

    void updateDockerNetworkOrigin(String vdmsId);

    String getInternalIPbyDockername(String vdmsId, String dockerName);

    String getVendorOrgIdByNetworkName(String networkName);

    void updateVendorOrgIdbydocker(String vendorOrgId, String vdmsId, String dockerName);

    String getInternalInterfaceByDockerName(String dockerName);

    java.util.List<io.sclera.dto.DockerInfoDto> getDockerInterfaceList(Integer networkOrigin);

    java.util.List<io.sclera.dto.DockerInfoDto> getVdmsConfigInterfaceList();
}
