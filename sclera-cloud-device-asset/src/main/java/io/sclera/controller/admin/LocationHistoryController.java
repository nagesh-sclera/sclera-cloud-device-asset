package io.sclera.controller.admin;

import io.sclera.dto.LocationHistoryDTO;
import io.sclera.service.LocationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@CrossOrigin(allowedHeaders = "*", origins = "*")
public class LocationHistoryController {

    @Autowired
    LocationHistoryService locationHistoryService;

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/addlocationhistory")
    public void addLocationHistory(@PathVariable String username, @PathVariable String vdmsid, @RequestBody LocationHistoryDTO locationHistory) {
        locationHistoryService.addLocationHistory(username, vdmsid, locationHistory);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/location/{location_id}/getlocationhistory")
    public Set<LocationHistoryDTO> getLocationHistory(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String location_id) {
        return locationHistoryService.getLocationHistory(username, vdmsid, location_id);
    }
}