package io.sclera.service;
import io.sclera.client.APICallClient;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.DeviceDTO;
import io.sclera.utils.AuthenticationUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.DocumentRepository;
import io.sclera.dto.DocumentMediaDTO;
import io.sclera.interfaces.DocumentServiceInterface;
import io.sclera.utils.FileUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Manages document media: persistence, device tagging, PDF encryption detection, and ChatBot synchronization.
 * <p>
 * Collaborates with {@link DocumentRepository} for document and tag persistence,
 * {@link DeviceService} for device lookups and document-count maintenance,
 * {@link APICallClient} for pushing tagged document data to the ChatBot,
 * {@link AuthenticationUtils} for service access tokens, and {@link FileUtils} for document file storage.
 */
@Service
public class DocumentService implements DocumentServiceInterface {
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    FileUtils fileUtils;

    @Autowired
    DeviceService deviceService;

    @Autowired
    APICallClient apiCallService;

    @Autowired
    AuthenticationUtils authenticationUtils;

    @Autowired
    UserActionLogService userActionLogService;

//	public void upsertDocument(String username, String vdmsid, DocumentMediaDTO document, MultipartFile documentFile) {
//		
//		
//		if(document.getId() == null)
//		{
//			String id = Generators.timeBasedGenerator().generate().toString();
//			document.setId(id);
//			BigInteger createdTimestamp = BigInteger.valueOf(System.currentTimeMillis());
//			
//			String link = 	fileUtils.addDocumentToServer(id + ".pdf", documentFile);
//			document.setLink(link);
//			
//			documentRepository.upsertDocument(document.getId(), document.getName(), document.getCategory(), 
//					document.getDescription(), document.getLink(), username , createdTimestamp);
//			
//		}else
//		{
//			if(documentFile != null)
//			{
//				fileUtils.removeDocumentFromServer(document.getId()+".pdf");
//				String link = 	fileUtils.addDocumentToServer(document.getId()+ ".pdf", documentFile);
//				document.setLink(link);
//			}
//			documentRepository.upsertDocument(document.getId(), document.getName(), document.getCategory(), 
//					document.getDescription(), document.getLink(), username , null);
//		}
//		
//	}

    /**
     * Creates or updates a document, deriving its PDF encryption type from the linked file.
     *
     * @param username the acting user
     * @param vdmsid the VDMS identifier scoping the request
     * @param document the document to persist; receives a generated id when new
     * @param httpServletRequest the request, used to read the {@code Authorization} header for fetching the document file
     */
    public void upsertDocument(String username, String vdmsid, DocumentMediaDTO document, HttpServletRequest httpServletRequest) {

        String token = httpServletRequest.getHeader("Authorization");
        Integer encryptedType = checkEncryptedType(document,token);
        if (document.getId() == null) {
            String id = Generators.timeBasedGenerator().generate().toString();
            document.setId(id);
            BigInteger createdTimestamp = BigInteger.valueOf(System.currentTimeMillis());
            documentRepository.upsertDocument(document.getId(), document.getName(), document.getCategory(),
                    document.getDescription(), document.getLink(), username, createdTimestamp,encryptedType);

        } else {
            documentRepository.upsertDocument(document.getId(), document.getName(), document.getCategory(),
                    document.getDescription(), document.getLink(), username, null,encryptedType);
        }

    }

