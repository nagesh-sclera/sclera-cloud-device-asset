package io.sclera.service;

import io.sclera.dto.touchscreen.settings.DockerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to edge-D */
@Service
public class DockerService {

    private static final Logger log = LoggerFactory.getLogger(DockerService.class);

    /**
     * Stub that checks whether a host network is present for the given network origin; always returns null.
     */
    public DockerDTO checkIfHostNetworkPresentByNetworkOrigin(Integer networkOrigin) {
        log.warn("STUB: checkIfHostNetworkPresentByNetworkOrigin called");
        return null;
    }

    /**
     * Stub for resolving a Docker container's internal IP; always returns null.
     */
    public String getDockerInternalIp(String dockerName) { log.warn("STUB: getDockerInternalIp"); return null; }

    /**
     * Stub for resolving a Docker container's gateway IP; always returns null.
     */
    public String getGatewayIp(String dockerName) { log.warn("STUB: getGatewayIp"); return null; }

    /**
     * Stub for updating the Docker network origin for a VDMS; currently a no-op.
     */
    public void updateDockerNetworkOrigin(String vdmsId) { log.warn("STUB: updateDockerNetworkOrigin"); }

    /**
     * Stub for resolving an internal IP by VDMS and Docker name; always returns null.
     */
    public String getInternalIPbyDockername(String vdmsId, String dockerName) { log.warn("STUB: getInternalIPbyDockername"); return null; }

    /**
     * Stub for resolving a vendor organization id by network name; always returns null.
     */
    public String getVendorOrgIdByNetworkName(String networkName) { log.warn("STUB: getVendorOrgIdByNetworkName"); return null; }

    /**
     * Stub for updating a vendor organization id on a Docker container; currently a no-op.
     */
    public void updateVendorOrgIdbydocker(String vendorOrgId, String vdmsId, String dockerName) { log.warn("STUB: updateVendorOrgIdbydocker"); }

    /**
     * Stub for resolving the internal network interface of a Docker container; always returns null.
     */
    public String getInternalInterfaceByDockerName(String dockerName) { log.warn("STUB: getInternalInterfaceByDockerName"); return null; }

    /**
     * Stub for listing Docker interfaces for a network origin; always returns an empty list.
     */
    public java.util.List<io.sclera.dto.DockerInfoDto> getDockerInterfaceList(Integer networkOrigin) { log.warn("STUB: getDockerInterfaceList"); return java.util.Collections.emptyList(); }

    /**
     * Stub for listing VDMS config Docker interfaces; always returns an empty list.
     */
    public java.util.List<io.sclera.dto.DockerInfoDto> getVdmsConfigInterfaceList() { log.warn("STUB: getVdmsConfigInterfaceList"); return java.util.Collections.emptyList(); }
}
