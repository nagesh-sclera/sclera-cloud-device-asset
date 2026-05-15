package io.sclera.service;

import io.sclera.dto.*;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class PropertyQrcodeService {
    public void updatePropertyServiceLocations(String locationId) {
        StubLog.warn("PropertyQrcodeService", "updatePropertyServiceLocations", "AP-C1edge");
    }
    public PropertyServiceDTO upsertPropertyServiceDetails(String username, String vdmsId, PropertyServiceDTO dto) {
        StubLog.warn("PropertyQrcodeService", "upsertPropertyServiceDetails", "AP-C1edge"); return dto;
    }
    public void addPropertyServiceLocations(String username, String vdmsId, String serviceId, Set<LocationDTO> locations) {
        StubLog.warn("PropertyQrcodeService", "addPropertyServiceLocations", "AP-C1edge");
    }
    public void multiUpdatePropertyServiceResponse(String username, String vdmsId, Set<PropertyServiceResponseDTO> responses) {
        StubLog.warn("PropertyQrcodeService", "multiUpdatePropertyServiceResponse", "AP-C1edge");
    }
    public Set<PropertyServiceDTO> getPropertyServices(String username, String vdmsId) {
        StubLog.warn("PropertyQrcodeService", "getPropertyServices", "AP-C1edge"); return Collections.emptySet();
    }
    public Set<PropertyQrcodeDTO> getPropertyServiceLocationsById(String username, String vdmsId, String serviceId) {
        StubLog.warn("PropertyQrcodeService", "getPropertyServiceLocationsById", "AP-C1edge"); return Collections.emptySet();
    }
    public void deletePropertyServiceRequests(String username, String vdmsId, Set<PropertyServiceRequestDTO> requests) {
        StubLog.warn("PropertyQrcodeService", "deletePropertyServiceRequests", "AP-C1edge");
    }
    public void deletePropertyServiceLocations(String username, String vdmsId, String serviceId, Set<String> locations) {
        StubLog.warn("PropertyQrcodeService", "deletePropertyServiceLocations", "AP-C1edge");
    }
    public void deletePropertyService(String username, String vdmsId, String serviceId) {
        StubLog.warn("PropertyQrcodeService", "deletePropertyService", "AP-C1edge");
    }
    public Set<PropertyQrcodeDTO> getZoneMap(String username, String vdmsId, String buildingId, String floorId, String locationId, String serviceId) {
        StubLog.warn("PropertyQrcodeService", "getZoneMap", "AP-C1edge"); return Collections.emptySet();
    }
    public void syncServiceValue(String vdmsId) {
        StubLog.warn("PropertyQrcodeService", "syncServiceValue", "AP-C1edge");
    }
}
