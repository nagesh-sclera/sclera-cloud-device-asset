package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.service.impl.UserActionLogDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Thin Dapr client delegating to the sclera-audit microservice.
 *
 * Replaces the no-op {@code io.sclera.service.ArchivedRecordService} stub.
 * The single void method swallows any exception (sidecar-down, network error)
 * with a WARN log so that call sites are never interrupted by audit failures.
 *
 * Path and verb are aligned with ArchivedRecordController (GET-only camelCase
 * skeleton endpoint). NOTE: the List body is lost under GET-only routing —
 * this is a known PoC limitation; real POST routing is a Wave-2 prerequisite.
 */
@Component
public class ArchivedRecordClient {

    private static final Logger log = LoggerFactory.getLogger(ArchivedRecordClient.class);
    private static final String APP_ID = "sclera-audit";

    private final DaprClient dapr;

    public ArchivedRecordClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code ArchivedRecordService#batchUpdateArchivedRecords}.
     * Maps to GET sclera-audit/archivedrecord/batchUpdateArchivedRecords.
     * Documented default: void (no return value); exceptions are swallowed.
     * NOTE: List body is lost under GET-only skeleton routing (Wave-2: needs POST).
     */
    public void batchUpdateArchivedRecords(List<UserActionLogDTO> logs) {
        try {
            dapr.invokeMethod(APP_ID, "archivedrecord/batchUpdateArchivedRecords", logs, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("ArchivedRecordClient.batchUpdateArchivedRecords failed; swallowing: {}", e.getMessage());
        }
    }
}
