package io.sclera.controller.admin;

import io.sclera.dto.*;
import io.sclera.service.impl.PropertyQrcodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST endpoints for managing property services, their tagged locations, service requests/responses
 * and QR-code zone maps.
 * Delegates all persistence and business logic to {@link PropertyQrcodeService}.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Property Services", description = "Manage property services, tagged locations, service requests/responses and QR-code zone maps.")
public class PropertyServiceController {
    private static final Logger log = LoggerFactory.getLogger(PropertyServiceController.class);

    @Autowired
    PropertyQrcodeService propertyQrcodeService;

    // Upsert Property service and Property Service Requests
    /**
     * Creates or updates a property service and its associated service requests.
     *
     * @param username         owning user
     * @param vdmsid           owning VDMS id
     * @param propertyService  property service payload to upsert
     * @return the upserted property service
     */
    @Operation(summary = "Upsert a property service",
            description = "Creates or updates a property service and its associated service requests.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property service upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertpropertyservice")
    public PropertyServiceDTO upsertPropertyServiceDetails(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody PropertyServiceDTO propertyService) {
        log.info("upsertPropertyServiceDetails username={} vdmsid={}", username, vdmsid);
        return propertyQrcodeService.upsertPropertyServiceDetails(username, vdmsid, propertyService);
    }

    // Add Locations to Property Service
//    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/service/{property_service_id}/addpropertyservicelocations")
//    public void addPropertyServiceLocations(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String property_service_id, @RequestBody Set<BuildingDTO> buildingDTOS) {
//        propertyQrcodeService.addPropertyServiceLocations(username, vdmsid, property_service_id,buildingDTOS);
//    }

