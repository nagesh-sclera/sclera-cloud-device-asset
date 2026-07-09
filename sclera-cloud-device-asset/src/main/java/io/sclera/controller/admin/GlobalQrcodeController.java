package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.GlobalQrcodeDTO;
import io.sclera.service.impl.GlobalQrcodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST endpoints for managing global QR codes: generate, list, scan/lookup,
 * export to PDF, and delete.
 *
 * Path-prefix convention: all admin controllers in this module use the class-level
 * {@code @RequestMapping("/api/v1/sclera-cloud-device-asset-service")} prefix.
 * The edge reference used no class-level prefix and put username/vdmsid as
 * path variables; here they are {@code @RequestParam} to match the cloud module
 * convention used by BuildingController, PropertyServiceController, etc.
 *
 * Delegates all business logic to {@link GlobalQrcodeService}.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Global QR Codes", description = "Generate, list, scan, export (PDF) and delete global QR codes.")
public class GlobalQrcodeController {

    private static final Logger log = LoggerFactory.getLogger(GlobalQrcodeController.class);

    @Autowired
    GlobalQrcodeService globalQrcodeService;

    /**
     * Lists global QR codes filtered by type, search key, pagination and an
     * optional JSON filter object (building/floor/device-type filters).
     *
     * @param username     owning user
     * @param vdmsid       owning VDMS id
     * @param qrcode_type  one of: all | untagged | device | location
     * @param searchkey    optional search string (defaults to "null")
     * @param pageno       1-based page number (default 1)
     * @param pagesize     page size (default 10)
     * @param filterObject JSON body carrying device_types / building_id / floor_id
     * @return matching QR code DTOs
     */
    @Operation(summary = "List global QR codes",
            description = "Returns global QR codes filtered by type, search key, pagination and an optional JSON filter object.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR codes returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/type/{qrcode_type}/getqrcodes")
    public Set<GlobalQrcodeDTO> getGlobalQrCode(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "QR code type: all | untagged | device | location") @PathVariable String qrcode_type,
            @RequestParam(defaultValue = "null") String searchkey,
            @RequestParam(defaultValue = "1") Integer pageno,
            @RequestParam(defaultValue = "10") Integer pagesize,
            @RequestBody JSONObject filterObject) {
        log.info("getGlobalQrCode username={} vdmsid={} qrcode_type={} pageno={} pagesize={}", username, vdmsid, qrcode_type, pageno, pagesize);
        return globalQrcodeService.getGlobalQrCode(username, vdmsid, qrcode_type, searchkey, pageno, pagesize, filterObject);
    }

    /**
     * Deletes the QR codes identified by the given ids, removes their stored
     * images, and refreshes device qrcode_count for any linked devices.
     *
     * @param username        owning user
     * @param vdmsid          owning VDMS id
     * @param globalQrcodeIds set of QR code ids to delete
     */
    @Operation(summary = "Delete global QR codes",
            description = "Deletes the QR codes identified by the given ids, removes stored images, and refreshes device qrcode counts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR codes deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/deleteqrcodes")
    public void deleteGlobalQrcode(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody Set<String> globalQrcodeIds) {
        log.info("deleteGlobalQrcode username={} vdmsid={} count={}", username, vdmsid, globalQrcodeIds == null ? 0 : globalQrcodeIds.size());
        globalQrcodeService.deleteGlobalQrcode(username, vdmsid, globalQrcodeIds);
    }

    /**
     * Exports QR codes as a PDF. If {@code qrcodes} is supplied only those ids
     * are exported; otherwise all codes matching {@code type} and the filter lists
     * are exported.
     *
     * @param response     HTTP response used to stream the PDF
     * @param username     owning user
     * @param vdmsid       owning VDMS id
     * @param qrcodes      optional explicit list of QR code ids to export
     * @param type         export type: all | device | location | untagged (default "all")
     * @param dockernames  optional docker-name filter (default "all")
     * @param device_types optional device-type filter (default "all")
     * @param building_ids optional building-id filter (default "all")
     * @param floor_ids    optional floor-id filter (default "all")
     * @param width        PDF cell width in inches (default 2)
     * @param height       PDF cell height in inches (default 2)
     */
    @Operation(summary = "Export QR codes as PDF",
            description = "Exports QR codes as a PDF attachment. Supply specific ids via 'qrcodes', or filter by type and scope lists.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF exported"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/exportqrcodes", produces = MediaType.APPLICATION_PDF_VALUE)
    public void exportGlobalQrCodes(
            HttpServletResponse response,
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestParam(required = false) List<String> qrcodes,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false, defaultValue = "all") List<String> dockernames,
            @RequestParam(required = false, defaultValue = "all") List<String> device_types,
            @RequestParam(required = false, defaultValue = "all") List<String> building_ids,
            @RequestParam(required = false, defaultValue = "all") List<String> floor_ids,
            @RequestParam(required = false, defaultValue = "2") Integer width,
            @RequestParam(required = false, defaultValue = "2") Integer height) {
        log.info("exportGlobalQrCodes username={} vdmsid={} type={}", username, vdmsid, type);
        globalQrcodeService.exportGlobalQrCodes(response, username, vdmsid, qrcodes, type, dockernames, device_types, building_ids, floor_ids, width, height);
    }

    /**
     * Creates or updates QR codes from the supplied set. A DTO without an id
     * triggers creation (new UUID-based id generated); a DTO with an existing id
     * updates the record.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param globalQrcodes set of QR code DTOs to upsert
     */
    @Operation(summary = "Upsert global QR codes",
            description = "Creates or updates global QR codes. DTOs without an id create new records; DTOs with an existing id update them.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR codes upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertglobalqrcode")
    public void upsertGlobalQrcode(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody Set<GlobalQrcodeDTO> globalQrcodes) {
        log.info("upsertGlobalQrcode username={} vdmsid={} count={}", username, vdmsid, globalQrcodes == null ? 0 : globalQrcodes.size());
        globalQrcodeService.upsertGlobalQrcode(username, vdmsid, globalQrcodes);
    }

    /**
     * Generates {@code count} new untagged (generic) QR codes and persists them.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @param count    number of QR codes to generate
     */
    @Operation(summary = "Generate global QR codes",
            description = "Generates the requested number of new untagged (generic) QR codes and persists them.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR codes generated"),
            @ApiResponse(responseCode = "400", description = "Invalid count value"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/count/{count}/createqrcode")
    public void createGlobalQrcode(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Number of QR codes to generate") @PathVariable Integer count) {
        log.info("createGlobalQrcode username={} vdmsid={} count={}", username, vdmsid, count);
        globalQrcodeService.createGlobalQrcode(username, vdmsid, count);
    }

    /**
     * Looks up QR code detail by id, location_id or device_id embedded in the
     * request body. Used by scanning clients to resolve which asset a scanned
     * code belongs to.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param qrcodeid  DTO carrying the scanned QR code id (and optional location_id / device_id)
     * @return list of matching QR code detail DTOs
     */
    @Operation(summary = "Get QR code detail (scan)",
            description = "Resolves a scanned QR code to its asset detail. Supply id / location_id / device_id in the request body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR code detail returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "QR code not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/getqrcodedetail")
    public List<GlobalQrcodeDTO> getQrcodeDetail(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody GlobalQrcodeDTO qrcodeid) {
        log.info("getQrcodeDetail username={} vdmsid={}", username, vdmsid);
        return globalQrcodeService.getQrcodeDetail(username, vdmsid, qrcodeid);
    }
}
