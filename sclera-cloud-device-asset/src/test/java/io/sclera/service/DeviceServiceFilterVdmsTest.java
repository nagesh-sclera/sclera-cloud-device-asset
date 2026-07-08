package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.Repository.DeviceRepository;
import io.sclera.client.ClientNfcClient;
import io.sclera.client.NfcClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceFilterVdmsTest {

    @Mock DeviceRepository deviceRepository;
    @Mock io.sclera.service.QrCodeService qrCodeService;
    @Mock io.sclera.service.ClientQrCodeService clientQrCodeService;
    @Mock NfcClient nfcService;
    @Mock ClientNfcClient clientNfcService;

    @InjectMocks DeviceService deviceService;

    @Test
    void getDeviceIdsByFilter_usesPassedVdmsId_forQrLookup() {
        JSONArray qrTaggedIds = new JSONArray();
        qrTaggedIds.add("d1");

        when(qrCodeService.getDeviceIdsTaggedToQrCode("V-ARG")).thenReturn(qrTaggedIds);
        when(clientQrCodeService.getDeviceIdsTaggedToClientQrCode("V-ARG")).thenReturn(new JSONArray());

        deviceService.getDeviceIdsByFilter("V-ARG", List.of(), List.of(), null, List.of(),
                Boolean.TRUE, null, List.of());

        verify(qrCodeService).getDeviceIdsTaggedToQrCode(eq("V-ARG"));
        verify(deviceRepository).getDeviceIds(any(), any(), any(), any(), eq(Boolean.TRUE), any(), any(), any(), any());
    }
}
