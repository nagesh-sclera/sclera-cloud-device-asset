package io.sclera.controller.admin;

import io.sclera.dto.FloorDTO;
import io.sclera.dto.LocationDTO;
import io.sclera.service.FloorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * REST endpoints for managing building floors, their images and floor map paths.
 * Delegates all persistence and business logic to {@link FloorService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class FloorController {

    private static final Logger log = LoggerFactory.getLogger(FloorController.class);

    @Autowired
    FloorService floorService;

    /**
     * Creates or updates the floors belonging to the given building.
     *
     * @param username            owning user
     * @param vdms_id             owning VDMS id
     * @param building_id         building the floors belong to
     * @param floors              floors to upsert
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     * @return the upserted floors
     */
    @RequestMapping(method = RequestMethod.POST, value = "/building/{building_id}/upsertfloors")
    public Set<FloorDTO> upsertFloorsByBuildingId(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String building_id, @RequestBody Set<FloorDTO> floors,  HttpServletRequest httpServletRequest) {
        log.info("upsertFloorsByBuildingId username={} vdms_id={} building_id={}", username, vdms_id, building_id);
        try {
            return floorService.upsertFloorsByBuildingId(username, vdms_id, building_id, floors, httpServletRequest);
        } catch (Exception e) {
            log.error("upsertFloorsByBuildingId failed building_id={}: {}", building_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Adds or updates a floor image and its details for a floor.
     *
     * @param username            owning user
     * @param vdms_id             owning VDMS id
     * @param floor_image         floor image file to store (optional)
     * @param floor_dto           serialized floor details payload (optional)
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     * @return status/identifier resulting from the image upsert
     */
    @RequestMapping(method = RequestMethod.POST, value = "/upsertfloordetails")
    public Integer addFloorImageByFloorId(@RequestParam String username, @RequestParam String vdms_id, @RequestParam(value = "images", required = false) MultipartFile floor_image,
                                          @RequestParam(value = "floor", required = false)  String floor_dto, HttpServletRequest httpServletRequest) {
        log.info("addFloorImageByFloorId username={} vdms_id={}", username, vdms_id);
        try {
            return floorService.addFloorImageByFloorId(username, vdms_id, floor_image,floor_dto, httpServletRequest);
        } catch (Exception e) {
            log.error("addFloorImageByFloorId failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Deletes the floors identified by the given ids.
     *
     * @param username            owning user
     * @param vdms_id             owning VDMS id
     * @param floor_ids           ids of the floors to delete
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/building/deletefloors")
    public void deleteFloorsByIds(@RequestParam String username , @RequestParam String vdms_id , @RequestBody Set<String> floor_ids, HttpServletRequest httpServletRequest){
        log.info("deleteFloorsByIds username={} vdms_id={}", username, vdms_id);
        try {
            floorService.deleteFloorsByIds(username ,vdms_id ,floor_ids, httpServletRequest);
        } catch (Exception e) {
            log.error("deleteFloorsByIds failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Deletes the image associated with the given floor.
     *
     * @param username            owning user
     * @param vdms_id             owning VDMS id
     * @param floor_id            floor whose image is deleted
     * @param clear_path          whether to also clear the stored image path (default "no")
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/floor/{floor_id}/deletefloorimage")
    public void deleteFloorImageByFloorId(@RequestParam String username, @RequestParam String vdms_id,  @PathVariable String floor_id, @RequestParam(defaultValue = "no") String clear_path, HttpServletRequest httpServletRequest) {
        log.info("deleteFloorImageByFloorId username={} vdms_id={} floor_id={} clear_path={}", username, vdms_id, floor_id, clear_path);
        try {
            floorService.deleteFloorImageByFloorId(username, vdms_id,floor_id,clear_path, httpServletRequest);
        } catch (Exception e) {
            log.error("deleteFloorImageByFloorId failed floor_id={}: {}", floor_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates the stored map path for the given floor.
     *
     * @param username            owning user
     * @param floor_id            floor whose path is updated
     * @param path                new floor map path
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     * @return status of the path update
     */
    @RequestMapping(method = RequestMethod.POST, value = "/floor/{floor_id}/updatefloorpath")
    public String updatePathByFloorId(@RequestParam String username, @PathVariable String floor_id, @RequestBody String path, HttpServletRequest httpServletRequest) {
        log.info("updatePathByFloorId username={} floor_id={}", username, floor_id);
        try {
            return floorService.updatePathByFloorId(username,floor_id,path, httpServletRequest);
        } catch (Exception e) {
            log.error("updatePathByFloorId failed floor_id={}: {}", floor_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the stored map path for the given floor.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param floor_id  floor whose path is requested
     * @return the floor map path
     */
    @RequestMapping(method = RequestMethod.GET, value = "/floor/{floor_id}/getfloorpathbyfloorid")
    public String getFloorPathByFloorId(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String floor_id) {
        log.info("getFloorPathByFloorId username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        try {
            return floorService.getFloorPathByFloorId(username, vdms_id, floor_id);
        } catch (Exception e) {
            log.error("getFloorPathByFloorId failed floor_id={}: {}", floor_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the floor identified by the given id.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param floor_id  floor to return
     * @return the floor
     */
    @RequestMapping(method = RequestMethod.GET, value = "/floor/{floor_id}/getfloorbyfloorid")
    public FloorDTO getFloorByFloorId(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String floor_id) {
        log.info("getFloorByFloorId username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        try {
            return floorService.getFloorByFloorId(username, vdms_id, floor_id);
        } catch (Exception e) {
            log.error("getFloorByFloorId failed floor_id={}: {}", floor_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the floors belonging to the given building, optionally filtered by a field.
     *
     * @param username     owning user
     * @param vdms_id      owning VDMS id
     * @param building_id  building whose floors are requested
     * @param field        optional field name to filter on
     * @param field_id     optional field value to filter on
     * @return the matching floors
     */
    @RequestMapping(method = RequestMethod.GET, value = "/building/{building_id}/getfloorsbybuildingid")
    public Set<FloorDTO> getFloorsByBuildingId(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String building_id ,
                                               @RequestParam(required = false) String field,@RequestParam(required = false) String field_id) {
        log.info("getFloorsByBuildingId username={} vdms_id={} building_id={}", username, vdms_id, building_id);
        try {
            return floorService.getFloorsByBuildingId(username, vdms_id, building_id, field, field_id);
        } catch (Exception e) {
            log.error("getFloorsByBuildingId failed building_id={}: {}", building_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the detailed information (including image details) for the given floor.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param floor_id  floor whose details are requested
     * @return the floor details
     */
    @RequestMapping(method = RequestMethod.GET, value = "/floor/{floor_id}/getfloordetailsbyfloorid")
    public FloorDTO getFloorDetailsByFloorId(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String floor_id) {
        log.info("getFloorDetailsByFloorId username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        try {
            return floorService.getFloorDetailsByFloorId(username, vdms_id, floor_id);
        } catch (Exception e) {
            log.error("getFloorDetailsByFloorId failed floor_id={}: {}", floor_id, e.getMessage(), e);
            throw e;
        }
    }

}
