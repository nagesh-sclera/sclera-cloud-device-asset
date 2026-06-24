package io.sclera.controller.frontend;


import io.sclera.dto.CategoryDTO;
import io.sclera.service.ProcedureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/procedures")
public class ProcedureController {

    @Autowired
    private ProcedureService procedureService;

    @GetMapping
    public ResponseEntity<?> getAllProcedure(@RequestParam(required = false, defaultValue = "all") String key,
                                             @RequestParam(required = false, defaultValue = "creation_timestamp") String sort,
                                             @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                             @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int pageSize,
                                             @RequestParam(name = "loggedInUser") String loggedInUser, HttpServletRequest httpServletRequest) {
        return procedureService.getAllProcedure(key, sort, pageNo, pageSize, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/addProcedure")
    public ResponseEntity<?> addProcedure(@RequestParam(value = "body") String body,
                                          @RequestParam(value = "icon", required = false) MultipartFile icon,
                                          @RequestParam(name = "loggedInUser") String loggedInUser,
                                          HttpServletRequest httpServletRequest) throws IOException {
        return procedureService.addProcedure(body, icon, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/{procedureId}")
    public ResponseEntity<?> updateProcedureById(@PathVariable String procedureId, @RequestParam(value = "body") String body,
                                                 @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                 @RequestParam(name = "loggedInUser") String loggedInUser,
                                                 @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                 HttpServletRequest httpServletRequest) throws IOException {
        return procedureService.updateProcedureById(procedureId, body, icon, loggedInUser,iconUrl, httpServletRequest);
    }

    @PutMapping(value = "/{procedureId}/icon")
    public ResponseEntity<?> updateProcedureIconById(@PathVariable String procedureId, @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                     @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                     @RequestParam(name = "loggedInUser") String loggedInUser,
                                                     HttpServletRequest httpServletRequest) throws IOException {
        return procedureService.updateProcedureIconById(procedureId, iconUrl, icon, loggedInUser, httpServletRequest);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProcedure(@RequestBody List<String> procedureIds, @RequestParam(name = "loggedInUser") String loggedInUser,
                                             HttpServletRequest httpServletRequest) {
        return procedureService.deleteProcedure(procedureIds, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/addBulkProcedure")
    public ResponseEntity<?> addBulkProcedure(@RequestBody List<CategoryDTO>categoryDTOS,
                                              @RequestParam(name = "loggedInUser") String loggedInUser,
                                              HttpServletRequest httpServletRequest) throws IOException {
        return procedureService.addBulkProcedure(categoryDTOS, loggedInUser, httpServletRequest);
    }
}
