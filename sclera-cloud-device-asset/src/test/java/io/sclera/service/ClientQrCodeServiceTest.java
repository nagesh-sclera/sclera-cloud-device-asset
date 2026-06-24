package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.Repository.ClientQrCodeRepository;
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
 * Unit tests for ClientQrCodeService read/count/lookup methods.
 * DataSource-backed batch upsert is not covered here (heavy I/O).
 */
@ExtendWith(MockitoExtension.class)
class ClientQrCodeServiceTest {

    @Mock
    ClientQrCodeRepository clientQrCodeRepository;

    @Mock
    QrCodeService qrCodeService;

    @InjectMocks
    ClientQrCodeService service;

    @Test
    void getClientQrCodeCountByDeviceId_delegatesToRepo() {
        when(clientQrCodeRepository.getClientQrCodeCountByDeviceId("d1")).thenReturn(4);
        assertThat(service.getClientQrCodeCountByDeviceId("d1")).isEqualTo(4);
    }

    @Test
    void countByDeviceId_returnsIntCastOfRepoLong() {
        when(clientQrCodeRepository.countByDeviceId("d2")).thenReturn(7L);
        assertThat(service.countByDeviceId("d2")).isEqualTo(7);
    }

    @Test
    void getDeviceIdsTaggedToClientQrCode_delegates() {
        JSONArray arr = new JSONArray();
        when(clientQrCodeRepository.getDeviceIdsTaggedToClientQrCode("v1")).thenReturn(arr);
        assertThat(service.getDeviceIdsTaggedToClientQrCode("v1")).isSameAs(arr);
    }

    @Test
    void getLocationIdsTaggedToClientQrCode_delegates() {
        JSONArray arr = new JSONArray();
        when(clientQrCodeRepository.getLocationIdsTaggedToClientQrCode("v1")).thenReturn(arr);
        assertThat(service.getLocationIdsTaggedToClientQrCode("v1")).isSameAs(arr);
    }

    @Test
    void maxUpdatedClientQrCodeTimeStamp_delegates() {
        BigInteger ts = BigInteger.valueOf(9876543210L);
        when(clientQrCodeRepository.maxUpdatedClientQrCodeTimeStamp("d1")).thenReturn(ts);
        assertThat(service.maxUpdatedClientQrCodeTimeStamp("d1")).isSameAs(ts);
    }

    @Test
    void getClientQrCodeDetailsByIds_delegatesToQrCodeService() {
        Set<QrCodeDTO> codes = Set.of(mock(QrCodeDTO.class));
        Set<String> ids = Set.of("cqr1");
        when(qrCodeService.getClientQrCodeDetailsByIds(ids)).thenReturn(codes);
        assertThat(service.getClientQrCodeDetailsByIds(ids)).isSameAs(codes);
    }
}
