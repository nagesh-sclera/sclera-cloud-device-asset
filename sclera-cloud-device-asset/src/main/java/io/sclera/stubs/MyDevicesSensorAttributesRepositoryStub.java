package io.sclera.stubs;

import io.sclera.Repository.MyDevicesSensorAttributesRepository;
import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MyDevicesSensorAttributesRepositoryStub implements MyDevicesSensorAttributesRepository {

    @Override
    public void upsertMeasuringInstrumentAttribute(String id, String name, String type, String unit,
                                                   String value, String protocol, String category, String primaryId, String secondaryId,
                                                   String tertiaryId, String measuringInstrumentId, Integer attributeIndex) {
    }

    @Override
    public MeasuringInstrumentAttributesDTO getMeasuringInstrumentAttributeById(String id) {
        return null;
    }

    @Override
    public List<MeasuringInstrumentAttributesDTO> getAllMeasuringInstrumentAttributes() {
        return Collections.emptyList();
    }

    @Override
    public List<MeasuringInstrumentAttributesDTO> getMeasuringInstrumentAttributesByMeasuringInstrumentId(String measuringInstrumentId) {
        return Collections.emptyList();
    }
}
