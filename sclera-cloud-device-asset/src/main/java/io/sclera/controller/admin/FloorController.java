package io.sclera.controller.admin;

import io.sclera.dto.FloorDTO;
import io.sclera.dto.LocationDTO;
import io.sclera.service.FloorService;
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
public class FloorController {

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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/building/{building_id}/upsertfloors")
    public Set<FloorDTO> upsertFloorsByBuildingId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String building_id, @RequestBody Set<FloorDTO> floors,  HttpServletRequest httpServletRequest) {
        return floorService.upsertFloorsByBuildingId(username, vdms_id, building_id, floors, httpServletRequest);
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/upsertfloordetails")
    public Integer addFloorImageByFloorId(@PathVariable String username, @PathVariable String vdms_id, @RequestParam(value = "images", required = false) MultipartFile floor_image,
                                          @RequestParam(value = "floor", required = false)  String floor_dto, HttpServletRequest httpServletRequest) {
        return floorService.addFloorImageByFloorId(username, vdms_id, floor_image,floor_dto, httpServletRequest);
    }


    /**
     * Deletes the floors identified by the given ids.
     *
     * @param username            owning user
     * @param vdms_id             owning VDMS id
     * @param floor_ids           ids of the floors to delete
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdms_id}/building/deletefloors")
    public void deleteFloorsByIds(@PathVariable String username , @PathVariable String vdms_id , @RequestBody Set<String> floor_ids, HttpServletRequest httpServletRequest){
        floorService.deleteFloorsByIds(username ,vdms_id ,floor_ids, httpServletRequest);
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/deletefloorimage")
    public void deleteFloorImageByFloorId(@PathVariable String username, @PathVariable String vdms_id,  @PathVariable String floor_id, @RequestParam(defaultValue = "no") String clear_path, HttpServletRequest httpServletRequest) {
        floorService.deleteFloorImageByFloorId(username, vdms_id,floor_id,clear_path, httpServletRequest);
    }

    /**
     * Updates the stored map path for the given floor.
     *
     * @param username            owning user
     * @param vdms_id             owning VDMS id
     * @param floor_id            floor whose path is updated
     * @param path                new floor map path
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     * @return status of the path update
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/updatefloorpath")
    public String updatePathByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id, @RequestBody String path, HttpServletRequest httpServletRequest) {
        return floorService.updatePathByFloorId(username,floor_id,path, httpServletRequest);
    }

    /**
     * Returns the stored map path for the given floor.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param floor_id  floor whose path is requested
     * @return the floor map path
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getfloorpathbyfloorid")
    public String getFloorPathByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id) {
        return floorService.getFloorPathByFloorId(username, vdms_id, floor_id);
    }

    /**
     * Returns the floor identified by the given id.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param floor_id  floor to return
     * @return the floor
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getfloorbyfloorid")
    public FloorDTO getFloorByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id) {
        return floorService.getFloorByFloorId(username, vdms_id, floor_id);
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/building/{building_id}/getfloorsbybuildingid")
    public Set<FloorDTO> getFloorsByBuildingId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String building_id ,
                                               @RequestParam(required = false) String field,@RequestParam(required = false) String field_id) {
        return floorService.getFloorsByBuildingId(username, vdms_id, building_id, field, field_id);
    }

    /**
     * Returns the detailed information (including image details) for the given floor.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param floor_id  floor whose details are requested
     * @return the floor details
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getfloordetailsbyfloorid")
    public FloorDTO getFloorDetailsByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id) {
        return floorService.getFloorDetailsByFloorId(username, vdms_id, floor_id);
    }

}
