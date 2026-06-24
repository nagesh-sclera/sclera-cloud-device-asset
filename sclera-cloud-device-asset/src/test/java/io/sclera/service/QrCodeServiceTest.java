package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.Repository.QrCodeRepository;
import io.sclera.dto.QrCodeDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for QrCodeService read/count/lookup methods.
 * DataSource-backed batch upsert is not covered here (heavy I/O).
 */
@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @Mock
    QrCodeRepository qrCodeRepository;

    @InjectMocks
    QrCodeService service;

    @Test
    void getQrCodeCountByDeviceId_delegatesToRepo() {
        when(qrCodeRepository.getQrCodeCountByDeviceId("d1")).thenReturn(3);
        assertThat(service.getQrCodeCountByDeviceId("d1")).isEqualTo(3);
    }

    @Test
    void countByDeviceId_returnsIntCastOfRepoLong() {
        when(qrCodeRepository.countByDeviceId("d2")).thenReturn(5L);
        assertThat(service.countByDeviceId("d2")).isEqualTo(5);
    }

    @Test
    void getQrCodesByDeviceIds_delegates() {
        Set<QrCodeDTO> codes = Set.of(mock(QrCodeDTO.class));
        Set<String> ids = Set.of("d1");
        when(qrCodeRepository.getQrCodesByDeviceIds(ids)).thenReturn(codes);
        assertThat(service.getQrCodesByDeviceIds(ids)).isSameAs(codes);
    }

    @Test
    void getQrCodesByLocationIds_delegates() {
        Set<QrCodeDTO> codes = Set.of(mock(QrCodeDTO.class));
        Set<String> ids = Set.of("l1");
        when(qrCodeRepository.getQrCodesByLocationIds(ids)).thenReturn(codes);
        assertThat(service.getQrCodesByLocationIds(ids)).isSameAs(codes);
    }

    @Test
    void getDeviceIdsTaggedToQrCode_delegates() {
        JSONArray arr = new JSONArray();
        when(qrCodeRepository.getDeviceIdsTaggedToQrCode("v1")).thenReturn(arr);
        assertThat(service.getDeviceIdsTaggedToQrCode("v1")).isSameAs(arr);
    }

    @Test
    void getLocationIdsTaggedToQrCode_delegates() {
        JSONArray arr = new JSONArray();
        when(qrCodeRepository.getLocationIdsTaggedToQrCode("v1")).thenReturn(arr);
        assertThat(service.getLocationIdsTaggedToQrCode("v1")).isSameAs(arr);
    }

    @Test
    void getMaxUpdatedQrCodeTimeStamp_delegates() {
        BigInteger ts = BigInteger.valueOf(1234567890L);
        when(qrCodeRepository.getMaxUpdatedQrCodeTimeStamp("d1")).thenReturn(ts);
        assertThat(service.getMaxUpdatedQrCodeTimeStamp("d1")).isSameAs(ts);
    }

    @Test
    void getQrCodeDetailsByIds_delegates() {
        Set<QrCodeDTO> codes = Set.of(mock(QrCodeDTO.class));
        Set<String> ids = Set.of("qr1");
        when(qrCodeRepository.getQrCodeDetailsByIds(ids)).thenReturn(codes);
        assertThat(service.getQrCodeDetailsByIds(ids)).isSameAs(codes);
    }

    @Test
    void getClientQrCodeDetailsByIds_delegates() {
        Set<QrCodeDTO> codes = Set.of(mock(QrCodeDTO.class));
        Set<String> ids = Set.of("cqr1");
        when(qrCodeRepository.getClientQrCodeDetailsByIds(ids)).thenReturn(codes);
        assertThat(service.getClientQrCodeDetailsByIds(ids)).isSameAs(codes);
    }
}
