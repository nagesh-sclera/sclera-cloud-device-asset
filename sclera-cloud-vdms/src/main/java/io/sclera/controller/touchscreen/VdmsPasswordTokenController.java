package io.sclera.controller.touchscreen;

import io.sclera.dto.TouchscreenDTO;
import io.sclera.service.VdmsPasswordTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/touchscreen")
public class VdmsPasswordTokenController {

    @Autowired
    private VdmsPasswordTokenService vdmsPasswordTokenService;


//    @PostMapping("/vdms/{vdmsId}/getVdmsAccessTokenByRefreshToken")
//    public ResponseEntity<?> getVdmsAccessTokenByRefreshToken(@PathVariable String vdmsId, @RequestBody TouchscreenDTO touchscreenDTO, HttpServletRequest request){
//        return  vdmsPasswordTokenService.getVdmsAccessTokenByRefreshToken(vdmsId,touchscreenDTO,request);
//    }


    @PostMapping("/vdms/{vdmsId}/getVdmsAccessTokenByVdmsIdAndPassword")
    public ResponseEntity<?> getVdmsAccessTokenByVdmsIdAndPassword(@PathVariable String vdmsId,@RequestBody String password ,HttpServletRequest httpServletRequest){
        return vdmsPasswordTokenService.getVdmsAccessTokenByVdmsIdAndPassword(vdmsId,password,httpServletRequest);
    }

//    @GetMapping("/vdms/{vdmsId}/getVdmsPasswordByVdmsId")
//    public ResponseEntity<?> getVdmsTokenPasswordByVdmsId(@PathVariable String vdmsId,HttpServletRequest httpServletRequest){
//        return vdmsPasswordTokenService.getVdmsTokenPasswordByVdmsId(vdmsId,httpServletRequest);
//    }
}
