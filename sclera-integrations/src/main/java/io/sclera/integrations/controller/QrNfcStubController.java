package io.sclera.integrations.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * No-op stubs for the QR-code / NFC integration endpoints. The cloud-device-asset
 * clients (QrCodeClient, ClientQrCodeClient, NfcClient, ClientNfcClient) invoke
 * these via Dapr while building the asset list, but ignore the response and return
 * hardcoded defaults. These weren't part of the integrations skeleton, so the calls
 * 404'd and spammed WARN logs on every list load. Returning 200 here silences that
 * without changing behaviour (the clients still return their defaults).
 */
@RestController
public class QrNfcStubController {

    @RequestMapping({ "/qrCode/**", "/clientQrCode/**", "/nfc/**", "/clientNfc/**" })
    public List<Object> stub() {
        return Collections.emptyList();
    }
}
