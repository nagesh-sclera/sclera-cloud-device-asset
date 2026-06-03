package io.sclera.service;

import io.sclera.Repository.LocationHistoryRepository;
import io.sclera.dto.LocationHistoryDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for LocationHistoryService: addLocationHistory metadata stamping, id generation,
 * the type/status -> description branches, and the getLocationHistory delegate.
 */
@ExtendWith(MockitoExtension.class)
class LocationHistoryServiceTest {

    @Mock LocationHistoryRepository locationHistoryRepository;

    @InjectMocks LocationHistoryService service;

    private LocationHistoryDTO dto(String id, String type, String status) {
        LocationHistoryDTO d = mock(LocationHistoryDTO.class);
        lenient().when(d.getId()).thenReturn(id);
        lenient().when(d.getType()).thenReturn(type);
        lenient().when(d.getStatus()).thenReturn(status);
        return d;
    }

    @Test
    void addLocationHistory_qrTag_setsTagDescriptionAndMetadata() {
        LocationHistoryDTO d = dto("h1", "qr_code", "tag");

        service.addLocationHistory("user", "v1", d);

        verify(d).setUpdated_email("user");
        verify(d).setUpdated_timestamp(any());
        verify(d).setDescription("Location successfully tagged to QR code.");
        verify(locationHistoryRepository).addLocationHistory(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void addLocationHistory_qrRetag_setsRetagDescription() {
        LocationHistoryDTO d = dto("h1", "qr_code", "retag");
        service.addLocationHistory("user", "v1", d);
        verify(d).setDescription("Location successfully retagged to QR code.");
    }

    @Test
    void addLocationHistory_nfcTag_setsNfcDescription() {
        LocationHistoryDTO d = dto("h1", "nfc", "tag");
        service.addLocationHistory("user", "v1", d);
        verify(d).setDescription("Location successfully tagged to NFC.");
    }

    @Test
    void addLocationHistory_nullId_generatesId() {
        LocationHistoryDTO d = dto(null, "nfc", "retag");
        service.addLocationHistory("user", "v1", d);
        verify(d).setId(any());
    }

    @Test
    void getLocationHistory_delegates() {
        Set<LocationHistoryDTO> hist = Set.of(mock(LocationHistoryDTO.class));
        when(locationHistoryRepository.getLocationHistory("l1")).thenReturn(hist);
        assertThat(service.getLocationHistory("user", "v1", "l1")).isSameAs(hist);
    }
}
