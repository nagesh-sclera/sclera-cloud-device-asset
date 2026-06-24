package io.sclera.controller.admin;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import io.sclera.dto.DocumentMediaDTO;
import io.sclera.service.DocumentService;
import io.sclera.utils.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST endpoints for managing documents and their association with devices.
 * Delegates all persistence and business logic to {@link DocumentService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Documents", description = "Upsert, delete, fetch and tag documents and their association with devices for a VDMS.")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);


    @Autowired
    DocumentService documentService;


//	@RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertdocument")
//	public void upsertDocument(@PathVariable String username, @PathVariable String vdmsid, @RequestParam("documentDetail") String documentString, @RequestParam(value="documentFile",required = false) MultipartFile documentFile ) throws JsonMappingException, JsonProcessingException
//	{
//		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		DocumentMediaDTO document = mapper.readValue(documentString, DocumentMediaDTO.class);
//		documentService.upsertDocument(username , vdmsid, document , documentFile);
//	}


    /**
     * Creates or updates a document for the given user and VDMS.
     *
     * @param username            owning user
     * @param vdmsid              owning VDMS id
     * @param document            document payload to upsert
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Upsert a document",
            description = "Creates or updates a document for the given user and VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertdocument")
    public void upsertDocument(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody DocumentMediaDTO document, HttpServletRequest httpServletRequest) {
        log.info("upsertDocument username={} vdmsid={}", username, vdmsid);
        documentService.upsertDocument(username, vdmsid, document, httpServletRequest);
    }


    /**
     * Uploads a document file and attaches it to the given device/asset: stores the file, creates the
     * document record, tags it to the device, and refreshes the device's document count.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param deviceid      the device/asset to attach the document to
     * @param name          the document name
     * @param category      the document category (optional)
     * @param description   the document description (optional)
     * @param documentFile  the uploaded file (required, non-empty)
     */
    @RequestMapping(method = RequestMethod.POST, value = "/device/{deviceid}/uploaddocument")
    public void uploadDocument(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String deviceid,
                               @RequestParam String name, @RequestParam(required = false) String category,
                               @RequestParam(required = false) String description,
                               @RequestParam("documentFile") MultipartFile documentFile, HttpServletRequest httpServletRequest) {
        log.info("uploadDocument username={} vdmsid={} deviceid={}", username, vdmsid, deviceid);
        try {
            if (documentFile == null || documentFile.isEmpty()) {
                throw new IllegalArgumentException("documentFile is required and must not be empty");
            }
            documentService.uploadDocument(username, vdmsid, deviceid, name, category, description, documentFile, httpServletRequest);
        } catch (Exception e) {
            log.error("uploadDocument failed username={} vdmsid={} deviceid={}: {}", username, vdmsid, deviceid, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Deletes the document identified by the given id.
     *
     * @param username    owning user
     * @param vdmsid      owning VDMS id
     * @param documentid  document to delete
     */
    @Operation(summary = "Delete a document by id",
            description = "Deletes the document identified by the given id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document deleted"),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/documentid/{documentid}/deletedocument")
    public void deleteDocumentbyId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Document to delete") @PathVariable String documentid) {
        log.info("deleteDocumentbyId username={} vdmsid={} documentid={}", username, vdmsid, documentid);
        documentService.deleteDocument(username, vdmsid, documentid);
    }


//	@RequestMapping(method = RequestMethod.DELETE, value = "user/{username}/vdms/{vdmsid}/documentid/{documentid}/deletedocumentfile")
//	public void deleteDocumentFilebyId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String documentid)
//	{
//		 documentService.deleteDocumentFilebyId(username, vdmsid, documentid);
//	}


    /**
     * Returns a paginated, optionally filtered set of documents for the given user and VDMS.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param pageno     page number to return (default 1)
     * @param pagesize   number of documents per page (default 5)
     * @param searchkey  optional search filter (default "null")
     * @return the matching documents
     */
    @Operation(summary = "Get documents for a VDMS",
            description = "Returns a paginated, optionally filtered set of documents for the given user and VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getdocuments")
    public Page<DocumentMediaDTO> getDocuments(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of documents per page") @RequestParam(defaultValue = "5") Integer pagesize,
            @Parameter(description = "Optional search filter") @RequestParam(defaultValue = "null") String searchkey) {
        log.info("getDocuments username={} vdmsid={}", username, vdmsid);
        return PageUtils.toPage(documentService.getDocuments(username, vdmsid, pageno, pagesize, searchkey), pageno, pagesize);
    }


    /**
     * Returns a paginated set of documents tagged to the given device.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param deviceid  device whose documents are requested
     * @param pageno    page number to return (default 1)
     * @param pagesize  number of documents per page (default 5)
     * @return the documents tagged to the device
     */
    @Operation(summary = "Get documents by device id",
            description = "Returns a paginated set of documents tagged to the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/{deviceid}/getdocumentbydeviceid")
    public Page<DocumentMediaDTO> getDocumentsByDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device whose documents are requested") @PathVariable String deviceid,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of documents per page") @RequestParam(defaultValue = "5") Integer pagesize) {
        log.info("getDocumentsByDeviceId username={} vdmsid={} deviceid={}", username, vdmsid, deviceid);
        return PageUtils.toPage(documentService.getDocumentsByDeviceId(username, vdmsid, deviceid, pageno, pagesize), pageno, pagesize);
    }

    /**
     * Tags the given documents to one or more devices.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param share_method  how the documents are shared/tagged (default "add")
     * @param document      documents to tag
     */
    @Operation(summary = "Tag documents to devices",
            description = "Tags the given documents to one or more devices. The share_method controls how the documents are shared.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents tagged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/tagdocumenttodevice")
    public void tagDocumentToDevice(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "How the documents are shared/tagged") @RequestParam(defaultValue = "add") String share_method,
            @RequestBody Set<DocumentMediaDTO> document) {
        log.info("tagDocumentToDevice username={} vdmsid={} share_method={}", username, vdmsid, share_method);
        documentService.tagDocumentToDevice(username, vdmsid, share_method, document);
    }


    /**
     * Removes the device tagging for the given documents.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param document  documents to untag
     */
    @Operation(summary = "Untag documents from devices",
            description = "Removes the device tagging for the given documents.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents untagged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/untagdocumenttodevice")
    public void untagDocumentToDevice(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody Set<DocumentMediaDTO> document) {
        log.info("untagDocumentToDevice username={} vdmsid={}", username, vdmsid);
        documentService.untagDocumentToDevice(username, vdmsid, document);
    }


}