    private Integer checkEncryptedType(DocumentMediaDTO document, String token) {

        Integer encryptedType = 0;
        /*
         * encryptedType = 0 - No password and No permission restriction
         * encryptedType = 1 - No password but has permission restriction
         * encryptedType = 2 - Has password restriction
         */
        try{
            URL url = new URL(document.getLink());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", token);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStream input = connection.getInputStream()) {
                    PDDocument pdfDocument = null;
                    try {
                        // Attempts to load the file without a password; if it fails, it means the file is password-protected.
                        pdfDocument = Loader.loadPDF(input.readAllBytes());
                        AccessPermission permissions = pdfDocument.getCurrentAccessPermission();
                        boolean hasRestrictions =
                                !permissions.canAssembleDocument() ||
                                        !permissions.canPrint() ||
                                        !permissions.canModify() ||
                                        !permissions.canExtractContent() ||
                                        !permissions.canModifyAnnotations() ||
                                        !permissions.canFillInForm() ||
                                        !permissions.canExtractForAccessibility();
                        if (pdfDocument.isEncrypted() || hasRestrictions) {
                            //no password only has permission restriction
                            encryptedType = 1;
                        }
                    } catch (Exception e) {
                        //Has password restriction
                        encryptedType = 2;
                    } finally {
                        if (pdfDocument != null) {
                            pdfDocument.close();
                        }
                    }
                }
            } else {
                log.error("Failed to fetch file! HTTP Code: " + responseCode);
            }
        }catch (Exception e) {
            log.error(" Error: " + e.getMessage());
        }
        return encryptedType;


    }

    /**
     * Uploads a document file for an asset: stores the file, creates the document record, tags it to the
     * device, and refreshes the device's document count. The stored file's public URL becomes the
     * document's {@code link}; the PDF encryption type is derived from that link as in {@link #upsertDocument}.
     *
     * @param username the acting user
     * @param vdmsid the VDMS identifier scoping the request
     * @param deviceid the device/asset to attach the document to
     * @param name the document name
     * @param category the document category (may be null)
     * @param description the document description (may be null)
     * @param documentFile the uploaded file (required, non-empty)
     * @param httpServletRequest the request, used to read the {@code Authorization} header for the encryption check
     */
    public void uploadDocument(String username, String vdmsid, String deviceid, String name, String category,
                               String description, MultipartFile documentFile, HttpServletRequest httpServletRequest) {

        String id = Generators.timeBasedGenerator().generate().toString();
        BigInteger createdTimestamp = BigInteger.valueOf(System.currentTimeMillis());

        String extension = getFileExtension(documentFile.getOriginalFilename());
        String fileName = extension.isEmpty() ? id : id + "." + extension;

        String link;
        try {
            link = fileUtils.addDocumentToServer(fileName, documentFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store document file for device " + deviceid, e);
        }

        DocumentMediaDTO document = new DocumentMediaDTO();
        document.setId(id);
        document.setLink(link);
        Integer encryptedType = checkEncryptedType(document, httpServletRequest.getHeader("Authorization"));

        documentRepository.upsertDocument(id, name, category, description, link, username, createdTimestamp, encryptedType);
        documentRepository.tagDocumentToDevice(id, deviceid);
        deviceService.updateDeviceDocumentsCountByDeviceId(deviceid);
        userActionLogService.addUserAction(username, "document", "TAG",
                "Document '" + name + "' tagged to device " + deviceid, "success", "document", deviceid);
    }

    /** Returns the file extension (without the dot) of a filename, or "" if there is none. */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 && dot < filename.length() - 1) ? filename.substring(dot + 1) : "";
    }

    /** Extracts the stored file name (the segment after the last {@code /}) from a document link URL. */
    static String getFileNameFromLink(String link) {
        return link.substring(link.lastIndexOf('/') + 1);
    }

    /**
     * Removes a document along with its stored file and all of its device tag records.
     *
     * @param username the acting user
     * @param vdmsid the VDMS identifier scoping the request
     * @param documentid the id of the document to delete
     */
    public void deleteDocument(String username, String vdmsid, String documentid) {
        java.util.List<String> taggedDeviceIds = documentRepository.getDocumentByDeviceId(documentid);
        String link = documentRepository.getDocumentLinkByDocumentId(documentid);
        if (link != null && !link.isBlank()) {
            fileUtils.removeDocumentFromServer(getFileNameFromLink(link));
        }
        deleteTagRecordByDocumentId(documentid);
        documentRepository.deleteDocumentById(documentid);
        if (taggedDeviceIds != null) {
            for (String deviceId : taggedDeviceIds) {
                userActionLogService.addUserAction(username, "document", "DELETE",
                        "Document " + documentid + " deleted from device " + deviceId, "success", "document", deviceId);
            }
        }
    }

