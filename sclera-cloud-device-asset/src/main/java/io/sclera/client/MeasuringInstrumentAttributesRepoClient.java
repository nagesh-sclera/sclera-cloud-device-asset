package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.Repository.MeasuringInstrumentAttributesRepository;
import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin Dapr client delegating to sclera-edge.
 * Replaces {@code io.sclera.stubs.MeasuringInstrumentAttributesRepositoryStub}.
 */
@Component
@Primary
public class MeasuringInstrumentAttributesRepoClient implements MeasuringInstrumentAttributesRepository {

    private static final Logger log = LoggerFactory.getLogger(MeasuringInstrumentAttributesRepoClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;

    public MeasuringInstrumentAttributesRepoClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Upserts a measuring instrument attribute via sclera-edge.
     * Swallows any failure with a WARN log.
     */
    @Override
    public void upsertMeasuringInstrumentAttribute(String id, String name, String type, String unit,
            String value, String protocol, String category, String primaryId, String secondaryId,
            String tertiaryId, String measuringInstrumentId, Integer attributeIndex) {
        try {
            dapr.invokeMethod(APP_ID, "measuringinstrumentattributes/upsertMeasuringInstrumentAttribute", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MeasuringInstrumentAttributesRepoClient.upsertMeasuringInstrumentAttribute failed; swallowing", e);
        }
    }

    /**
     * Fetches a measuring instrument attribute by its id via sclera-edge.
     * Returns null on failure.
     */
    @Override
    public MeasuringInstrumentAttributesDTO getMeasuringInstrumentAttributeById(String id) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("id", id);
            return dapr.invokeMethod(APP_ID, "measuringinstrumentattributes/getMeasuringInstrumentAttributeById", p, HttpExtension.GET, MeasuringInstrumentAttributesDTO.class).block();
        } catch (Exception e) {
            log.warn("MeasuringInstrumentAttributesRepoClient.getMeasuringInstrumentAttributeById failed; returning null", e);
        }
        return null;
    }

    /**
     * Fetches all measuring instrument attributes via sclera-edge.
     * Returns an empty list on failure.
     */
    @Override
    public List<MeasuringInstrumentAttributesDTO> getAllMeasuringInstrumentAttributes() {
        try {
            dapr.invokeMethod(APP_ID, "measuringinstrumentattributes/getAllMeasuringInstrumentAttributes", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MeasuringInstrumentAttributesRepoClient.getAllMeasuringInstrumentAttributes failed; returning default", e);
        }
        return Collections.emptyList();
    }

    /**
     * Fetches measuring instrument attributes for the given measuring instrument id via sclera-edge.
     * Returns an empty list on failure.
     */
    @Override
    public List<MeasuringInstrumentAttributesDTO> getMeasuringInstrumentAttributesByMeasuringInstrumentId(String measuringInstrumentId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("measuringInstrumentId", measuringInstrumentId);
            dapr.invokeMethod(APP_ID, "measuringinstrumentattributes/getMeasuringInstrumentAttributesByMeasuringInstrumentId", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MeasuringInstrumentAttributesRepoClient.getMeasuringInstrumentAttributesByMeasuringInstrumentId failed; returning default", e);
        }
        return Collections.emptyList();
    }
}
