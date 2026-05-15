package io.sclera.service;

import io.sclera.dto.PolyLensDeviceDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class PolyLensService {
    public Set<PolyLensDeviceDTO> getAllPolyLensDevices(String a, String b, String c, String d) { return Collections.emptySet(); }
    public void updatePolyLensDeviceId(String oldId, String newId, Set<String> ids) {}
    public Integer getPolyLensDeviceCountByDeviceId(String deviceId) { return 0; }
}
