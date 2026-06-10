package io.sclera.controller.admin;

import io.sclera.dto.LocationHistoryDTO;
import io.sclera.service.LocationHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST endpoints recording and retrieving the change history of a location.
 * Delegates to {@link LocationHistoryService}.
 */
@RestController
@CrossOrigin(allowedHeaders = "*", origins = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class LocationHistoryController {

    private static final Logger log = LoggerFactory.getLogger(LocationHistoryController.class);

    @Autowired
    LocationHistoryService locationHistoryService;

    /**
     * Appends a history entry describing a location change.
     *
     * @param username        owning user
     * @param vdmsid          owning VDMS id
     * @param locationHistory history entry to record
     */
    @RequestMapping(method = RequestMethod.POST, value = "/addlocationhistory")
    public void addLocationHistory(@RequestParam String username, @RequestParam String vdmsid, @RequestBody LocationHistoryDTO locationHistory) {
        log.info("addLocationHistory username={} vdmsid={}", username, vdmsid);
        try {
            locationHistoryService.addLocationHistory(username, vdmsid, locationHistory);
        } catch (Exception e) {
            log.error("addLocationHistory failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Returns the recorded history for the given location.
     *
     * @param username    owning user
     * @param vdmsid      owning VDMS id
     * @param location_id location whose history is fetched
     * @return set of history entries for the location
     */
    @RequestMapping(method = RequestMethod.GET, value = "/location/{location_id}/getlocationhistory")
    public Set<LocationHistoryDTO> getLocationHistory(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String location_id) {
        log.info("getLocationHistory username={} vdmsid={} location_id={}", username, vdmsid, location_id);
        try {
            return locationHistoryService.getLocationHistory(username, vdmsid, location_id);
        } catch (Exception e) {
            log.error("getLocationHistory failed username={} location_id={}: {}", username, location_id, e.getMessage(), e);
            throw e;
        }
    }
}