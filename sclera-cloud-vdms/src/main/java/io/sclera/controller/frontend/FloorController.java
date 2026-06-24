package io.sclera.controller.frontend;


import io.sclera.dto.ResponseDTO;
import io.sclera.service.FloorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/api")
public class FloorController {

    @Autowired
    private FloorService floorService;

    @GetMapping(value = "/organisation/{orgId}/user/{email}/floor/{floorId}/getRegionCoordinates")
    public ResponseEntity<ResponseDTO> getRegionCoordinates(@PathVariable String orgId , @PathVariable String email , @PathVariable String floorId ,
                                                            @RequestParam(required = true ,name = "floorMapUrl") String floorMapUrl ,
                                                            HttpServletRequest httpServletRequest) throws MalformedURLException, InterruptedException {
        return floorService.getRegionCoordinates(orgId ,email ,floorId ,floorMapUrl ,httpServletRequest);
    }




}
