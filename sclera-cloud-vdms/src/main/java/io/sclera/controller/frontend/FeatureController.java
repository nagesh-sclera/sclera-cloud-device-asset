package io.sclera.controller.frontend;

import io.sclera.dto.ResponseDTO;
import io.sclera.service.FeatureService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/feature")
public class FeatureController {

    @Autowired
    private FeatureService featureService;

    @GetMapping
    public ResponseEntity<ResponseDTO> getFeatureList(@RequestParam(name = "loggedInUser") String loggedInUser,
                                                      HttpServletRequest httpServletRequest) {
        return featureService.getFeatureList(loggedInUser, httpServletRequest);
    }

    @GetMapping("/{featureId}")
    public ResponseEntity<ResponseDTO> getFeatureDetailsById(@PathVariable String featureId, @RequestParam(name = "loggedInUser") String loggedInUser,
                                                             HttpServletRequest httpServletRequest) {
        return featureService.getFeatureDetailsById(featureId, loggedInUser, httpServletRequest);
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> addFeature(@RequestParam(name = "body") String body,
                                                  @RequestParam(name = "image", required = false) MultipartFile image,
                                                  @RequestParam(name = "loggedInUser") String loggedInUser,
                                                  HttpServletRequest httpServletRequest) throws IOException {
        return featureService.addFeature(body, image, loggedInUser, httpServletRequest);
    }

    @PutMapping("/{featureId}")
    public ResponseEntity<ResponseDTO> updateFeatureDetailsById(@PathVariable String featureId,
                                                                @RequestParam(name = "body") String body,
                                                                @RequestParam(name = "imageUrl", required = false) String imageUrl,
                                                                @RequestParam(name = "image", required = false) MultipartFile image,
                                                                @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                HttpServletRequest httpServletRequest) throws IOException {
        return featureService.updateFeatureDetailsById(featureId, body, imageUrl, image, loggedInUser, httpServletRequest);
    }


    @DeleteMapping("/{featureId}")
    public ResponseEntity<ResponseDTO> deleteFeatureById(@PathVariable String featureId, @RequestParam(name = "loggedInUser") String loggedInUser,
                                                         HttpServletRequest httpServletRequest) {
        return featureService.deleteFeatureById(featureId, loggedInUser, httpServletRequest);
    }

}
