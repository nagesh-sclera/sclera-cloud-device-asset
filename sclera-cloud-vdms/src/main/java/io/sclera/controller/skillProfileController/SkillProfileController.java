package io.sclera.controller.skillProfileController;


import io.sclera.dto.ResponseDTO;
import io.sclera.service.SkillProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skillProfile")
public class SkillProfileController {

    @Autowired
    private SkillProfileService skillProfileService;

    @PutMapping(value = "/vdms/{vdmsId}/syncSkillProfileByVdmsId")
    public ResponseEntity<ResponseDTO> syncSkillProfileByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return skillProfileService.syncSkillProfileByVdmsId(vdmsId, httpServletRequest);
    }
}
