package io.sclera.controller.touchscreen;

import io.sclera.service.TouchscreenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TouchScreenKeysController {


    @Autowired
    private TouchscreenService touchscreenService;

    @GetMapping(value = "/{vdmsId}/vdms-keys/disk_key")
    public String getDiskKeyByUniqueID(@PathVariable String vdmsId ,@RequestParam String uniqueId , HttpServletRequest httpServletRequest){
        return touchscreenService.getDiskKeyByUniqueID(vdmsId ,uniqueId ,httpServletRequest);
    }

    @GetMapping(value = "/{vdmsId}/vdms-keys/mysql_key")
    public String getMySQLKeyByUniqueID(@PathVariable String vdmsId ,@RequestParam String uniqueId , HttpServletRequest httpServletRequest){
        return touchscreenService.getMySQLKeyByUniqueID(vdmsId ,uniqueId ,httpServletRequest);
    }

    @GetMapping(value = "/{vdmsId}/vdms-keys/sqlite_key")
    public String getSQLiteKeyByUniqueID(@PathVariable String vdmsId ,@RequestParam String uniqueId , HttpServletRequest httpServletRequest){
        return touchscreenService.getSQLiteKeyByUniqueID(vdmsId ,uniqueId ,httpServletRequest);
    }




}
