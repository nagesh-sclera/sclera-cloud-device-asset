package io.sclera.controller.admin;

import io.sclera.dto.*;
import io.sclera.service.PropertyQrcodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class PropertyServiceController {

    @Autowired
    PropertyQrcodeService propertyQrcodeService;

    // Upsert Property service and Property Service Requests
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertpropertyservice")
    public PropertyServiceDTO upsertPropertyServiceDetails(@PathVariable String username, @PathVariable String vdmsid, @RequestBody PropertyServiceDTO propertyService) {
        return propertyQrcodeService.upsertPropertyServiceDetails(username, vdmsid, propertyService);
    }

    // Add Locations to Property Service
//    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/service/{property_service_id}/addpropertyservicelocations")
//    public void addPropertyServiceLocations(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String property_service_id, @RequestBody Set<BuildingDTO> buildingDTOS) {
//        propertyQrcodeService.addPropertyServiceLocations(username, vdmsid, property_service_id,buildingDTOS);
//    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/service/{property_service_id}/addpropertyservicelocations")
    public void addPropertyServiceLocations(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String property_service_id, @RequestBody Set<LocationDTO> locationDTOS) {
        propertyQrcodeService.addPropertyServiceLocations(username, vdmsid, property_service_id,locationDTOS);
    }


    // update property service response
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/updatepropertyserviceresponses")
    public void multiUpdatePropertyServiceResponse(@PathVariable String username, @PathVariable String vdmsid,@RequestBody Set<PropertyServiceResponseDTO> propertyServiceResponses) {
        propertyQrcodeService.multiUpdatePropertyServiceResponse(username,vdmsid,propertyServiceResponses);
    }

    //Get Property Services
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getpropertyservices")
    public Set<PropertyServiceDTO> getPropertyServices(@PathVariable String username, @PathVariable String vdmsid) {
       return propertyQrcodeService.getPropertyServices(username, vdmsid);
    }

    // Get property service locations
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/service/{property_service_id}/getpropertyservicelocations")
    public  Set<PropertyQrcodeDTO> getPropertyServiceLocationsById(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String property_service_id) {
        return propertyQrcodeService.getPropertyServiceLocationsById(username, vdmsid, property_service_id);
    }

    //delete property service requests
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/deletepropertyservicerequests")
    public void deletePropertyServiceRequests(@PathVariable String username, @PathVariable String vdmsid, @RequestBody Set<PropertyServiceRequestDTO> propertyServiceRequests) {
        propertyQrcodeService.deletePropertyServiceRequests(username, vdmsid, propertyServiceRequests);
    }

    //delete locations tagged to service
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/service/{property_service_id}/deletepropertyservicelocations")
    public void deletePropertyServiceLocations(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String property_service_id,
                                              @RequestBody Set<String> locations) {
        propertyQrcodeService.deletePropertyServiceLocations(username, vdmsid, property_service_id, locations);
    }

    //delete property service
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/service/{property_service_id}/deletepropertyservice")
    public void deletePropertyService(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String property_service_id) {
        propertyQrcodeService.deletePropertyService(username, vdmsid, property_service_id);
    }

    // Get zone map
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/building/{building_id}/floor/{floor_id}/location/{location_id}/service/{property_service_id}/getzonemap")
    public  Set<PropertyQrcodeDTO> getZoneMap(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String building_id,@PathVariable String floor_id, @PathVariable String location_id,@PathVariable String property_service_id) {
       return propertyQrcodeService.getZoneMap(username, vdmsid,building_id,floor_id,location_id,property_service_id);
    }
   
}
