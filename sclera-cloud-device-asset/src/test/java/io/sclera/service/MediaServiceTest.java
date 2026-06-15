package io.sclera.service;

import io.sclera.Repository.MediaRepository;
import io.sclera.dto.DocumentMediaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for MediaService: upsert id-generation vs update branch, tag (add/replace),
 * untag loops, offset pagination, delete cascade, and updateMediaDeviceId retain branch.
 */
@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock MediaRepository mediaRepository;
    @Mock DeviceService deviceService;

    @InjectMocks MediaService service;

    private DocumentMediaDTO media(String id, String deviceId) {
        DocumentMediaDTO m = mock(DocumentMediaDTO.class);
        org.mockito.Mockito.lenient().when(m.getId()).thenReturn(id);
        org.mockito.Mockito.lenient().when(m.getDevice_id()).thenReturn(deviceId);
        return m;
    }

    // ---- upsertMedia -----------------------------------------------------

    @Test
    void upsertMedia_nullId_generatesIdAndReturnsIt() {
        DocumentMediaDTO m = media(null, null);

        String id = service.upsertMedia("user", "v", m);

        assertThat(id).isNotNull();
        verify(m).setId(any());
        verify(mediaRepository).upsertMedia(any(), any(), any(), any(), any(), eq("user"), any(), isNull());
    }

    @Test
    void upsertMedia_existingId_returnsSameId() {
        DocumentMediaDTO m = media("m1", null);

        String id = service.upsertMedia("user", "v", m);

        assertThat(id).isEqualTo("m1");
        verify(mediaRepository).upsertMedia(eq("m1"), any(), any(), any(), any(), eq("user"), isNull(), isNull());
    }

    // ---- pagination offsets ----------------------------------------------

    @Test
    void getMedias_computesOffset() {
        List<DocumentMediaDTO> medias = List.of(media("m1", null));
        when(mediaRepository.getMedias(eq("key"), eq(PageRequest.of(2, 10)))).thenReturn(medias); // page 3
        assertThat(service.getMedias("u", "v", 3, 10, "key")).containsAll(medias);
    }

    @Test
    void getMediasByDeviceId_computesOffset() {
        List<DocumentMediaDTO> medias = List.of(media("m1", null));
        when(mediaRepository.getMediasByDeviceIdByPagination(eq("d1"), eq(PageRequest.of(1, 5)))).thenReturn(medias); // page 2
        assertThat(service.getMediasByDeviceId("u", "v", "d1", 2, 5)).containsAll(medias);
    }

    @Test
    void getMediasCountByDeviceId_delegates() {
        when(mediaRepository.getMediasCountByDeviceId("d1")).thenReturn(4);
        assertThat(service.getMediasCountByDeviceId("d1")).isEqualTo(4);
    }

    // ---- delete cascade --------------------------------------------------

    @Test
    void deleteMedia_untagsThenDeletes() {
        when(mediaRepository.getMediaByDeviceId("m1")).thenReturn(List.of("d1"));

        service.deleteMedia("u", "v", "m1");

        verify(mediaRepository).deleteTagRecordByMediaId("m1");
        verify(deviceService).updateDeviceMediaCountByDeviceId("d1");
        verify(mediaRepository).deleteMediaById("m1");
    }

    // ---- tagMediaToDevice ------------------------------------------------

    @Test
    void tagMediaToDevice_add_tagsAndUpdatesCount() {
        service.tagMediaToDevice("u", "v", "add", Set.of(media("m1", "d1")));
        verify(mediaRepository).tagMediaToDevice("m1", "d1");
        verify(deviceService).updateDeviceMediaCountByDeviceId("d1");
    }

    @Test
    void tagMediaToDevice_replace_untagsThenTags() {
        when(mediaRepository.getMediasByDeviceId("d1")).thenReturn(Set.of());

        service.tagMediaToDevice("u", "v", "replace", Set.of(media("m1", "d1")));

        verify(mediaRepository).getMediasByDeviceId("d1"); // untag path consulted
        verify(mediaRepository).tagMediaToDevice("m1", "d1");
    }

    // ---- untagMediaToDevice ----------------------------------------------

    @Test
    void untagMediaToDevice_untagsEach() {
        service.untagMediaToDevice("u", "v", Set.of(media("m1", "d1")));
        verify(mediaRepository).untagMediaToDevice("m1", "d1");
        verify(deviceService).updateDeviceMediaCountByDeviceId("d1");
    }

    // ---- updateMediaDeviceId ---------------------------------------------

    @Test
    void updateMediaDeviceId_retained_updatesBothCounts() {
        service.updateMediaDeviceId("d1", "old", Set.of("old"));
        verify(mediaRepository).updateMediaDeviceId("d1", "old");
        verify(deviceService).updateDeviceMediaCountByDeviceId("d1");
        verify(deviceService).updateDeviceMediaCountByDeviceId("old");
    }

    @Test
    void updateMediaDeviceId_notRetained_updatesOnlyNew() {
        service.updateMediaDeviceId("d1", "old", Set.of("other"));
        verify(deviceService).updateDeviceMediaCountByDeviceId("d1");
        verify(deviceService, never()).updateDeviceMediaCountByDeviceId("old");
    }
}
