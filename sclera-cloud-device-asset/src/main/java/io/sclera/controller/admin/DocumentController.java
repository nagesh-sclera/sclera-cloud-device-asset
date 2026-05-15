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
import io.sclera.service.DocumentService;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DocumentController {


    @Autowired
    DocumentService documentService;


//	@RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertdocument")
//	public void upsertDocument(@PathVariable String username, @PathVariable String vdmsid, @RequestParam("documentDetail") String documentString, @RequestParam(value="documentFile",required = false) MultipartFile documentFile ) throws JsonMappingException, JsonProcessingException
//	{
//		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		DocumentMediaDTO document = mapper.readValue(documentString, DocumentMediaDTO.class);
//		documentService.upsertDocument(username , vdmsid, document , documentFile);
//	}


    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertdocument")
    public void upsertDocument(@PathVariable String username, @PathVariable String vdmsid, @RequestBody DocumentMediaDTO document, HttpServletRequest httpServletRequest) {
        documentService.upsertDocument(username, vdmsid, document,httpServletRequest);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "user/{username}/vdms/{vdmsid}/documentid/{documentid}/deletedocument")
    public void deleteDocumentbyId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String documentid) {
        documentService.deleteDocument(username, vdmsid, documentid);
    }


//	@RequestMapping(method = RequestMethod.DELETE, value = "user/{username}/vdms/{vdmsid}/documentid/{documentid}/deletedocumentfile")
//	public void deleteDocumentFilebyId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String documentid)
//	{
//		 documentService.deleteDocumentFilebyId(username, vdmsid, documentid);
//	}


    @RequestMapping(method = RequestMethod.GET, value = "user/{username}/vdms/{vdmsid}/getdocuments")
    public Set<DocumentMediaDTO> getDocuments(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize, @RequestParam(defaultValue = "null") String searchkey) {
        return documentService.getDocuments(username, vdmsid, pageno, pagesize, searchkey);
    }


    @RequestMapping(method = RequestMethod.GET, value = "user/{username}/vdms/{vdmsid}/device/{deviceid}/getdocumentbydeviceid")
    public Set<DocumentMediaDTO> getDocumentsByDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String deviceid
            , @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize) {
        return documentService.getDocumentsByDeviceId(username, vdmsid, deviceid, pageno, pagesize);
    }

    @RequestMapping(method = RequestMethod.POST, value = "user/{username}/vdms/{vdmsid}/tagdocumenttodevice")
    public void tagDocumentToDevice(@PathVariable String username, @PathVariable String vdmsid,
                                    @RequestParam(defaultValue = "add") String share_method, @RequestBody Set<DocumentMediaDTO> document) {
        documentService.tagDocumentToDevice(username, vdmsid, share_method, document);
    }


    @RequestMapping(method = RequestMethod.POST, value = "user/{username}/vdms/{vdmsid}/untagdocumenttodevice")
    public void untagDocumentToDevice(@PathVariable String username, @PathVariable String vdmsid, @RequestBody Set<DocumentMediaDTO> document) {
        documentService.untagDocumentToDevice(username, vdmsid, document);
    }


}
