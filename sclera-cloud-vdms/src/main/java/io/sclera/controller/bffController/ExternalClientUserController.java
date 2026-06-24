package io.sclera.controller.bffController;

import io.sclera.dto.ExternalClientUserDTO;
import io.sclera.dto.ExternalClientUserInfoDTO;
import io.sclera.service.UserService;
import io.sclera.service.VdmsService;
import io.sclera.service.VdmsVisibilityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/bff")
public class ExternalClientUserController {

    @Autowired
    private UserService userService;
    @Autowired
    private VdmsVisibilityService vdmsVisibilityService;

    @Autowired
    private VdmsService vdmsService;

    @PostMapping(value = "/external/organisations/{orgId}/users")
    public ResponseEntity<?> addExternalClientUser(@PathVariable String orgId, @RequestBody ExternalClientUserInfoDTO userDTO, HttpServletRequest httpServletRequest){
        return userService.addExternalClientUser(orgId,userDTO,httpServletRequest);
    }
    @PutMapping(value = "/external/organisations/{orgId}/users/{email}/tagProperties")
    public ResponseEntity<?> tagPropertyByOrganisationIdAndVdmsId(@PathVariable String orgId,@PathVariable String email, @RequestBody List<String> vdmsIds,HttpServletRequest httpServletRequest){
        return vdmsVisibilityService.tagPropertyByOrganisationIdAndVdmsId(orgId,email,vdmsIds,httpServletRequest);
    }

    @GetMapping(value = "/external/organisations/{orgId}/getVdmsPropertyInfoByOrganisationId")
    public ResponseEntity<?> getVdmsPropertyInfoByOrganisationId(@PathVariable String orgId,HttpServletRequest httpServletRequest){
        return vdmsService.getVdmsPropertyInfoByOrganisationId(orgId,httpServletRequest);
    }

    @GetMapping(value = "/external/organisations/{orgId}/users/{email}/getVisibleVdmsInfoByEmail")
    public ResponseEntity<?> getVisibleVdmsInfoByEmail(@PathVariable String orgId,@PathVariable String email,HttpServletRequest httpServletRequest){
        return vdmsVisibilityService.getVisibleVdmsInfoByEmail(orgId,email,httpServletRequest);
    }

    @GetMapping(value = "/external/users/{email}/getVisibleVdmsByEmail")
    public ResponseEntity<?> getVisibleVdmsByEmail(@PathVariable String email,HttpServletRequest httpServletRequest){
        return vdmsVisibilityService.getVisibleVdmsIdsByEmail(email,httpServletRequest);
    }

    @PutMapping(value = "/external/organisations/{orgId}/users/{email}/revokeProperties")
    public ResponseEntity<?> revokeExternalUsersPropertyByVdmsIdsAndEmail(@PathVariable String orgId,@PathVariable String email,@RequestBody(required = false) List<String> vdmsIds,HttpServletRequest httpServletRequest){
        return  vdmsVisibilityService.deleteVisibleVdmsByEmailAndVdmsIds(orgId, email,vdmsIds,httpServletRequest);
    }

    @DeleteMapping(value = "/external/organisations/users/{email}")
    public ResponseEntity<?> deleteExternalClientUser(@PathVariable String email,HttpServletRequest httpServletRequest){
        return userService.deleteExternalClientUser(email,httpServletRequest);
    }

    @GetMapping(value = "/external/organisations/{orgId}/getExternalClientVdmsPropertyInfoByOrganisationId")
    public ResponseEntity<?> getExternalClientVdmsPropertyInfoByOrganisationId(@PathVariable String orgId,
                                                                               @RequestParam(defaultValue = "1") @Min(1)  int PageNo,
                                                                               @RequestParam(defaultValue = "100") @Min(1) @Max(100) int PageSize, HttpServletRequest httpServletRequest){
        return vdmsService.getExternalClientVdmsPropertyInfoByOrganisationId(orgId,PageNo,PageSize,httpServletRequest);
    }


}
