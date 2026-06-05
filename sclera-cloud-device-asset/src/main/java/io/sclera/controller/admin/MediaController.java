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

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import io.sclera.dto.DocumentMediaDTO;
import io.sclera.service.MediaService;


/**
 * REST endpoints for managing media items and their association with devices.
 * Delegates all persistence and business logic to {@link MediaService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MediaController {

    @Autowired
    MediaService mediaService;

    /**
     * Creates or updates a media item for the given user and VDMS.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param media     media payload to upsert
     * @return identifier or status of the upserted media
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertmedia")
    public String upsertDocument(@PathVariable String username, @PathVariable String vdmsid, @RequestBody DocumentMediaDTO media) {


        return mediaService.upsertMedia(username, vdmsid, media);
    }


    /**
     * Deletes the media item identified by the given id.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param mediaid   media item to delete
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "user/{username}/vdms/{vdmsid}/mediaid/{mediaid}/deletemedia")
    public void deleteDocumentbyId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String mediaid) {
        mediaService.deleteMedia(username, vdmsid, mediaid);
    }


    /**
     * Returns a paginated, optionally filtered set of media items for the given user and VDMS.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param pageno     page number to return (default 1)
     * @param pagesize   number of media items per page (default 5)
     * @param searchkey  optional search filter (default "null")
     * @return the matching media items
     */
    @RequestMapping(method = RequestMethod.GET, value = "user/{username}/vdms/{vdmsid}/getmedias")
    public Set<DocumentMediaDTO> getMedias(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize, @RequestParam(defaultValue = "null") String searchkey) {
        return mediaService.getMedias(username, vdmsid, pageno, pagesize, searchkey);
    }

    /**
     * Returns a paginated set of media items tagged to the given device.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param deviceid  device whose media items are requested
     * @param pageno    page number to return (default 1)
     * @param pagesize  number of media items per page (default 5)
     * @return the media items tagged to the device
     */
    @RequestMapping(method = RequestMethod.GET, value = "user/{username}/vdms/{vdmsid}/device/{deviceid}/getmediabydeviceid")
    public Set<DocumentMediaDTO> getMediasByDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String deviceid, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize) {
        return mediaService.getMediasByDeviceId(username, vdmsid, deviceid, pageno, pagesize);
    }


    /**
     * Tags the given media items to one or more devices.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param share_method  how the media items are shared/tagged (default "add")
     * @param media         media items to tag
     */
    @RequestMapping(method = RequestMethod.POST, value = "user/{username}/vdms/{vdmsid}/tagmediatodevice")
    public void tagMediaToDevice(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(defaultValue = "add") String share_method, @RequestBody Set<DocumentMediaDTO> media) {
        mediaService.tagMediaToDevice(username, vdmsid, share_method, media);
    }


    /**
     * Removes the device tagging for the given media items.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param media     media items to untag
     */
    @RequestMapping(method = RequestMethod.POST, value = "user/{username}/vdms/{vdmsid}/untagmediatodevice")
    public void untagMediaToDevice(@PathVariable String username, @PathVariable String vdmsid, @RequestBody Set<DocumentMediaDTO> media) {
        mediaService.untagMediaToDevice(username, vdmsid, media);
    }


}
