package io.sclera.service;

import io.sclera.Repository.DocumentRepository;
import io.sclera.dto.DocumentMediaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean methods of DocumentService: pagination delegates, count,
 * tag-record deletion + device count refresh, untag, and device-id reassignment branch.
 * The PDF/network checkEncryptedType, upsertDocument and ChatBot-sync methods are deferred.
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock DocumentRepository documentRepository;
    @Mock DeviceService deviceService;

    @InjectMocks DocumentService service;

    @Test
    void getDocuments_computesOffsetAndDelegates() {
        Set<DocumentMediaDTO> docs = Set.of(mock(DocumentMediaDTO.class));
        // pageno=2, pagesize=10 -> offset 10
        when(documentRepository.getDocuments(10, 10, "key")).thenReturn(docs);
        assertThat(service.getDocuments("u", "v", 2, 10, "key")).isSameAs(docs);
    }

    @Test
    void getDocumentsByDeviceId_computesOffsetAndDelegates() {
        Set<DocumentMediaDTO> docs = Set.of(mock(DocumentMediaDTO.class));
        when(documentRepository.getDocumentsByDeviceIdByPagination("d1", 10, 10)).thenReturn(docs);
        assertThat(service.getDocumentsByDeviceId("u", "v", "d1", 2, 10)).isSameAs(docs);
    }

    @Test
    void getDocumentsCountByDeviceId_delegates() {
        when(documentRepository.getDocumentsCountByDeviceId("d1")).thenReturn(4);
        assertThat(service.getDocumentsCountByDeviceId("d1")).isEqualTo(4);
    }

    @Test
    void deleteTagRecordByDocumentId_deletesAndRefreshesEachDeviceCount() {
        when(documentRepository.getDocumentByDeviceId("doc1")).thenReturn(List.of("d1", "d2"));

        service.deleteTagRecordByDocumentId("doc1");

        verify(documentRepository).deleteTagRecordByDocumentId("doc1");
        verify(deviceService).updateDeviceDocumentsCountByDeviceId("d1");
        verify(deviceService).updateDeviceDocumentsCountByDeviceId("d2");
    }

    @Test
    void deleteDocument_deletesTagRecordsThenDocument() {
        when(documentRepository.getDocumentByDeviceId("doc1")).thenReturn(List.of());

        service.deleteDocument("u", "v", "doc1");

        verify(documentRepository).deleteDocumentById("doc1");
    }

    @Test
    void untagDocumentToDevice_untagsEachAndRefreshesCount() {
        DocumentMediaDTO doc = mock(DocumentMediaDTO.class);
        when(doc.getId()).thenReturn("doc1");
        when(doc.getDevice_id()).thenReturn("d1");

        service.untagDocumentToDevice("u", "v", Set.of(doc));

        verify(documentRepository).untagDocumentToDevice("doc1", "d1");
        verify(deviceService).updateDeviceDocumentsCountByDeviceId("d1");
    }

    @Test
    void updateDocumentDeviceId_refreshesNewDeviceOnly_whenExistingNotRetained() {
        service.updateDocumentDeviceId("d1", "old1", Set.of());

        verify(documentRepository).updateDocumentDeviceId("d1", "old1");
        verify(deviceService).updateDeviceDocumentsCountByDeviceId("d1");
        verify(deviceService, never()).updateDeviceDocumentsCountByDeviceId("old1");
    }

    @Test
    void updateDocumentDeviceId_alsoRefreshesExisting_whenRetained() {
        service.updateDocumentDeviceId("d1", "old1", Set.of("old1"));

        verify(deviceService).updateDeviceDocumentsCountByDeviceId("d1");
        verify(deviceService).updateDeviceDocumentsCountByDeviceId("old1");
    }
}
