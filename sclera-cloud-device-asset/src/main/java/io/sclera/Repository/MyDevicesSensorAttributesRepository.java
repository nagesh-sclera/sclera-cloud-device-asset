package io.sclera.Repository;

import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface MyDevicesSensorAttributesRepository {
    void upsertMeasuringInstrumentAttribute(String id, String name, String type, String unit,
                                            String value, String protocol, String category, String primaryId, String secondaryId,
                                            String tertiaryId, String measuringInstrumentId, Integer attributeIndex);

    MeasuringInstrumentAttributesDTO getMeasuringInstrumentAttributeById(String id);

    List<MeasuringInstrumentAttributesDTO> getAllMeasuringInstrumentAttributes();

    List<MeasuringInstrumentAttributesDTO> getMeasuringInstrumentAttributesByMeasuringInstrumentId(String measuringInstrumentId);
    // Methods added on demand by compile loop.
}
