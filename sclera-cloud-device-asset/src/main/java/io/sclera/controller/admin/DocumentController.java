package io.sclera.controller.admin;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import io.sclera.service.DocumentService;
import io.sclera.utils.PageUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST endpoints for managing documents and their association with devices.
 * Delegates all persistence and business logic to {@link DocumentService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
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
    @RequestMapping(method = RequestMethod.POST, value = "/upsertdocument")
    public void upsertDocument(@RequestParam String username, @RequestParam String vdmsid, @RequestBody DocumentMediaDTO document, HttpServletRequest httpServletRequest) {
        log.info("upsertDocument username={} vdmsid={}", username, vdmsid);
        try {
            documentService.upsertDocument(username, vdmsid, document,httpServletRequest);
        } catch (Exception e) {
            log.error("upsertDocument failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/documentid/{documentid}/deletedocument")
    public void deleteDocumentbyId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String documentid) {
        log.info("deleteDocumentbyId username={} vdmsid={} documentid={}", username, vdmsid, documentid);
        try {
            documentService.deleteDocument(username, vdmsid, documentid);
        } catch (Exception e) {
            log.error("deleteDocumentbyId failed username={} vdmsid={} documentid={}: {}", username, vdmsid, documentid, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/getdocuments")
    public Page<DocumentMediaDTO> getDocuments(@RequestParam String username, @RequestParam String vdmsid, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize, @RequestParam(defaultValue = "null") String searchkey) {
        log.info("getDocuments username={} vdmsid={}", username, vdmsid);
        try {
            return PageUtils.toPage(documentService.getDocuments(username, vdmsid, pageno, pagesize, searchkey), pageno, pagesize);
        } catch (Exception e) {
            log.error("getDocuments failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/device/{deviceid}/getdocumentbydeviceid")
    public Page<DocumentMediaDTO> getDocumentsByDeviceId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String deviceid
            , @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize) {
        log.info("getDocumentsByDeviceId username={} vdmsid={} deviceid={}", username, vdmsid, deviceid);
        try {
            return PageUtils.toPage(documentService.getDocumentsByDeviceId(username, vdmsid, deviceid, pageno, pagesize), pageno, pagesize);
        } catch (Exception e) {
            log.error("getDocumentsByDeviceId failed username={} vdmsid={} deviceid={}: {}", username, vdmsid, deviceid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Tags the given documents to one or more devices.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param share_method  how the documents are shared/tagged (default "add")
     * @param document      documents to tag
     */
    @RequestMapping(method = RequestMethod.POST, value = "/tagdocumenttodevice")
    public void tagDocumentToDevice(@RequestParam String username, @RequestParam String vdmsid,
                                    @RequestParam(defaultValue = "add") String share_method, @RequestBody Set<DocumentMediaDTO> document) {
        log.info("tagDocumentToDevice username={} vdmsid={} share_method={}", username, vdmsid, share_method);
        try {
            documentService.tagDocumentToDevice(username, vdmsid, share_method, document);
        } catch (Exception e) {
            log.error("tagDocumentToDevice failed username={} vdmsid={} share_method={}: {}", username, vdmsid, share_method, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Removes the device tagging for the given documents.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param document  documents to untag
     */
    @RequestMapping(method = RequestMethod.POST, value = "/untagdocumenttodevice")
    public void untagDocumentToDevice(@RequestParam String username, @RequestParam String vdmsid, @RequestBody Set<DocumentMediaDTO> document) {
        log.info("untagDocumentToDevice username={} vdmsid={}", username, vdmsid);
        try {
            documentService.untagDocumentToDevice(username, vdmsid, document);
        } catch (Exception e) {
            log.error("untagDocumentToDevice failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
    }


}
