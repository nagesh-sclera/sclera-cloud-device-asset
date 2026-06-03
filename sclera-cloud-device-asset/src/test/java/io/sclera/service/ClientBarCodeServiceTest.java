package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.Repository.ClientBarCodeRepository;
import io.sclera.dto.ClientBarCodeDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean delegate methods of ClientBarCodeService. The DataSource-backed
 * batch upsert and the API-pagination sync methods are deferred (heavy I/O).
 */
@ExtendWith(MockitoExtension.class)
class ClientBarCodeServiceTest {

    @Mock ClientBarCodeRepository clientBarCodeRepository;

    @InjectMocks ClientBarCodeService service;

    @Test
    void getClientBarCodeCountByDeviceId_delegates() {
        when(clientBarCodeRepository.getClientBarCodeCountByDeviceId("d1")).thenReturn(7);
        assertThat(service.getClientBarCodeCountByDeviceId("d1")).isEqualTo(7);
    }

    @Test
    void getLocationIdsTaggedToClientBarCode_delegates() {
        JSONArray arr = new JSONArray();
        when(clientBarCodeRepository.getLocationIdsTaggedToClientBarCode("v1")).thenReturn(arr);
        assertThat(service.getLocationIdsTaggedToClientBarCode("v1")).isSameAs(arr);
    }

    @Test
    void getDeviceIdsTaggedToClientBarCode_delegates() {
        JSONArray arr = new JSONArray();
        when(clientBarCodeRepository.getDeviceIdsTaggedToClientBarCode("v1")).thenReturn(arr);
        assertThat(service.getDeviceIdsTaggedToClientBarCode("v1")).isSameAs(arr);
    }

    @Test
    void getBarCodesByLocationIds_delegates() {
        Set<ClientBarCodeDTO> codes = Set.of(mock(ClientBarCodeDTO.class));
        Set<String> ids = Set.of("l1");
        when(clientBarCodeRepository.getBarCodesByLocationIds(ids)).thenReturn(codes);
        assertThat(service.getBarCodesByLocationIds(ids)).isSameAs(codes);
    }

    @Test
    void getBarCodesByDeviceIds_delegates() {
        Set<ClientBarCodeDTO> codes = Set.of(mock(ClientBarCodeDTO.class));
        Set<String> ids = Set.of("d1");
        when(clientBarCodeRepository.getBarCodesByDeviceIds(ids)).thenReturn(codes);
        assertThat(service.getBarCodesByDeviceIds(ids)).isSameAs(codes);
    }
}
