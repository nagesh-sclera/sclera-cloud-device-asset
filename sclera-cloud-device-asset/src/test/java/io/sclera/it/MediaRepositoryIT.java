package io.sclera.it;

import io.sclera.Repository.MediaRepository;
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

@Sql(scripts = "/schema-pg.sql",            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/media-pilot.sql",     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-media-pilot.sql",  executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class MediaRepositoryIT extends PostgresJpaIT {

    @Autowired
    MediaRepository mediaRepository;

    @PersistenceContext
    EntityManager em;

    // ── getExtensionById ─────────────────────────────────────────────────────

    @Test
    void getExtensionById_returnsExtensionForKnownMedia() {
        String ext = mediaRepository.getExtensionById("med1");
        assertThat(ext).isEqualTo("pdf");
    }

    @Test
    void getExtensionById_unknownMedia_returnsNull() {
        assertThat(mediaRepository.getExtensionById("no-such-id")).isNull();
    }

    // ── deleteMediaById ───────────────────────────────────────────────────────

    @Test
    void deleteMediaById_removesMediaRow() {
        mediaRepository.deleteMediaById("med2");
        em.flush();
        em.clear();
        String ext = mediaRepository.getExtensionById("med2");
        assertThat(ext).isNull();
    }

    // ── getMedias (no filter) ─────────────────────────────────────────────────

    @Test
    void getMedias_noFilter_returnsAllMedia() {
        List<DocumentMediaDTO> results = mediaRepository.getMedias("null", PageRequest.of(0, 10));
        assertThat(results).hasSize(2);
        assertThat(results).extracting(DocumentMediaDTO::getId)
                .containsExactlyInAnyOrder("med1", "med2");
    }

    @Test
    void getMedias_matchingFilter_returnsOnlyMatching() {
        // "Manual" matches med1 (name="Manual A"), not med2 (name="Image B")
        List<DocumentMediaDTO> results = mediaRepository.getMedias("Manual", PageRequest.of(0, 10));
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo("med1");
    }

    @Test
    void getMedias_nullColumnRow_stillReturnedWhenNoFilter() {
        // med2 has a NULL description; COALESCE must not drop it from unfiltered results
        List<DocumentMediaDTO> results = mediaRepository.getMedias("null", PageRequest.of(0, 10));
        assertThat(results).extracting(DocumentMediaDTO::getId).contains("med2");
    }

    @Test
    void getMedias_pagination_firstPageReturnsSubset() {
        List<DocumentMediaDTO> page = mediaRepository.getMedias("null", PageRequest.of(0, 1));
        assertThat(page).hasSize(1);
    }

    // ── getMediasByDeviceId ───────────────────────────────────────────────────

    @Test
    void getMediasByDeviceId_returnsTaggedMedia() {
        Set<DocumentMediaDTO> results = mediaRepository.getMediasByDeviceId("dev-m1");
        assertThat(results).hasSize(1);
        DocumentMediaDTO dto = results.iterator().next();
        assertThat(dto.getId()).isEqualTo("med1");
        assertThat(dto.getDevice_id()).isEqualTo("dev-m1");
        assertThat(dto.getName()).isEqualTo("Manual A");
        assertThat(dto.getCreated_timestamp()).isEqualTo(BigInteger.valueOf(1000000L));
    }

    @Test
    void getMediasByDeviceId_unknownDevice_returnsEmpty() {
        Set<DocumentMediaDTO> results = mediaRepository.getMediasByDeviceId("no-such-device");
        assertThat(results).isEmpty();
    }

    // ── getMediasByDeviceIdByPagination ───────────────────────────────────────

    @Test
    void getMediasByDeviceIdByPagination_returnsTaggedMedia() {
        List<DocumentMediaDTO> results =
                mediaRepository.getMediasByDeviceIdByPagination("dev-m1", PageRequest.of(0, 10));
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo("med1");
    }

    @Test
    void getMediasByDeviceIdByPagination_pagination_emptySecondPage() {
        List<DocumentMediaDTO> results =
                mediaRepository.getMediasByDeviceIdByPagination("dev-m1", PageRequest.of(1, 10));
        assertThat(results).isEmpty();
    }
}
