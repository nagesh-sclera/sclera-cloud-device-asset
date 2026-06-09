package io.sclera.Repository;

import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface MeasuringInstrumentAttributesRepository {
    /**
     * Inserts a measuring instrument attribute, or updates it when the identifier already exists.
     *
     * @param id the attribute identifier
     * @param name the attribute name
     * @param type the attribute type
     * @param unit the measurement unit
     * @param value the attribute value
     * @param protocol the attribute protocol
     * @param category the attribute category
     * @param primaryId the primary identifier
     * @param secondaryId the secondary identifier
     * @param tertiaryId the tertiary identifier
     * @param measuringInstrumentId the owning measuring instrument identifier
     * @param attributeIndex the attribute index
     */
    void upsertMeasuringInstrumentAttribute(String id, String name, String type, String unit, String value, String protocol, String category, String primaryId, String secondaryId, String tertiaryId, String measuringInstrumentId, Integer attributeIndex);

    /**
     * Returns the measuring instrument attribute with the given identifier.
     *
     * @param id the attribute identifier
     * @return the matching attribute
     */
    MeasuringInstrumentAttributesDTO getMeasuringInstrumentAttributeById(String id);

    /**
     * Returns all measuring instrument attributes.
     *
     * @return all attributes
     */
    List<MeasuringInstrumentAttributesDTO> getAllMeasuringInstrumentAttributes();

    /**
     * Returns the attributes belonging to the given measuring instrument.
     *
     * @param measuringInstrumentId the measuring instrument identifier
     * @return the matching attributes
     */
    List<MeasuringInstrumentAttributesDTO> getMeasuringInstrumentAttributesByMeasuringInstrumentId(String measuringInstrumentId);
    // Methods added on demand by compile loop.
}
