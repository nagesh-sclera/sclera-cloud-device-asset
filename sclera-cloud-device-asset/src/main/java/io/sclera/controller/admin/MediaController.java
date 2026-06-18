package io.sclera.controller.admin;

import java.util.Set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import io.sclera.dto.DocumentMediaDTO;
import io.sclera.service.MediaService;
import io.sclera.utils.PageUtils;
import org.springframework.data.domain.Page;


/**
 * REST endpoints for managing media items and their association with devices.
 * Delegates all persistence and business logic to {@link MediaService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Media", description = "Upsert, read, delete and tag media items against devices for a VDMS.")
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
    @Operation(summary = "Upsert a media item",
            description = "Creates or updates a media item for the given user and VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertmedia")
    public String upsertDocument(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody DocumentMediaDTO media) {
        log.info("upsertDocument username={} vdmsid={}", username, vdmsid);
        return mediaService.upsertMedia(username, vdmsid, media);
    }

    /**
     * Deletes the media item identified by the given id.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param mediaid   media item to delete
     */
    @Operation(summary = "Delete a media item by id",
            description = "Deletes the media item identified by the given id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media deleted"),
            @ApiResponse(responseCode = "404", description = "Media not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/mediaid/{mediaid}/deletemedia")
    public void deleteDocumentbyId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Media item to delete") @PathVariable String mediaid) {
        log.info("deleteDocumentbyId username={} vdmsid={} mediaid={}", username, vdmsid, mediaid);
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
    @Operation(summary = "Get media items",
            description = "Returns a paginated, optionally filtered set of media items for the given user and VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media items returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getmedias")
    public Page<DocumentMediaDTO> getMedias(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of media items per page") @RequestParam(defaultValue = "5") Integer pagesize,
            @Parameter(description = "Optional search filter") @RequestParam(defaultValue = "null") String searchkey) {
        log.info("getMedias username={} vdmsid={}", username, vdmsid);
        return PageUtils.toPage(mediaService.getMedias(username, vdmsid, pageno, pagesize, searchkey), pageno, pagesize);
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
    @Operation(summary = "Get media items for a device",
            description = "Returns a paginated set of media items tagged to the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media items returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/{deviceid}/getmediabydeviceid")
    public Page<DocumentMediaDTO> getMediasByDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device whose media items are requested") @PathVariable String deviceid,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of media items per page") @RequestParam(defaultValue = "5") Integer pagesize) {
        log.info("getMediasByDeviceId username={} vdmsid={} deviceid={}", username, vdmsid, deviceid);
        return PageUtils.toPage(mediaService.getMediasByDeviceId(username, vdmsid, deviceid, pageno, pagesize), pageno, pagesize);
    }

    /**
     * Tags the given media items to one or more devices.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param share_method  how the media items are shared/tagged (default "add")
     * @param media         media items to tag
     */
    @Operation(summary = "Tag media to devices",
            description = "Tags the given media items to one or more devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media tagged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/tagmediatodevice")
    public void tagMediaToDevice(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "How the media items are shared/tagged") @RequestParam(defaultValue = "add") String share_method,
            @RequestBody Set<DocumentMediaDTO> media) {
        log.info("tagMediaToDevice username={} vdmsid={} share_method={}", username, vdmsid, share_method);
        mediaService.tagMediaToDevice(username, vdmsid, share_method, media);
    }

    /**
     * Removes the device tagging for the given media items.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param media     media items to untag
     */
    @Operation(summary = "Untag media from devices",
            description = "Removes the device tagging for the given media items.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media untagged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/untagmediatodevice")
    public void untagMediaToDevice(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody Set<DocumentMediaDTO> media) {
        log.info("untagMediaToDevice username={} vdmsid={}", username, vdmsid);
        mediaService.untagMediaToDevice(username, vdmsid, media);
    }


}
