package io.sclera.service;

import java.util.List;
import java.util.Set;

import org.json.JSONException;

import io.sclera.dto.DocumentMediaDTO;

import jakarta.servlet.http.HttpServletRequest;

/** Service contract for {@link io.sclera.service.DocumentService}. */
public interface DocumentService {

    void upsertDocument(String username, String vdmsid, DocumentMediaDTO document, HttpServletRequest httpServletRequest);

    void deleteDocument(String username, String vdmsid, String documentid);

    Set<DocumentMediaDTO> getDocuments(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey);

    Set<DocumentMediaDTO> getDocumentsByDeviceId(String username, String vdmsid, String deviceid, Integer pageno, Integer pagesize);

    void tagDocumentToDevice(String username, String vdmsid, String share_method, Set<DocumentMediaDTO> documents);

    void updateChatBotOnTagDocument(String username, String vdmsid, Set<DocumentMediaDTO> documents) throws JSONException;

    void untagDocumentToDevice(String username, String vdmsid, Set<DocumentMediaDTO> documents);

    void deleteTagRecordByDocumentId(String document_id);

    Integer getDocumentsCountByDeviceId(String device_id);

    void updateDocumentDeviceId(String device_id, String existing_device_id, Set<String> retainDevices);
    void uploadDocument(String username, String vdmsid, String deviceid, String name, String category, String description, org.springframework.web.multipart.MultipartFile documentFile, jakarta.servlet.http.HttpServletRequest httpServletRequest);
}
