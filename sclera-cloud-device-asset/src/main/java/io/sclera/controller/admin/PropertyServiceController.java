package io.sclera.controller.admin;

import io.sclera.dto.*;
import io.sclera.client.PropertyQrcodeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST endpoints for managing property services, their tagged locations, service requests/responses
 * and QR-code zone maps.
 * Delegates all persistence and business logic to {@link PropertyQrcodeClient}.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class PropertyServiceController {

    @Autowired
    PropertyQrcodeClient propertyQrcodeService;

    // Upsert Property service and Property Service Requests
    /**
     * Creates or updates a property service and its associated service requests.
     *
     * @param username         owning user
     * @param vdmsid           owning VDMS id
     * @param propertyService  property service payload to upsert
     * @return the upserted property service
     */
    @RequestMapping(method = RequestMethod.POST, value = "/upsertpropertyservice")
    public PropertyServiceDTO upsertPropertyServiceDetails(@RequestParam String username, @RequestParam String vdmsid, @RequestBody PropertyServiceDTO propertyService) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/service/{property_service_id}/addpropertyservicelocations")
    public void addPropertyServiceLocations(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String property_service_id, @RequestBody Set<LocationDTO> locationDTOS) {
        propertyQrcodeService.addPropertyServiceLocations(username, vdmsid, property_service_id,locationDTOS);
    }


    // update property service response
    /**
     * Updates multiple property service responses in a single request.
     *
     * @param username                  owning user
     * @param vdmsid                    owning VDMS id
     * @param propertyServiceResponses  property service responses to update
     */
    @RequestMapping(method = RequestMethod.POST, value = "/updatepropertyserviceresponses")
    public void multiUpdatePropertyServiceResponse(@RequestParam String username, @RequestParam String vdmsid,@RequestBody Set<PropertyServiceResponseDTO> propertyServiceResponses) {
        propertyQrcodeService.multiUpdatePropertyServiceResponse(username,vdmsid,propertyServiceResponses);
    }

    //Get Property Services
    /**
     * Returns all property services for the given user and VDMS.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @return the property services
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getpropertyservices")
    public Set<PropertyServiceDTO> getPropertyServices(@RequestParam String username, @RequestParam String vdmsid) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/service/{property_service_id}/getpropertyservicelocations")
    public  Set<PropertyQrcodeDTO> getPropertyServiceLocationsById(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String property_service_id) {
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/deletepropertyservicerequests")
    public void deletePropertyServiceRequests(@RequestParam String username, @RequestParam String vdmsid, @RequestBody Set<PropertyServiceRequestDTO> propertyServiceRequests) {
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/service/{property_service_id}/deletepropertyservicelocations")
    public void deletePropertyServiceLocations(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String property_service_id,
                                              @RequestBody Set<String> locations) {
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/service/{property_service_id}/deletepropertyservice")
    public void deletePropertyService(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String property_service_id) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/building/{building_id}/floor/{floor_id}/location/{location_id}/service/{property_service_id}/getzonemap")
    public  Set<PropertyQrcodeDTO> getZoneMap(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String building_id,@PathVariable String floor_id, @PathVariable String location_id,@PathVariable String property_service_id) {
       return propertyQrcodeService.getZoneMap(username, vdmsid,building_id,floor_id,location_id,property_service_id);
    }
   
}
