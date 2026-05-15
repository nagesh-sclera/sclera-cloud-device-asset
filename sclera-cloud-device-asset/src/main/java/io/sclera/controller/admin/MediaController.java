package io.sclera.controller.admin;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.sclera.dto.DocumentMediaDTO;
import io.sclera.service.MediaService;


@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MediaController {

    @Autowired
    MediaService mediaService;

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertmedia")
    public String upsertDocument(@PathVariable String username, @PathVariable String vdmsid, @RequestBody DocumentMediaDTO media) {


        return mediaService.upsertMedia(username, vdmsid, media);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "user/{username}/vdms/{vdmsid}/mediaid/{mediaid}/deletemedia")
    public void deleteDocumentbyId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String mediaid) {
        mediaService.deleteMedia(username, vdmsid, mediaid);
    }


    @RequestMapping(method = RequestMethod.GET, value = "user/{username}/vdms/{vdmsid}/getmedias")
    public Set<DocumentMediaDTO> getMedias(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize, @RequestParam(defaultValue = "null") String searchkey) {
        return mediaService.getMedias(username, vdmsid, pageno, pagesize, searchkey);
    }

    @RequestMapping(method = RequestMethod.GET, value = "user/{username}/vdms/{vdmsid}/device/{deviceid}/getmediabydeviceid")
    public Set<DocumentMediaDTO> getMediasByDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String deviceid, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize) {
        return mediaService.getMediasByDeviceId(username, vdmsid, deviceid, pageno, pagesize);
    }


    @RequestMapping(method = RequestMethod.POST, value = "user/{username}/vdms/{vdmsid}/tagmediatodevice")
    public void tagMediaToDevice(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(defaultValue = "add") String share_method, @RequestBody Set<DocumentMediaDTO> media) {
        mediaService.tagMediaToDevice(username, vdmsid, share_method, media);
    }


    @RequestMapping(method = RequestMethod.POST, value = "user/{username}/vdms/{vdmsid}/untagmediatodevice")
    public void untagMediaToDevice(@PathVariable String username, @PathVariable String vdmsid, @RequestBody Set<DocumentMediaDTO> media) {
        mediaService.untagMediaToDevice(username, vdmsid, media);
    }


}
