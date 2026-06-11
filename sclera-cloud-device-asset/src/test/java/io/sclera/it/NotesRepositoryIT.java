package io.sclera.it;

import io.sclera.Repository.NotesRepository;
import io.sclera.dto.Product_NotesDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link NotesRepository} after conversion from native SQL to JPQL.
 *
 * Three @Sql phases:
 *  - BEFORE_TEST_CLASS: create schema (idempotent via IF NOT EXISTS)
 *  - BEFORE_TEST_METHOD: seed minimal FK-respecting rows
 *  - AFTER_TEST_METHOD:  clean up seed rows
 *
 * Bulk-write assertions use scalar JPQL reads via EntityManager, not findById,
 * to avoid loading the full Device eager graph in the minimal test schema.
 */
@Sql(scripts = "/schema-pg.sql",        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/notes-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-notes-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class NotesRepositoryIT extends PostgresJpaIT {

    @Autowired
    NotesRepository notesRepository;

    @PersistenceContext
    EntityManager em;

    // ── COUNT ─────────────────────────────────────────────────────────────────

    @Test
    void getNotesCountByDeviceId_returnsCorrectCount() {
        // dev-n1 has 3 notes (note-1, note-2, note-g1)
        assertThat(notesRepository.getNotesCountByDeviceId("dev-n1")).isEqualTo(3);
    }

    @Test
    void getNotesCountByDeviceId_returnsZeroForUnknownDevice() {
        assertThat(notesRepository.getNotesCountByDeviceId("no-such-device")).isEqualTo(0);
    }

    // ── SELECT id queries ─────────────────────────────────────────────────────

    @Test
    void getGlobalNotesByDeviceId_returnsOnlyGlobalIds() {
        Set<String> ids = notesRepository.getGlobalNotesByDeviceId("dev-n1");
        assertThat(ids).containsExactlyInAnyOrder("note-g1");
    }

    @Test
    void getNoteIdsByDeviceId_returnsOnlyNonGlobalIds() {
        Set<String> ids = notesRepository.getNoteIdsByDeviceId("dev-n1");
        assertThat(ids).containsExactlyInAnyOrder("note-1", "note-2");
    }

    // ── DTO projection ────────────────────────────────────────────────────────

    @Test
    void getNotesByDeviceId_returnsConstructorProjection() {
        Set<Product_NotesDTO> notes = notesRepository.getNotesByDeviceId("dev-n1");
        assertThat(notes).hasSize(3);

        Product_NotesDTO globalNote = notes.stream()
                .filter(n -> "note-g1".equals(n.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(globalNote.getTitle()).isEqualTo("Global Title");
        assertThat(globalNote.getBody()).isEqualTo("Global Body");
        assertThat(globalNote.getDevice_id()).isEqualTo("dev-n1");
        assertThat(globalNote.getIs_global()).isEqualTo(1);

        Product_NotesDTO note1 = notes.stream()
                .filter(n -> "note-1".equals(n.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(note1.getIs_global()).isEqualTo(0);
    }

    @Test
    void getNotesByDeviceId_isolatesPerDevice() {
        Set<Product_NotesDTO> notes = notesRepository.getNotesByDeviceId("dev-n2");
        assertThat(notes).hasSize(1);
        assertThat(notes.iterator().next().getId()).isEqualTo("note-3");
    }

    // ── UPDATE global note ────────────────────────────────────────────────────

    @Test
    void updateGlobalNoteByNoteIdAndDeviceId_updatesBodyAndTitle() {
        notesRepository.updateGlobalNoteByNoteIdAndDeviceId("New Body", "New Title", "note-g1", "dev-n1");

        String body = (String) em.createQuery(
                "SELECT n.body FROM Notes n WHERE n.id = 'note-g1' AND n.device.id = 'dev-n1'")
                .getSingleResult();
        String title = (String) em.createQuery(
                "SELECT n.title FROM Notes n WHERE n.id = 'note-g1' AND n.device.id = 'dev-n1'")
                .getSingleResult();
        assertThat(body).isEqualTo("New Body");
        assertThat(title).isEqualTo("New Title");
    }

    @Test
    void updateGlobalNoteByNoteIdAndDeviceId_doesNotTouchNonGlobalNote() {
        // note-1 has is_global=0, so the WHERE ... AND n.is_global=1 should match nothing
        notesRepository.updateGlobalNoteByNoteIdAndDeviceId("X", "Y", "note-1", "dev-n1");

        String body = (String) em.createQuery(
                "SELECT n.body FROM Notes n WHERE n.id = 'note-1' AND n.device.id = 'dev-n1'")
                .getSingleResult();
        assertThat(body).isEqualTo("Body One"); // unchanged
    }

    // ── UPDATE non-global note ────────────────────────────────────────────────

    @Test
    void updateNoteByNoteIdAndDeviceId_updatesAnyNote() {
        notesRepository.updateNoteByNoteIdAndDeviceId("Updated Title", "Updated Body", "note-1", "dev-n1");

        String title = (String) em.createQuery(
                "SELECT n.title FROM Notes n WHERE n.id = 'note-1' AND n.device.id = 'dev-n1'")
                .getSingleResult();
        String body = (String) em.createQuery(
                "SELECT n.body FROM Notes n WHERE n.id = 'note-1' AND n.device.id = 'dev-n1'")
                .getSingleResult();
        assertThat(title).isEqualTo("Updated Title");
        assertThat(body).isEqualTo("Updated Body");
    }

    // ── DELETE specific note ──────────────────────────────────────────────────

    @Test
    void deleteNoteByNoteIdAndDeviceId_removesExactNote() {
        notesRepository.deleteNoteByNoteIdAndDeviceId("note-1", "dev-n1");

        Long count = em.createQuery(
                "SELECT COUNT(n) FROM Notes n WHERE n.id = 'note-1' AND n.device.id = 'dev-n1'",
                Long.class)
                .getSingleResult();
        assertThat(count).isZero();
        // other notes untouched
        assertThat(notesRepository.getNotesCountByDeviceId("dev-n1")).isEqualTo(2);
    }

    // ── DELETE global notes for device ────────────────────────────────────────

    @Test
    void deleteGlobalNotesByDeviceId_removesOnlyGlobalNotes() {
        notesRepository.deleteGlobalNotesByDeviceId("dev-n1");

        Long globalCount = em.createQuery(
                "SELECT COUNT(n) FROM Notes n WHERE n.device.id = 'dev-n1' AND n.is_global = 1",
                Long.class)
                .getSingleResult();
        assertThat(globalCount).isZero();

        // non-global notes remain
        Long nonGlobalCount = em.createQuery(
                "SELECT COUNT(n) FROM Notes n WHERE n.device.id = 'dev-n1' AND n.is_global = 0",
                Long.class)
                .getSingleResult();
        assertThat(nonGlobalCount).isEqualTo(2);
    }

    // ── DELETE all notes for device ───────────────────────────────────────────

    @Test
    void deleteNotesByDeviceId_removesAllNotesForDevice() {
        notesRepository.deleteNotesByDeviceId("dev-n1");

        Long count = em.createQuery(
                "SELECT COUNT(n) FROM Notes n WHERE n.device.id = 'dev-n1'",
                Long.class)
                .getSingleResult();
        assertThat(count).isZero();

        // notes for other device untouched
        assertThat(notesRepository.getNotesCountByDeviceId("dev-n2")).isEqualTo(1);
    }
}
