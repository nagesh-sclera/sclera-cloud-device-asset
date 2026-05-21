package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.PhonebookAddressDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-identity microservice (CP-2).
 *
 * Replaces the no-op {@code io.sclera.service.PhonebookService} stub.
 * Methods either return documented defaults (null) on exception or swallow
 * exceptions with a WARN log so that call sites are never interrupted.
 *
 * NOTE: addPhoneBookByDeviceId passes a Set body under GET routing — the body
 * is silently dropped. Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class PhonebookClient {

    private static final Logger log = LoggerFactory.getLogger(PhonebookClient.class);
    private static final String APP_ID = "sclera-identity";

    private final DaprClient dapr;

    public PhonebookClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code PhonebookService#addPhoneBookByDeviceId}.
     * Maps to GET sclera-identity/phonebook/addPhoneBookByDeviceId.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void addPhoneBookByDeviceId(String username, String vdmsid, String dockername,
                                       Set<PhonebookAddressDto> vendors, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsid", vdmsid);
        payload.put("dockername", dockername);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "phonebook/addPhoneBookByDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("PhonebookClient.addPhoneBookByDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code PhonebookService#getPhoneAddressById}.
     * Maps to GET sclera-identity/phonebook/getPhoneAddressById.
     * Returns null on sidecar failure (documented stub default).
     */
    public PhonebookAddressDto getPhoneAddressById(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            return dapr.invokeMethod(APP_ID, "phonebook/getPhoneAddressById", payload, HttpExtension.GET, PhonebookAddressDto.class).block();
        } catch (Exception e) {
            log.warn("PhonebookClient.getPhoneAddressById failed; returning null: {}", e.getMessage());
            return null;
        }
    }
}
