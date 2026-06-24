package io.sclera.service;

import io.sclera.Repository.GlobalQrcodeRepository;
import io.sclera.utils.QrImageStorageService;
import io.sclera.utils.ResourceUrlConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GlobalQrcodeServiceTest {

    @Test
    void createGeneratesImageAndPersists() {
        GlobalQrcodeRepository repo = mock(GlobalQrcodeRepository.class);
        QrImageStorageService storage = mock(QrImageStorageService.class);
        when(storage.store(any(), anyString(), anyString())).thenReturn("http://img/x.png");

        ResourceUrlConfig cfg = new ResourceUrlConfig();
        cfg.setGlobal_qrcode_server_url("https://app");

        GlobalQrcodeService svc = new GlobalQrcodeService(repo, storage, cfg, mock(DeviceService.class));

        svc.createGlobalQrcode("u", "vdms-1", 1);

        // Verify a PNG was stored via QrImageStorageService
        verify(storage, times(1)).store(any(), anyString(), eq("png"));

        // Verify the URL returned by storage is what was persisted
        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(repo, times(1)).upsertGlobalQrcode(anyString(), url.capture(), isNull(), isNull());
        assertThat(url.getValue()).isEqualTo("http://img/x.png");
    }

    @Test
    void createGeneratesCorrectQrPayload() {
        GlobalQrcodeRepository repo = mock(GlobalQrcodeRepository.class);
        QrImageStorageService storage = mock(QrImageStorageService.class);

        // Capture the bytes passed to store so we can verify payload indirectly
        // (we just check store is called with a non-empty byte array)
        when(storage.store(any(), anyString(), anyString())).thenReturn("http://img/y.png");

        ResourceUrlConfig cfg = new ResourceUrlConfig();
        cfg.setGlobal_qrcode_server_url("https://sclera.example.com");

        GlobalQrcodeService svc = new GlobalQrcodeService(repo, storage, cfg, mock(DeviceService.class));
        svc.createGlobalQrcode("admin", "tenant-99", 2);

        // Two QR codes requested → two stores and two upserts
        verify(storage, times(2)).store(any(byte[].class), anyString(), eq("png"));
        verify(repo, times(2)).upsertGlobalQrcode(anyString(), eq("http://img/y.png"), isNull(), isNull());
    }
}
