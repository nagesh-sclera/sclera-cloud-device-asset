package io.sclera.controller.admin;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class MediaController {

    private static final Logger log = LoggerFactory.getLogger(MediaController.class);

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
    @RequestMapping(method = RequestMethod.POST, value = "/upsertmedia")
    public String upsertDocument(@RequestParam String username, @RequestParam String vdmsid, @RequestBody DocumentMediaDTO media) {
        log.info("upsertDocument username={} vdmsid={}", username, vdmsid);
        try {


            return mediaService.upsertMedia(username, vdmsid, media);
        } catch (Exception e) {
            log.error("upsertDocument failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Deletes the media item identified by the given id.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param mediaid   media item to delete
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/mediaid/{mediaid}/deletemedia")
    public void deleteDocumentbyId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String mediaid) {
        log.info("deleteDocumentbyId username={} vdmsid={} mediaid={}", username, vdmsid, mediaid);
        try {
            mediaService.deleteMedia(username, vdmsid, mediaid);
        } catch (Exception e) {
            log.error("deleteDocumentbyId failed username={} vdmsid={} mediaid={}: {}", username, vdmsid, mediaid, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/getmedias")
    public Set<DocumentMediaDTO> getMedias(@RequestParam String username, @RequestParam String vdmsid, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize, @RequestParam(defaultValue = "null") String searchkey) {
        log.info("getMedias username={} vdmsid={}", username, vdmsid);
        try {
            return mediaService.getMedias(username, vdmsid, pageno, pagesize, searchkey);
        } catch (Exception e) {
            log.error("getMedias failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/device/{deviceid}/getmediabydeviceid")
    public Set<DocumentMediaDTO> getMediasByDeviceId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String deviceid, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize) {
        log.info("getMediasByDeviceId username={} vdmsid={} deviceid={}", username, vdmsid, deviceid);
        try {
            return mediaService.getMediasByDeviceId(username, vdmsid, deviceid, pageno, pagesize);
        } catch (Exception e) {
            log.error("getMediasByDeviceId failed username={} vdmsid={} deviceid={}: {}", username, vdmsid, deviceid, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Tags the given media items to one or more devices.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param share_method  how the media items are shared/tagged (default "add")
     * @param media         media items to tag
     */
    @RequestMapping(method = RequestMethod.POST, value = "/tagmediatodevice")
    public void tagMediaToDevice(@RequestParam String username, @RequestParam String vdmsid, @RequestParam(defaultValue = "add") String share_method, @RequestBody Set<DocumentMediaDTO> media) {
        log.info("tagMediaToDevice username={} vdmsid={} share_method={}", username, vdmsid, share_method);
        try {
            mediaService.tagMediaToDevice(username, vdmsid, share_method, media);
        } catch (Exception e) {
            log.error("tagMediaToDevice failed username={} vdmsid={} share_method={}: {}", username, vdmsid, share_method, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Removes the device tagging for the given media items.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param media     media items to untag
     */
    @RequestMapping(method = RequestMethod.POST, value = "/untagmediatodevice")
    public void untagMediaToDevice(@RequestParam String username, @RequestParam String vdmsid, @RequestBody Set<DocumentMediaDTO> media) {
        log.info("untagMediaToDevice username={} vdmsid={}", username, vdmsid);
        try {
            mediaService.untagMediaToDevice(username, vdmsid, media);
        } catch (Exception e) {
            log.error("untagMediaToDevice failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
    }


}