//	public void deleteDocumentFilebyId(String username, String vdmsid, String documentid) {
//
//		fileUtils.removeDocumentFromServer(documentid + ".pdf");
//
//	}

    /**
     * Returns a paginated, search-filtered page of documents.
     *
     * @param username the acting user
     * @param vdmsid the VDMS identifier scoping the request
     * @param pageno the 1-based page number
     * @param pagesize the number of documents per page
     * @param searchkey the search filter applied to documents
     * @return the matching documents for the requested page
     */
    public Set<DocumentMediaDTO> getDocuments(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey) {
        // TODO Auto-generated method stub
        return new java.util.HashSet<>(documentRepository.getDocuments(searchkey, PageRequest.of(pageno - 1, pagesize)));
    }

    /**
     * Returns a paginated page of documents tagged to a specific device.
     *
     * @param username the acting user
     * @param vdmsid the VDMS identifier scoping the request
     * @param deviceid the id of the device whose documents are requested
     * @param pageno the 1-based page number
     * @param pagesize the number of documents per page
     * @return the documents tagged to the device for the requested page
     */
    public Set<DocumentMediaDTO> getDocumentsByDeviceId(String username, String vdmsid, String deviceid, Integer pageno, Integer pagesize) {
        // TODO Auto-generated method stub
        return new java.util.HashSet<>(documentRepository.getDocumentsByDeviceIdByPagination(deviceid, PageRequest.of(pageno - 1, pagesize)));
    }

    /**
     * Tags documents to devices, lazily computing missing encryption types and refreshing device document counts and the ChatBot.
     *
     * @param username the acting user
     * @param vdmsid the VDMS identifier scoping the request
     * @param share_method when {@code "replace"}, existing tags on each affected device are removed before tagging
     * @param documents the document-to-device tagging pairs to apply
     */
    public void tagDocumentToDevice(String username, String vdmsid, String share_method, Set<DocumentMediaDTO> documents) {

        if (share_method != null && share_method.equals("replace")) {
            Set<String> device_ids = new HashSet<>();
            for (DocumentMediaDTO document : documents) {
                device_ids.add(document.getDevice_id());
            }
            for (String device_id : device_ids) {
                this.untagDocumentsByDeviceId(username, vdmsid, device_id);
            }
        }

        for (DocumentMediaDTO document : documents) {
            try {
                DocumentMediaDTO documentMediaDTO = documentRepository.getDocumentById(document.getId());
                if (documentMediaDTO.getEncrypted_type() == null) {
                    log.info("Checking encryption for document ID: {}", document.getId());
                    String token = "Bearer " + authenticationUtils.getAccess_token();
                    Integer encryptedType = checkEncryptedType(documentMediaDTO,token);
                    document.setEncrypted_type(encryptedType);
                    documentMediaDTO.setEncrypted_type(encryptedType);
                    documentRepository.updateEncryption(document.getId(), encryptedType);
                    log.info("Updated encryption type for document ID: {}: {}", document.getId(), encryptedType);
                }
                log.info("Tagging document ID: {} to device ID: {}", document.getId(), document.getDevice_id());
                documentRepository.tagDocumentToDevice(document.getId(), document.getDevice_id());
                deviceService.updateDeviceDocumentsCountByDeviceId(document.getDevice_id());
                String docLabel = document.getName() != null ? document.getName() : document.getId();
                userActionLogService.addUserAction(username, "document", "TAG",
                        "Document '" + docLabel + "' tagged to device " + document.getDevice_id(),
                        "success", "document", document.getDevice_id());
            } catch (Exception e) {
                log.error("Error tagging document to device. Document ID: {}, Device ID: {}, Error: {}", document.getId(), document.getDevice_id(), e.getMessage());
            }
        }

        try {
            this.updateChatBotOnTagDocument(username,vdmsid,documents);
        } catch (Exception e) {
            log.error("Failed to update ChatBot on tag document for user: {}, VDMSID: {}, Error: {}", username, vdmsid, e.getMessage());
        }

    }

    /**
     * Pushes unencrypted document links, grouped by device vendor and model, to the ChatBot.
     *
     * @param username the acting user
     * @param vdmsid the VDMS identifier scoping the request
     * @param documents the tagged documents whose links and devices drive the ChatBot update
     * @throws JSONException if building the ChatBot request payload fails
     */
    public void updateChatBotOnTagDocument(String username, String vdmsid, Set<DocumentMediaDTO> documents) throws JSONException {

        if (documents.isEmpty()) {
            log.info("Documents set is empty for user: {}, VDMSID: {}", username, vdmsid);
        }

        Set<String> deviceIds = documents.stream()
                .map(DocumentMediaDTO::getDevice_id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        log.info("ids: {}", deviceIds);
        Set<DeviceDTO> devices = deviceService.getDeviceDetailsByIdList(deviceIds);
        log.info("devices size: {}" , devices);
        Set<String> combinedStrings = new HashSet<>();
        JSONArray bodyArray = new JSONArray();

        Set<String> linkFinal = new HashSet<>();
        for (DocumentMediaDTO document : documents) {
            DocumentMediaDTO documentMediaDTO = documentRepository.getDocumentById(document.getId());
            if(documentMediaDTO.getEncrypted_type() != null && documentMediaDTO.getEncrypted_type() == 0){
                linkFinal.add(documentMediaDTO.getLink());
            }
        }
        log.info("links size: {}, links: {}" , linkFinal.size(), linkFinal);
        for(DeviceDTO device: devices){
            String vendor_model = null;
            if(device.getVendor() != null && !device.getVendor().isBlank()
                    && device.getModel() != null && !device.getModel().isBlank()){

                vendor_model = device.getVendor().toLowerCase() + "|" + device.getModel().toLowerCase();

                if(!combinedStrings.contains(vendor_model)){
                    combinedStrings.add(vendor_model);
                    com.alibaba.fastjson.JSONObject bodyObject = new JSONObject();
                    bodyObject.put("manufacturer", device.getVendor());
                    bodyObject.put("model", device.getModel());
                    bodyObject.put("s3_urls", linkFinal);
                    bodyArray.add(bodyObject);
                }

            }
        }

        log.info("ChatBot update body:\n {}", bodyArray);

        if(!bodyArray.isEmpty()) {
            try {
                apiCallService.updateChatbotDeviceData(bodyArray);
                log.info("Successfully updated ChatBot for user: {}, VDMSID: {}", username, vdmsid);

            } catch (Exception e) {
                log.error("Failed to update ChatBot for user: {}, VDMSID: {}, Error: {}", username, vdmsid, e.getMessage());
            }
        }
    }



    private void untagDocumentsByDeviceId(String username, String vdmsid, String device_id) {
        Set<DocumentMediaDTO> delete_documents = documentRepository.getDocumentsByDeviceId(device_id);
        if (delete_documents != null) {
            this.untagDocumentToDevice(username, vdmsid, delete_documents);
        }
    }

    /**
     * Removes document-to-device tags and refreshes each affected device's document count.
     *
     * @param username the acting user
     * @param vdmsid the VDMS identifier scoping the request
     * @param documents the document-to-device tagging pairs to remove
     */
    public void untagDocumentToDevice(String username, String vdmsid, Set<DocumentMediaDTO> documents) {
        for (DocumentMediaDTO document : documents) {
            try {
                documentRepository.untagDocumentToDevice(document.getId(), document.getDevice_id());
                deviceService.updateDeviceDocumentsCountByDeviceId(document.getDevice_id());
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

    }

    /**
     * Deletes all device tag records for a document and refreshes the document counts of the previously tagged devices.
     *
     * @param document_id the id of the document whose tag records are removed
     */
    public void deleteTagRecordByDocumentId(String document_id) {
        List<String> device_ids = documentRepository.getDocumentByDeviceId(document_id);
        documentRepository.deleteTagRecordByDocumentId(document_id);

        for (String device_id : device_ids) {
            deviceService.updateDeviceDocumentsCountByDeviceId(device_id);
        }
    }

    /**
     * Returns the number of documents tagged to a device.
     *
     * @param device_id the id of the device
     * @return the count of documents tagged to the device
     */
    public Integer getDocumentsCountByDeviceId(String device_id) {
        return documentRepository.getDocumentsCountByDeviceId(device_id);
    }


    /**
     * Reassigns a document's device tags from one device to another and refreshes the affected device document counts.
     *
     * @param device_id the new device id to assign
     * @param existing_device_id the current device id being replaced
     * @param retainDevices device ids whose counts should also be refreshed when they include the existing device
     */
    public void updateDocumentDeviceId(String device_id, String existing_device_id, Set<String> retainDevices) {
        documentRepository.updateDocumentDeviceId(device_id, existing_device_id);
        deviceService.updateDeviceDocumentsCountByDeviceId(device_id);
        if (!retainDevices.isEmpty() && retainDevices.contains(existing_device_id)) {
            deviceService.updateDeviceDocumentsCountByDeviceId(existing_device_id);
        }
    }
}
