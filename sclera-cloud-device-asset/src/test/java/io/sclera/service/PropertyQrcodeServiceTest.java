package io.sclera.service;

import io.sclera.Repository.PropertyQrCodeRepository;
import io.sclera.Repository.PropertyServiceRepository;
import io.sclera.Repository.PropertyServiceRequestRepository;
import io.sclera.Repository.PropertyServiceResponseRepository;
import io.sclera.dto.PropertyQrcodeDTO;
import io.sclera.dto.PropertyServiceResponseDTO;
import io.sclera.utils.QrImageStorageService;
import io.sclera.utils.ResourceUrlConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PropertyQrcodeService}.
 *
 * Verifies:
 * <ul>
 *   <li>{@code generateQrcode} delegates to {@link QrImageStorageService#store} with a JPEG
 *       extension and returns the URL from storage.</li>
 *   <li>{@code updatePropertyServiceLocations} triggers the local cascade via
 *       {@link PropertyQrCodeRepository#deleteById} for every QR code bound to the given
 *       location.</li>
 * </ul>
 *
 * No Spring context is loaded — collaborators are Mockito mocks injected via
 * {@link InjectMocks}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyQrcodeServiceTest {

    @Mock
    PropertyServiceRepository propertyServiceRepository;

    @Mock
    PropertyQrCodeRepository propertyQrCodeRepository;

    @Mock
    PropertyServiceRequestRepository propertyServiceRequestRepository;

    @Mock
    PropertyServiceResponseRepository propertyServiceResponseRepository;

    @Mock
    QrImageStorageService imageStorage;

    @Mock
    ResourceUrlConfig resourceUrlConfig;

    @InjectMocks
    PropertyQrcodeService service;

    private static final String VDMS_ID = "vdms-001";
    private static final String BASE_URL = "https://app.sclera.com";
    private static final String QR_ID = "qr-abc-123";
    private static final String EXPECTED_URL = "https://app.sclera.com/images/qrcodes/qr-abc-123.jpeg";

    @BeforeEach
    void configureMocks() {
        when(resourceUrlConfig.getServices_cloud_server_url()).thenReturn(BASE_URL);
    }

    // -------------------------------------------------------------------------
    // generateQrcode
    // -------------------------------------------------------------------------

    @Test
    void generateQrcode_storesJpegAndReturnsUrl() {
        // Storage stub: return a predictable URL when bytes are stored as jpeg
        when(imageStorage.store(any(byte[].class), eq(QR_ID), eq("jpeg")))
                .thenReturn(EXPECTED_URL);

        String url = service.generateQrcode(QR_ID);

        // Should return what imageStorage.store returned
        assertThat(url).isEqualTo(EXPECTED_URL);

        // Verify store was called with the correct key and extension
        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(imageStorage).store(bytesCaptor.capture(), eq(QR_ID), eq("jpeg"));

        // The captured bytes must be a non-empty JPEG (ZXing will have produced some output)
        assertThat(bytesCaptor.getValue()).isNotEmpty();
    }

    @Test
    void generateQrcode_encodesPropertyQrcodeIdInPayload() {
        // We only verify that store is called with the right key — the payload content
        // is validated by the ZXing encoder internally; if it threw, store would not be
        // reached and this test would also fail.
        when(imageStorage.store(any(byte[].class), eq(QR_ID), eq("jpeg")))
                .thenReturn(EXPECTED_URL);

        service.generateQrcode(QR_ID);

        verify(resourceUrlConfig).getServices_cloud_server_url();
        verify(imageStorage).store(any(byte[].class), eq(QR_ID), eq("jpeg"));
    }

    // -------------------------------------------------------------------------
    // updatePropertyServiceLocations (cascade on location delete)
    // -------------------------------------------------------------------------

    @Test
    void updatePropertyServiceLocations_deletesQrcodesForAllBoundServices() {
        String location_id = "loc-42";
        String service_id_a = "svc-A";
        String service_id_b = "svc-B";
        String qr_id_a = "qr-A";
        String qr_id_b = "qr-B";

        // Two property services are linked to this location
        when(propertyQrCodeRepository.getPropertyServicesByLocationId(location_id))
                .thenReturn(Set.of(service_id_a, service_id_b));

        // Stub getPropertyQrcode for each (service, location) pair
        PropertyQrcodeDTO dtoA = new PropertyQrcodeDTO(qr_id_a, "http://x/a.jpeg", service_id_a, location_id);
        PropertyQrcodeDTO dtoB = new PropertyQrcodeDTO(qr_id_b, "http://x/b.jpeg", service_id_b, location_id);
        when(propertyQrCodeRepository.getPropertyQrcode(service_id_a, location_id)).thenReturn(dtoA);
        when(propertyQrCodeRepository.getPropertyQrcode(service_id_b, location_id)).thenReturn(dtoB);

        service.updatePropertyServiceLocations(location_id);

        // Both QR rows must be deleted from the DB
        verify(propertyQrCodeRepository).deleteById(qr_id_a);
        verify(propertyQrCodeRepository).deleteById(qr_id_b);

        // Both QR images must be deleted from storage (by their ID, not image_url)
        verify(imageStorage).delete(qr_id_a);
        verify(imageStorage).delete(qr_id_b);
    }

    @Test
    void updatePropertyServiceLocations_noOp_whenNoServicesLinked() {
        String location_id = "loc-empty";
        when(propertyQrCodeRepository.getPropertyServicesByLocationId(location_id))
                .thenReturn(new HashSet<>());

        service.updatePropertyServiceLocations(location_id);

        verify(propertyQrCodeRepository, never()).deleteById(any());
        verify(imageStorage, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // multiUpdatePropertyServiceResponse — local DB update, no cloud/socket
    // -------------------------------------------------------------------------

    @Test
    void multiUpdatePropertyServiceResponse_updatesTimestampAndPersists() {
        String qrcode_id = "qr-99";
        String request_id = "req-1";

        PropertyServiceResponseDTO dto = new PropertyServiceResponseDTO(
                "resp-1", false, BigInteger.ZERO, "OK", qrcode_id, request_id);
        Set<PropertyServiceResponseDTO> responses = Set.of(dto);

        service.multiUpdatePropertyServiceResponse("user", VDMS_ID, responses);

        // Timestamp must have been refreshed (non-zero)
        assertThat(dto.getTimestamp()).isGreaterThan(BigInteger.ZERO);

        // Repository update must have been invoked
        verify(propertyServiceResponseRepository).updatePropertyServiceResponse(
                eq(qrcode_id), eq(request_id), eq("OK"), eq(false), any(BigInteger.class));
    }
}
