package io.sclera.interfaces;

import io.sclera.dto.LocationHistoryDTO;
import java.util.Set;

/** Service contract for the matching service class. */
public interface LocationHistoryServiceInterface {
    void addLocationHistory(String username, String vdmsid, LocationHistoryDTO locationHistory);
    Set<LocationHistoryDTO> getLocationHistory(String username, String vdmsid, String location_id);
}
