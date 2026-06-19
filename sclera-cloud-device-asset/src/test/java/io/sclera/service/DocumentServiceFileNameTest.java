package io.sclera.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Docker-free unit test for {@link DocumentService#getFileNameFromLink(String)} — the helper that
 * derives the stored file name from a document link URL so {@code deleteDocument} can remove the file.
 */
class DocumentServiceFileNameTest {

    @Test
    void getFileNameFromLink_extractsLastSegmentWithExtension() {
        assertThat(DocumentService.getFileNameFromLink("http://localhost:8085/images/document/abc-123.pdf"))
                .isEqualTo("abc-123.pdf");
    }

    @Test
    void getFileNameFromLink_extractsLastSegmentWithoutExtension() {
        assertThat(DocumentService.getFileNameFromLink("http://localhost:8085/images/document/abc-123"))
                .isEqualTo("abc-123");
    }

    @Test
    void getFileNameFromLink_returnsWholeStringWhenNoSlash() {
        assertThat(DocumentService.getFileNameFromLink("abc-123.pdf")).isEqualTo("abc-123.pdf");
    }
}
