package io.sclera.it;

import io.sclera.Repository.DocumentRepository;
import io.sclera.dto.DocumentMediaDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",               executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/document-pilot.sql",     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-document-pilot.sql",  executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DocumentRepositoryIT extends PostgresJpaIT {

    @Autowired
    DocumentRepository documentRepository;

    @PersistenceContext
    EntityManager em;

    // ── deleteDocumentById ────────────────────────────────────────────────────

    @Test
    void deleteDocumentById_removesDocumentRow() {
        documentRepository.deleteDocumentById("doc2");
        em.flush();
        em.clear();
        assertThat(documentRepository.getDocumentLinkByDocumentId("doc2")).isNull();
    }

    // ── getDocumentLinkByDocumentId ───────────────────────────────────────────

    @Test
    void getDocumentLinkByDocumentId_returnsLinkForKnownDocument() {
        String link = documentRepository.getDocumentLinkByDocumentId("doc1");
        assertThat(link).isEqualTo("http://example.com/doc1");
    }

    @Test
    void getDocumentLinkByDocumentId_unknownDocument_returnsNull() {
        assertThat(documentRepository.getDocumentLinkByDocumentId("no-such")).isNull();
    }

    // ── getDocumentById ───────────────────────────────────────────────────────

    @Test
    void getDocumentById_returnsIdLinkAndEncryptedType() {
        DocumentMediaDTO dto = documentRepository.getDocumentById("doc1");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("doc1");
        assertThat(dto.getLink()).isEqualTo("http://example.com/doc1");
        assertThat(dto.getEncrypted_type()).isEqualTo(0);
    }

    @Test
    void getDocumentById_unknownDocument_returnsNull() {
        assertThat(documentRepository.getDocumentById("no-such")).isNull();
    }

    // ── updateEncryption ──────────────────────────────────────────────────────

    @Test
    void updateEncryption_changesEncryptedTypeField() {
        documentRepository.updateEncryption("doc1", 2);
        em.flush();
        em.clear();
        DocumentMediaDTO dto = documentRepository.getDocumentById("doc1");
        assertThat(dto.getEncrypted_type()).isEqualTo(2);
    }

    // ── getDocuments (no filter) ──────────────────────────────────────────────

    @Test
    void getDocuments_noFilter_returnsAllDocuments() {
        List<DocumentMediaDTO> results = documentRepository.getDocuments("null", PageRequest.of(0, 10));
        assertThat(results).hasSize(2);
        assertThat(results).extracting(DocumentMediaDTO::getId)
                .containsExactlyInAnyOrder("doc1", "doc2");
    }

    @Test
    void getDocuments_matchingFilter_returnsOnlyMatching() {
        // "Policy" matches doc1 (name="Policy A"), not doc2 (name="Spec B")
        List<DocumentMediaDTO> results = documentRepository.getDocuments("Policy", PageRequest.of(0, 10));
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo("doc1");
    }

    @Test
    void getDocuments_nullColumnRow_stillReturnedWhenNoFilter() {
        // doc2 has NULL description; COALESCE must not drop it from unfiltered results
        List<DocumentMediaDTO> results = documentRepository.getDocuments("null", PageRequest.of(0, 10));
        assertThat(results).extracting(DocumentMediaDTO::getId).contains("doc2");
    }

    @Test
    void getDocuments_pagination_firstPageReturnsSubset() {
        List<DocumentMediaDTO> page = documentRepository.getDocuments("null", PageRequest.of(0, 1));
        assertThat(page).hasSize(1);
    }

    // ── getDocumentsByDeviceId ────────────────────────────────────────────────

    @Test
    void getDocumentsByDeviceId_returnsTaggedDocuments() {
        Set<DocumentMediaDTO> results = documentRepository.getDocumentsByDeviceId("dev-d1");
        assertThat(results).hasSize(1);
        DocumentMediaDTO dto = results.iterator().next();
        assertThat(dto.getId()).isEqualTo("doc1");
        assertThat(dto.getDevice_id()).isEqualTo("dev-d1");
        assertThat(dto.getName()).isEqualTo("Policy A");
        assertThat(dto.getCreated_timestamp()).isEqualTo(BigInteger.valueOf(1000000L));
        assertThat(dto.getEncrypted_type()).isEqualTo(0);
    }

    @Test
    void getDocumentsByDeviceId_unknownDevice_returnsEmpty() {
        assertThat(documentRepository.getDocumentsByDeviceId("no-such-device")).isEmpty();
    }

    // ── getDocumentsByDeviceIdByPagination ────────────────────────────────────

    @Test
    void getDocumentsByDeviceIdByPagination_returnsTaggedDocuments() {
        List<DocumentMediaDTO> results =
                documentRepository.getDocumentsByDeviceIdByPagination("dev-d1", PageRequest.of(0, 10));
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo("doc1");
    }

    @Test
    void getDocumentsByDeviceIdByPagination_pagination_emptySecondPage() {
        List<DocumentMediaDTO> results =
                documentRepository.getDocumentsByDeviceIdByPagination("dev-d1", PageRequest.of(1, 10));
        assertThat(results).isEmpty();
    }
}
