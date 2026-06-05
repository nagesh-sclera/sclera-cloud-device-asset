package io.sclera.controller.admin;

import io.sclera.dto.LocationHistoryDTO;
import io.sclera.service.LocationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST endpoints recording and retrieving the change history of a location.
 * Delegates to {@link LocationHistoryService}.
 */
@RestController
@CrossOrigin(allowedHeaders = "*", origins = "*")
public class LocationHistoryController {

    @Autowired
    LocationHistoryService locationHistoryService;

    /**
     * Appends a history entry describing a location change.
     *
     * @param username        owning user
     * @param vdmsid          owning VDMS id
     * @param locationHistory history entry to record
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/addlocationhistory")
    public void addLocationHistory(@PathVariable String username, @PathVariable String vdmsid, @RequestBody LocationHistoryDTO locationHistory) {
        locationHistoryService.addLocationHistory(username, vdmsid, locationHistory);
    }


    /**
     * Returns the recorded history for the given location.
     *
     * @param username    owning user
     * @param vdmsid      owning VDMS id
     * @param location_id location whose history is fetched
     * @return set of history entries for the location
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/location/{location_id}/getlocationhistory")
    public Set<LocationHistoryDTO> getLocationHistory(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String location_id) {
        return locationHistoryService.getLocationHistory(username, vdmsid, location_id);
    }
}