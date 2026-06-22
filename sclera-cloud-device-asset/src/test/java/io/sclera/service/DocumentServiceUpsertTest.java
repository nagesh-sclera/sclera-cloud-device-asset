package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.Repository.DocumentRepository;
import io.sclera.client.APICallClient;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.DocumentMediaDTO;
import io.sclera.utils.AuthenticationUtils;
import io.sclera.utils.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for the DocumentService write paths the delegate-focused {@link DocumentServiceTest}
 * deferred: create/update upsert (with the PDF encryption probe short-circuiting on a non-URL
 * link), file upload, document-to-device tagging, and the ChatBot vendor/model push.
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceUpsertTest {

    @Mock DocumentRepository documentRepository;
    @Mock FileUtils fileUtils;
    @Mock DeviceService deviceService;
    @Mock APICallClient apiCallService;
    @Mock AuthenticationUtils authenticationUtils;
    @Mock UserActionLogService userActionLogService;

    @InjectMocks DocumentService service;

    private HttpServletRequest reqWithAuth() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("Authorization")).thenReturn("Bearer tok");
        return req;
    }

    /** A non-URL link makes checkEncryptedType throw-and-swallow, returning encryptedType 0 with no network. */
    private DocumentMediaDTO doc(String id) {
        DocumentMediaDTO d = new DocumentMediaDTO();
        if (id != null) d.setId(id);
        d.setName("Manual");
        d.setCategory("guide");
        d.setDescription("desc");
        d.setLink("not-a-url");
        return d;
    }

    @Test
    void upsertDocument_newDocument_generatesIdAndInsertsWithTimestamp() {
        service.upsertDocument("u", "v1", doc(null), reqWithAuth());

        verify(documentRepository).upsertDocument(anyString(), eq("Manual"), eq("guide"),
                eq("desc"), eq("not-a-url"), eq("u"), any(), eq(0));
    }

    @Test
    void upsertDocument_existingDocument_updatesWithNullTimestamp() {
        service.upsertDocument("u", "v1", doc("doc1"), reqWithAuth());

        verify(documentRepository).upsertDocument(eq("doc1"), eq("Manual"), eq("guide"),
                eq("desc"), eq("not-a-url"), eq("u"), isNull(), eq(0));
    }

    @Test
    void uploadDocument_storesFileTagsAndLogs() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("report.pdf");
        when(fileUtils.addDocumentToServer(anyString(), eq(file))).thenReturn("not-a-url");

        service.uploadDocument("u", "v1", "d1", "report", "cat", "desc", file, reqWithAuth());

        verify(documentRepository).upsertDocument(anyString(), eq("report"), eq("cat"),
                eq("desc"), eq("not-a-url"), eq("u"), any(), eq(0));
        verify(documentRepository).tagDocumentToDevice(anyString(), eq("d1"));
        verify(deviceService).updateDeviceDocumentsCountByDeviceId("d1");
        verify(userActionLogService).addUserAction(eq("u"), eq("document"), eq("TAG"),
                anyString(), eq("success"), eq("document"), eq("d1"));
    }

    @Test
    void tagDocumentToDevice_tagsAndRefreshesCount() {
        DocumentMediaDTO toTag = mock(DocumentMediaDTO.class);
        when(toTag.getId()).thenReturn("doc1");
        when(toTag.getDevice_id()).thenReturn("d1");
        DocumentMediaDTO stored = mock(DocumentMediaDTO.class);
        when(stored.getEncrypted_type()).thenReturn(0); // already known -> skip encryption probe
        when(stored.getLink()).thenReturn("http://l/doc1.pdf");
        when(documentRepository.getDocumentById("doc1")).thenReturn(stored);
        // chatbot push: no devices -> no outbound call
        when(deviceService.getDeviceDetailsByIdList(any())).thenReturn(Set.of());

        service.tagDocumentToDevice("u", "v1", null, Set.of(toTag));

        verify(documentRepository).tagDocumentToDevice("doc1", "d1");
        verify(deviceService).updateDeviceDocumentsCountByDeviceId("d1");
    }

    @Test
    void updateChatBotOnTagDocument_pushesVendorModelGroupedLinks() throws Exception {
        DocumentMediaDTO d = mock(DocumentMediaDTO.class);
        when(d.getId()).thenReturn("doc1");
        when(d.getDevice_id()).thenReturn("d1");
        DocumentMediaDTO stored = mock(DocumentMediaDTO.class);
        when(stored.getEncrypted_type()).thenReturn(0);
        when(stored.getLink()).thenReturn("http://l/doc1.pdf");
        when(documentRepository.getDocumentById("doc1")).thenReturn(stored);
        DeviceDTO device = mock(DeviceDTO.class);
        when(device.getVendor()).thenReturn("Acme");
        when(device.getModel()).thenReturn("X1");
        when(deviceService.getDeviceDetailsByIdList(any())).thenReturn(Set.of(device));

        service.updateChatBotOnTagDocument("u", "v1", Set.of(d));

        verify(apiCallService).updateChatbotDeviceData(any(JSONArray.class));
    }
}