    /**
     * Adds locations to the given property service.
     *
     * @param username             owning user
     * @param vdmsid               owning VDMS id
     * @param property_service_id  property service the locations are added to
     * @param locationDTOS         locations to add
     */
    @Operation(summary = "Add locations to a property service",
            description = "Adds locations to the given property service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations added"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/service/{property_service_id}/addpropertyservicelocations")
    public void addPropertyServiceLocations(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Property service the locations are added to") @PathVariable String property_service_id,
            @RequestBody Set<LocationDTO> locationDTOS) {
        log.info("addPropertyServiceLocations username={} vdmsid={} property_service_id={}", username, vdmsid, property_service_id);
        propertyQrcodeService.addPropertyServiceLocations(username, vdmsid, property_service_id, locationDTOS);
    }


    // update property service response
    /**
     * Updates multiple property service responses in a single request.
     *
     * @param username                  owning user
     * @param vdmsid                    owning VDMS id
     * @param propertyServiceResponses  property service responses to update
     */
    @Operation(summary = "Update property service responses",
            description = "Updates multiple property service responses in a single request.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property service responses updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/updatepropertyserviceresponses")
    public void multiUpdatePropertyServiceResponse(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody Set<PropertyServiceResponseDTO> propertyServiceResponses) {
        log.info("multiUpdatePropertyServiceResponse username={} vdmsid={}", username, vdmsid);
        propertyQrcodeService.multiUpdatePropertyServiceResponse(username, vdmsid, propertyServiceResponses);
    }

    //Get Property Services
    /**
     * Returns all property services for the given user and VDMS.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @return the property services
     */
    @Operation(summary = "Get property services",
            description = "Returns all property services for the given user and VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property services returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getpropertyservices")
    public Set<PropertyServiceDTO> getPropertyServices(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid) {
        log.info("getPropertyServices username={} vdmsid={}", username, vdmsid);
        return propertyQrcodeService.getPropertyServices(username, vdmsid);
    }

    // Get property service locations
    /**
     * Returns the locations tagged to the given property service.
     *
     * @param username             owning user
     * @param vdmsid               owning VDMS id
     * @param property_service_id  property service whose locations are requested
     * @return the tagged locations as QR-code entries
     */
    @Operation(summary = "Get property service locations",
            description = "Returns the locations tagged to the given property service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tagged locations returned"),
            @ApiResponse(responseCode = "404", description = "Property service not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/service/{property_service_id}/getpropertyservicelocations")
    public Set<PropertyQrcodeDTO> getPropertyServiceLocationsById(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Property service whose locations are requested") @PathVariable String property_service_id) {
        log.info("getPropertyServiceLocationsById username={} vdmsid={} property_service_id={}", username, vdmsid, property_service_id);
        return propertyQrcodeService.getPropertyServiceLocationsById(username, vdmsid, property_service_id);
    }

    //delete property service requests
    /**
     * Deletes the given property service requests.
     *
     * @param username                 owning user
     * @param vdmsid                   owning VDMS id
     * @param propertyServiceRequests  property service requests to delete
     */
    @Operation(summary = "Delete property service requests",
            description = "Deletes the given property service requests.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property service requests deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/deletepropertyservicerequests")
    public void deletePropertyServiceRequests(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody Set<PropertyServiceRequestDTO> propertyServiceRequests) {
        log.info("deletePropertyServiceRequests username={} vdmsid={}", username, vdmsid);
        propertyQrcodeService.deletePropertyServiceRequests(username, vdmsid, propertyServiceRequests);
    }

    //delete locations tagged to service
    /**
     * Deletes the given locations tagged to the property service.
     *
     * @param username             owning user
     * @param vdmsid               owning VDMS id
     * @param property_service_id  property service the locations are removed from
     * @param locations            ids of the locations to delete
     */
    @Operation(summary = "Delete property service locations",
            description = "Deletes the given locations tagged to the property service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tagged locations deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/service/{property_service_id}/deletepropertyservicelocations")
    public void deletePropertyServiceLocations(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Property service the locations are removed from") @PathVariable String property_service_id,
            @RequestBody Set<String> locations) {
        log.info("deletePropertyServiceLocations username={} vdmsid={} property_service_id={}", username, vdmsid, property_service_id);
        propertyQrcodeService.deletePropertyServiceLocations(username, vdmsid, property_service_id, locations);
    }

    //delete property service
    /**
     * Deletes the property service identified by the given id.
     *
     * @param username             owning user
     * @param vdmsid               owning VDMS id
     * @param property_service_id  property service to delete
     */
    @Operation(summary = "Delete a property service",
            description = "Deletes the property service identified by the given id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property service deleted"),
            @ApiResponse(responseCode = "404", description = "Property service not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/service/{property_service_id}/deletepropertyservice")
    public void deletePropertyService(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Property service to delete") @PathVariable String property_service_id) {
        log.info("deletePropertyService username={} vdmsid={} property_service_id={}", username, vdmsid, property_service_id);
        propertyQrcodeService.deletePropertyService(username, vdmsid, property_service_id);
    }

    // Get zone map
    /**
     * Returns the QR-code zone map for the given building, floor, location and property service.
     *
     * @param username             owning user
     * @param vdmsid               owning VDMS id
     * @param building_id          building scope of the zone map
     * @param floor_id             floor scope of the zone map
     * @param location_id          location scope of the zone map
     * @param property_service_id  property service scope of the zone map
     * @return the zone map as QR-code entries
     */
    @Operation(summary = "Get QR-code zone map",
            description = "Returns the QR-code zone map for the given building, floor, location and property service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zone map returned"),
            @ApiResponse(responseCode = "404", description = "Zone map not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/building/{building_id}/floor/{floor_id}/location/{location_id}/service/{property_service_id}/getzonemap")
    public Set<PropertyQrcodeDTO> getZoneMap(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Building scope of the zone map") @PathVariable String building_id,
            @Parameter(description = "Floor scope of the zone map") @PathVariable String floor_id,
            @Parameter(description = "Location scope of the zone map") @PathVariable String location_id,
            @Parameter(description = "Property service scope of the zone map") @PathVariable String property_service_id) {
        log.info("getZoneMap username={} vdmsid={} building_id={} floor_id={} location_id={} property_service_id={}", username, vdmsid, building_id, floor_id, location_id, property_service_id);
        return propertyQrcodeService.getZoneMap(username, vdmsid, building_id, floor_id, location_id, property_service_id);
    }

}
