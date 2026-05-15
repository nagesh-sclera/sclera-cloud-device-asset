package io.sclera.service;

import io.sclera.dto.touchscreen.settings.DockerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to edge-D */
@Service
public class DockerService {

    private static final Logger log = LoggerFactory.getLogger(DockerService.class);

    public DockerDTO checkIfHostNetworkPresentByNetworkOrigin(Integer networkOrigin) {
        log.warn("STUB: checkIfHostNetworkPresentByNetworkOrigin called");
        return null;
    }

    public String getDockerInternalIp(String dockerName) { log.warn("STUB: getDockerInternalIp"); return null; }
    public String getGatewayIp(String dockerName) { log.warn("STUB: getGatewayIp"); return null; }
    public void updateDockerNetworkOrigin(String vdmsId) { log.warn("STUB: updateDockerNetworkOrigin"); }
    public String getInternalIPbyDockername(String vdmsId, String dockerName) { log.warn("STUB: getInternalIPbyDockername"); return null; }
    public String getVendorOrgIdByNetworkName(String networkName) { log.warn("STUB: getVendorOrgIdByNetworkName"); return null; }
    public void updateVendorOrgIdbydocker(String vendorOrgId, String vdmsId, String dockerName) { log.warn("STUB: updateVendorOrgIdbydocker"); }
    public String getInternalInterfaceByDockerName(String dockerName) { log.warn("STUB: getInternalInterfaceByDockerName"); return null; }
    public java.util.List<io.sclera.dto.DockerInfoDto> getDockerInterfaceList(Integer networkOrigin) { log.warn("STUB: getDockerInterfaceList"); return java.util.Collections.emptyList(); }
    public java.util.List<io.sclera.dto.DockerInfoDto> getVdmsConfigInterfaceList() { log.warn("STUB: getVdmsConfigInterfaceList"); return java.util.Collections.emptyList(); }
}
