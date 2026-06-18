package io.sclera.controller.admin;

import io.sclera.dto.FloorDTO;
import io.sclera.dto.LocationDTO;
import io.sclera.service.FloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Floors", description = "Create, read, delete and update building floors, their images and floor map paths for a VDMS.")
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
    @Operation(summary = "Upsert floors for a building",
            description = "Creates or updates the floors belonging to the given building.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floors upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/building/{building_id}/upsertfloors")
    public Set<FloorDTO> upsertFloorsByBuildingId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Building the floors belong to") @PathVariable String building_id,
            @RequestBody Set<FloorDTO> floors, HttpServletRequest httpServletRequest) {
        log.info("upsertFloorsByBuildingId username={} vdms_id={} building_id={}", username, vdms_id, building_id);
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
    @Operation(summary = "Add or update a floor image",
            description = "Adds or updates a floor image and its details for a floor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor image upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertfloordetails")
    public Integer addFloorImageByFloorId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor image file to store") @RequestParam(value = "images", required = false) MultipartFile floor_image,
            @Parameter(description = "Serialized floor details payload") @RequestParam(value = "floor", required = false) String floor_dto,
            HttpServletRequest httpServletRequest) {
        log.info("addFloorImageByFloorId username={} vdms_id={}", username, vdms_id);
        return floorService.addFloorImageByFloorId(username, vdms_id, floor_image, floor_dto, httpServletRequest);
    }


    /**
     * Deletes the floors identified by the given ids.
     *
     * @param username            owning user
     * @param vdms_id             owning VDMS id
     * @param floor_ids           ids of the floors to delete
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Delete floors by ids",
            description = "Deletes the floors identified by the given ids.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floors deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/building/deletefloors")
    public void deleteFloorsByIds(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @RequestBody Set<String> floor_ids, HttpServletRequest httpServletRequest) {
        log.info("deleteFloorsByIds username={} vdms_id={}", username, vdms_id);
        floorService.deleteFloorsByIds(username, vdms_id, floor_ids, httpServletRequest);
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
    @Operation(summary = "Delete a floor image",
            description = "Deletes the image associated with the given floor. Optionally clears the stored image path when clear_path is set.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor image deleted"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/floor/{floor_id}/deletefloorimage")
    public void deleteFloorImageByFloorId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor whose image is deleted") @PathVariable String floor_id,
            @Parameter(description = "Whether to also clear the stored image path") @RequestParam(defaultValue = "no") String clear_path,
            HttpServletRequest httpServletRequest) {
        log.info("deleteFloorImageByFloorId username={} vdms_id={} floor_id={} clear_path={}", username, vdms_id, floor_id, clear_path);
        floorService.deleteFloorImageByFloorId(username, vdms_id, floor_id, clear_path, httpServletRequest);
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
    @Operation(summary = "Update floor map path",
            description = "Updates the stored map path for the given floor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor path updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/floor/{floor_id}/updatefloorpath")
    public String updatePathByFloorId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Floor whose path is updated") @PathVariable String floor_id,
            @RequestBody String path, HttpServletRequest httpServletRequest) {
        log.info("updatePathByFloorId username={} floor_id={}", username, floor_id);
        return floorService.updatePathByFloorId(username, floor_id, path, httpServletRequest);
    }

    /**
     * Returns the stored map path for the given floor.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param floor_id  floor whose path is requested
     * @return the floor map path
     */
    @Operation(summary = "Get floor map path by floor id",
            description = "Returns the stored map path for the given floor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor path returned"),
            @ApiResponse(responseCode = "404", description = "Floor not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/floor/{floor_id}/getfloorpathbyfloorid")
    public String getFloorPathByFloorId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor whose path is requested") @PathVariable String floor_id) {
        log.info("getFloorPathByFloorId username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
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
    @Operation(summary = "Get floor by floor id",
            description = "Returns the floor identified by the given id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor returned"),
            @ApiResponse(responseCode = "404", description = "Floor not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/floor/{floor_id}/getfloorbyfloorid")
    public FloorDTO getFloorByFloorId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor to return") @PathVariable String floor_id) {
        log.info("getFloorByFloorId username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
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
    @Operation(summary = "Get floors by building id",
            description = "Returns the floors belonging to the given building, optionally filtered by a field name and value.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floors returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/building/{building_id}/getfloorsbybuildingid")
    public Set<FloorDTO> getFloorsByBuildingId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Building whose floors are requested") @PathVariable String building_id,
            @Parameter(description = "Optional field name to filter on") @RequestParam(required = false) String field,
            @Parameter(description = "Optional field value to filter on") @RequestParam(required = false) String field_id) {
        log.info("getFloorsByBuildingId username={} vdms_id={} building_id={}", username, vdms_id, building_id);
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
    @Operation(summary = "Get floor details by floor id",
            description = "Returns the detailed information, including image details, for the given floor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor details returned"),
            @ApiResponse(responseCode = "404", description = "Floor not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/floor/{floor_id}/getfloordetailsbyfloorid")
    public FloorDTO getFloorDetailsByFloorId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor whose details are requested") @PathVariable String floor_id) {
        log.info("getFloorDetailsByFloorId username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        return floorService.getFloorDetailsByFloorId(username, vdms_id, floor_id);
    }

}
