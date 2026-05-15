package io.sclera.controller.admin;

import io.sclera.dto.FloorDTO;
import io.sclera.dto.LocationDTO;
import io.sclera.service.FloorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FloorController {

    @Autowired
    FloorService floorService;

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/building/{building_id}/upsertfloors")
    public Set<FloorDTO> upsertFloorsByBuildingId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String building_id, @RequestBody Set<FloorDTO> floors,  HttpServletRequest httpServletRequest) {
        return floorService.upsertFloorsByBuildingId(username, vdms_id, building_id, floors, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/upsertfloordetails")
    public Integer addFloorImageByFloorId(@PathVariable String username, @PathVariable String vdms_id, @RequestParam(value = "images", required = false) MultipartFile floor_image,
                                          @RequestParam(value = "floor", required = false)  String floor_dto, HttpServletRequest httpServletRequest) {
        return floorService.addFloorImageByFloorId(username, vdms_id, floor_image,floor_dto, httpServletRequest);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdms_id}/building/deletefloors")
    public void deleteFloorsByIds(@PathVariable String username , @PathVariable String vdms_id , @RequestBody Set<String> floor_ids, HttpServletRequest httpServletRequest){
        floorService.deleteFloorsByIds(username ,vdms_id ,floor_ids, httpServletRequest);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/deletefloorimage")
    public void deleteFloorImageByFloorId(@PathVariable String username, @PathVariable String vdms_id,  @PathVariable String floor_id, @RequestParam(defaultValue = "no") String clear_path, HttpServletRequest httpServletRequest) {
        floorService.deleteFloorImageByFloorId(username, vdms_id,floor_id,clear_path, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/updatefloorpath")
    public String updatePathByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id, @RequestBody String path, HttpServletRequest httpServletRequest) {
        return floorService.updatePathByFloorId(username,floor_id,path, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getfloorpathbyfloorid")
    public String getFloorPathByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id) {
        return floorService.getFloorPathByFloorId(username, vdms_id, floor_id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getfloorbyfloorid")
    public FloorDTO getFloorByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id) {
        return floorService.getFloorByFloorId(username, vdms_id, floor_id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/building/{building_id}/getfloorsbybuildingid")
    public Set<FloorDTO> getFloorsByBuildingId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String building_id ,
                                               @RequestParam(required = false) String field,@RequestParam(required = false) String field_id) {
        return floorService.getFloorsByBuildingId(username, vdms_id, building_id, field, field_id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getfloordetailsbyfloorid")
    public FloorDTO getFloorDetailsByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id) {
        return floorService.getFloorDetailsByFloorId(username, vdms_id, floor_id);
    }

}
