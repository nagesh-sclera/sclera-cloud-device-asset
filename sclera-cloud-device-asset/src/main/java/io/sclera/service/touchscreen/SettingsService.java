package io.sclera.service.touchscreen;

import io.sclera.dto.touchscreen.settings.InterfaceDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/** STUB: edge-only touchscreen */
@Service
public class SettingsService {
    public List<InterfaceDTO> getSystemInterfaces() {
        StubLog.warn("SettingsService", "getSystemInterfaces", "edge-local");
        return Collections.emptyList();
    }
}
